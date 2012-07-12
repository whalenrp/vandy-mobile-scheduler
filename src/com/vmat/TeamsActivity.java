package com.vmat;

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

import com.actionbarsherlock.app.ActionBar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TeamsActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener
{
	private static final String TAG = "TeamsActivity";
	
	private ListView listView;
	private String[] tabs;
	private CursorAdapter adapter;
	
	public void onCreate(Bundle savedInstanceState)
	{
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teams);
		
		listView = (ListView)findViewById(R.id.list);
		
		SQLiteDatabase db = new TeamsOpenHelper(this).getReadableDatabase();
		Cursor c = db.query("teams", new String[] { "_id", "name" }, null, null, null, null, null);
		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c, 
										  new String[] {"name"}, new int[] { android.R.id.text1 }, 0);
		listView.setAdapter(adapter);
		
		// Set up Action Bar Sherlock
		Context context = getSupportActionBar().getThemedContext();
		tabs = getResources().getStringArray(R.array.tabs);
		ArrayAdapter<CharSequence> navList = ArrayAdapter.createFromResource(context, 
				R.array.tabs, R.layout.sherlock_spinner_item);
		navList.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(navList, this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		new LoadDataTask().execute();
	}

	// ActionBar.OnNavigationListener Method
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) 
	{
		// Launch intents based on selected tab.
		return false;
	}
	
	class LoadDataTask extends AsyncTask<Void, Void, String>
	{
		private static final String TEAMS_URL = "http://70.138.50.84/apps.json";
		
		@Override
		protected String doInBackground(Void... voids) 
		{
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
		        final HttpParams params = httpClient.getParams();
		        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
		        HttpConnectionParams.setSoTimeout(params, 30 * 1000);
		        ConnManagerParams.setTimeout(params, 30 * 1000);
		        
		        final HttpGet httpget = new HttpGet(TEAMS_URL);
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
			return null;
		}
		
		protected void onPostExecute(String result)
		{
			try 
			{
				JSONArray jsonArray = new JSONArray(result);
				SQLiteDatabase db = new TeamsOpenHelper(TeamsActivity.this).getWritableDatabase();
				for (int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject o = jsonArray.getJSONObject(i);
					
					Cursor c = db.query("teams", new String[] { "server_id" }, "server_id="+o.getInt("id"),
										null, null, null, null);
					
					ContentValues cv = new ContentValues(8);
					cv.put("name", o.getString("name"));
					cv.put("os", o.getString("os"));
					cv.put("team", o.getJSONObject("team").getString("name"));
//					cv.put("tagline", o.getString("tagline"));
					cv.put("description", o.getString("description"));
					cv.put("server_id", o.getInt("id"));
					cv.put("team_id", o.getInt("team_id"));
					cv.put("created_at", o.getString("created_at"));
					cv.put("updated_at", o.getString("updated_at"));
					
					if (c.moveToFirst())
					{
						int updates = db.update("teams", cv, "server_id="+o.getInt("id"), null);
						Log.v(TAG, updates + " row(s) updated.");
					}
					else
					{
						long insertions = db.insert("teams", null, cv);
						Log.v(TAG, insertions + " row(s) inserted.");
					}
					
					c.close();
				}
				
				adapter.swapCursor(db.query("teams", new String[] { "_id", "name" }, null, null, null, null, null));
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
