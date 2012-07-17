package com.vmat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class GithubDetailActivity extends SherlockActivity 
{
	private static final String TAG = "GithubDetailActivity";
	private static final String[] PROJECTION = { "_id", "message", "author", "timestamp" };
	
	private ListView commitList;
	private TextView projectTitleText;
	
	private CursorAdapter adapter;
	private int projectId;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.github_detail);
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		
		commitList = (ListView)findViewById(R.id.commit_list);
		projectTitleText = (TextView)findViewById(R.id.text_project_title);
		
		projectId = getIntent().getIntExtra("project_id", -1);
		SQLiteDatabase db = new GeneralOpenHelper(this).getReadableDatabase();
		
		// Get info for top view
		Cursor topViewCursor = db.query("github_projects", null, "project_id="+projectId, null, null, null, null);
		if (topViewCursor.moveToFirst())
		{
			projectTitleText.setText(topViewCursor.getString(topViewCursor.getColumnIndex("title")));
		}
		topViewCursor.close();
		
		// May need to be sort-ordered by date...
		Cursor c = db.query("github_commits", PROJECTION, "project_id="+projectId, null, null, null, null);
		adapter = new GithubDetailRowAdapter();
		adapter.swapCursor(c);
		commitList.setAdapter(adapter);
		
		Cursor getSha = db.query("github_projects", new String[] { "last_sha" }, "project_id="+projectId, null, null, null, null);
		String sha = null;
		if (getSha.moveToFirst())
		{
			sha = getSha.getString(getSha.getColumnIndex("last_sha"));
		}
		getSha.close();
		db.close();
		new FetchGithubTask().execute(sha);
	}
	
	private class FetchGithubTask extends AsyncTask<String, Void, String>
	{
		// For now only downloads the one project. In the future when 
		// project locations are standardized, multiple project downloads 
		// can be automated, even selecting by user-involved projects.
		private String commits_url = "https://api.github.com/repos/whalenrp/vandy-mobile-scheduler/commits";
		
		protected String doInBackground(String... shas)
		{
			if (shas[0] != null)
				commits_url += "sha?="+shas[0];
			
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
		        final HttpParams params = httpClient.getParams();
		        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
		        HttpConnectionParams.setSoTimeout(params, 30 * 1000);
		        ConnManagerParams.setTimeout(params, 30 * 1000);
		        
		        HttpGet httpget = new HttpGet(commits_url);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				return httpClient.execute(httpget, responseHandler);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				httpClient.getConnectionManager().shutdown();
			}
			return "";
		}
		
		protected void onPostExecute(String results)
		{
			try
			{
				JSONArray commits = new JSONArray(results);
				SQLiteDatabase db = new GeneralOpenHelper(getApplicationContext()).getWritableDatabase();
				for (int i = 0; i < commits.length(); i++)
				{
					JSONObject o = commits.getJSONObject(i);
					String name = o.getJSONObject("commit").getJSONObject("author").getString("name");
					String date = o.getJSONObject("commit").getJSONObject("author").getString("date");
					String message = o.getJSONObject("commit").getString("message");
					
					ContentValues cv = new ContentValues(4);
					cv.put("project_id", projectId);
					cv.put("author", name);
					cv.put("timestamp", date);
					cv.put("message", message);
					
					// All downloaded commits are guaranteed to be new.
					long newId = db.insert("github_commits", null, cv);
					Log.v(TAG, "Row inserted with id " + newId);
				}
				// Needs to remember the newest 'sha' to store for next time.
				if (commits.length() > 0)
				{
					String latest_sha = commits.getJSONObject(0).getString("sha");
					ContentValues cv = new ContentValues(1);
					cv.put("last_sha", latest_sha);
					int i = db.update("github_projects", cv, "project_id="+projectId, null);
					Log.v(TAG, i + " row(s) updated with new sha " + latest_sha);
				}
				
				db.close();
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	private class GithubDetailRowAdapter extends CursorAdapter
	{
		public GithubDetailRowAdapter()
		{
			super(getApplicationContext(), null, 0);
		}
		
		public void bindView(View view, Context context, Cursor cursor) 
		{
			TextView message = (TextView)view.findViewById(R.id.text_commit_title);
			TextView author = (TextView)view.findViewById(R.id.text_commit_author);
			TextView timestamp = (TextView)view.findViewById(R.id.text_commit_timestamp);
			
			// Column names from the "github_commits" table
			message.setText(cursor.getString(cursor.getColumnIndex("message")));
			author.setText(cursor.getString(cursor.getColumnIndex("author")));
			
			String rawstamp = cursor.getString(cursor.getColumnIndex("timestamp"));
			try
			{
				timestamp.setText(convertDate(rawstamp));
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) 
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return inflater.inflate(R.layout.github_detail_row, null);
		}
		
	}
	
	private CharSequence convertDate(String tstamp) throws ParseException
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return DateFormat.format("MMM dd, yyyy h:mmaa", format.parse(tstamp));
	}
	
}
