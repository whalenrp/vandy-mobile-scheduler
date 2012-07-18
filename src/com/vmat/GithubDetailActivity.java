package com.vmat;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	
		String commits_url = "";
		try 
		{
			JSONObject o = new JSONObject(getIntent().getStringExtra("teamJSON"));
			projectId = o.getJSONObject("app").getInt("id");
			commits_url = o.getJSONObject("app").getString("url");
			projectTitleText.setText(o.getJSONObject("app").getString("name"));
		} 
		catch (JSONException e) 
		{
			Log.e(TAG, e.toString());
		} 
		
		SQLiteDatabase db = new GeneralOpenHelper(this).getReadableDatabase();
		
		// May need to be sort-ordered by date...
		Cursor c = db.query("github_commits", PROJECTION, "project_id="+projectId,
				null, null, null, null);
		adapter = new GithubDetailRowAdapter();
		adapter.swapCursor(c);
		commitList.setAdapter(adapter);
		
		// Lazy loading of github_projects row. If it doesn't already exist, create it here.
		String sha = null;
		Cursor proj = db.query("github_projects", new String[] { "last_sha" },
									"project_id="+projectId, null, null, null, null);
		if (!proj.moveToFirst())
		{
			ContentValues cv = new ContentValues(1);
			cv.put("project_id", projectId);
			db.insert("github_projects", null, cv);
		}
		else
		{
			sha = proj.getString(proj.getColumnIndex("last_sha"));
		}
		proj.close();
		db.close();
		if (sha != null)
		{
			commits_url += "?sha=" + sha;
		}
		new FetchGithubTask().execute(commits_url);
	}
	
	private class FetchGithubTask extends AsyncTask<String, Void, String>
	{
		private static final String TAG = "GithubDetailActivity$FetchGithubTask";
		// For now only downloads the one project. In the future when 
		// project locations are standardized, multiple project downloads 
		// can be automated, even selecting by user-involved projects.
		
		protected String doInBackground(String... urls)
		{
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
		        final HttpParams params = httpClient.getParams();
		        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
		        HttpConnectionParams.setSoTimeout(params, 30 * 1000);
		        ConnManagerParams.setTimeout(params, 30 * 1000);
		        
		        HttpGet httpget = new HttpGet(urls[0]);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				return httpClient.execute(httpget, responseHandler);
			}
			catch (Exception e)
			{
				Log.w(TAG, e.toString());
			}
			finally
			{
				httpClient.getConnectionManager().shutdown();
			}
			return "";
		}
		
		protected void onPostExecute(String results)
		{
			SQLiteDatabase db = new GeneralOpenHelper(getApplicationContext()).getWritableDatabase();
			try
			{
				JSONArray commits = new JSONArray(results);
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
			
				Cursor c = db.query("github_commits", PROJECTION, "project_id="+projectId, null, null, null, null);
				adapter.swapCursor(c);
			}
			catch (JSONException e)
			{
				Log.w(TAG, e.toString());
			}
			finally
			{
				db.close();
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
