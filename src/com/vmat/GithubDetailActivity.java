package com.vmat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class GithubDetailActivity extends SherlockActivity 
{
	private static final String TAG = "GithubDetailActivity";
	private static final String[] PROJECTION = { "_id", "message", "author", "timestamp" };
	
	private ListView commitList;
	private TextView projectTitleText;
	private Button emailButton;
	private TextView membersText;
	
	private CursorAdapter adapter;
	private int projectId;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.github_detail);
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		
		commitList = (ListView)findViewById(R.id.commit_list);
		projectTitleText = (TextView)findViewById(R.id.text_project_title);
		emailButton = (Button)findViewById(R.id.btn_email);
		membersText = (TextView)findViewById(R.id.text_members);
	
		String commits_url = "";
		List<String> names = new ArrayList<String>();
		List<String> emails = new ArrayList<String>();
		try 
		{
			JSONObject o = new JSONObject(getIntent().getStringExtra("teamJSON"));
			projectId = o.getJSONObject("app").getInt("github_id");
			commits_url = o.getJSONObject("app").getString("url");
			projectTitleText.setText(o.getJSONObject("app").getString("name"));
			JSONArray members = o.getJSONArray("members");
			for (int i = 0; i < members.length(); i++)
			{
				JSONObject j = members.getJSONObject(i);
				names.add(j.getString("name"));
				emails.add(j.getString("email"));
			}
		} 
		catch (JSONException e) 
		{
			Log.e(TAG, e.toString());
		}
		membersText.setText(join(names, ", "));
		
		final List<String> emailList = emails;
		emailButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri data = Uri.parse("mailto:" + join(emailList, ","));
				i.setData(data);
				startActivity(i);
			}
		});
		
		SQLiteDatabase db = new GeneralOpenHelper(this).getReadableDatabase();
		
		// May need to be sort-ordered by date...
		Cursor c = db.query("github_commits", PROJECTION, "project_id="+projectId,
				null, null, null, null);
		adapter = new GithubDetailRowAdapter();
		adapter.swapCursor(c);
		commitList.setAdapter(adapter);
		
		db.close();
		new FetchGithubTask().execute(commits_url);
	}
	
	private class FetchGithubTask extends AsyncTask<String, Void, String>
	{
		private static final String TAG = "GithubDetailActivity$FetchGithubTask";
		
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
				Cursor c = db.query("github_commits", new String[] { "sha" },
									null, null, null, null, null);
				Set<String> currentShas = new HashSet<String>();
				while (c.moveToNext())
				{
					currentShas.add(c.getString(c.getColumnIndex("sha")));
				}
				
				JSONArray commits = new JSONArray(results);
				for (int i = 0; i < commits.length(); i++)
				{
					JSONObject o = commits.getJSONObject(i);
					String name = o.getJSONObject("commit").getJSONObject("author").getString("name");
					String date = o.getJSONObject("commit").getJSONObject("author").getString("date");
					String message = o.getJSONObject("commit").getString("message");
					String sha = o.getString("sha");
					
					ContentValues cv = new ContentValues(5);
					cv.put("project_id", projectId);
					cv.put("author", name);
					cv.put("timestamp", date);
					cv.put("message", message);
					cv.put("sha", sha);
					
					if (!currentShas.contains(sha))
					{
						long newId = db.insert("github_commits", null, cv);
						Log.v(TAG, "Row inserted with id " + newId);
					}
				}
			
				Cursor refreshList = db.query("github_commits", PROJECTION, "project_id="+projectId,
											  null, null, null, null);
				adapter.swapCursor(refreshList);
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
	
	private String join(List<String> values, String delimiter)
	{
		if (values.size() == 0)
		{
			return "";
		}
		else
		{
			StringBuilder result = new StringBuilder();
			result.append(values.get(0));
			for (int i = 1; i < values.size(); i++)
			{
				result.append(delimiter).append(values.get(i));
			}
		    
		    return result.toString();
		}
	}
}
