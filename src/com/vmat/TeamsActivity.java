package com.vmat;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TeamsActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener
{
//	private static final String TAG = "TeamsActivity";
	private static final String[] PROJECTION = new String[] { "_id", "name", "tagline", "app_icon" };
	
	private ListView listView;
	private String[] tabs;
	private CursorAdapter adapter;
	
	private boolean isFirstLoad;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teams);
		
		listView = (ListView)findViewById(R.id.list);
		
		SQLiteDatabase db = new GeneralOpenHelper(this).getReadableDatabase();
		Cursor c = db.query("teams", PROJECTION, null, null, null, null, null);
		isFirstLoad = !(c.getCount() > 0);
		adapter = new TeamsCursorAdapter(this);
		adapter.swapCursor(c);
		db.close();
		
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) 
			{
				Intent i = new Intent(TeamsActivity.this, TeamsDetailActivity.class);
				Cursor c = adapter.getCursor();
				c.moveToPosition(pos);
				i.putExtra("_id", c.getInt(c.getColumnIndex("_id")));
				startActivity(i);
			}
			
		});
		
		// Set up Action Bar Sherlock
		Context context = getSupportActionBar().getThemedContext();
		tabs = getResources().getStringArray(R.array.tabs);
		ArrayAdapter<CharSequence> navList = ArrayAdapter.createFromResource(context, 
				R.array.tabs, R.layout.sherlock_spinner_item);
		navList.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(navList, this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		// Avoid calling back to Meetings (at index 2 of tabs)
		getSupportActionBar().setSelectedNavigationItem(2);
		
		new LoadDataTask().execute();
	}

	// ActionBar.OnNavigationListener Method
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) 
	{
		// Temporary code to let me see the teams activity:
    	String selectedTab = tabs[itemPosition];
    	if (selectedTab.equals("Meetings"))
    	{
    		Intent i = new Intent(this, MainActivity.class);
    		startActivity(i);
    	}
    	else if (selectedTab.equals("News"))
    	{
    		Intent i = new Intent(this, TwitterActivity.class);
    		startActivity(i);
    	}

    	else if (selectedTab.equals("myVM"))
    	{
    		Intent i = new Intent(this, MyVmMain.class);
	    	startActivity(i);
    	}
    	
        return true;
	}
	
	class LoadDataTask extends AsyncTask<Void, Void, Void>
	{
		private static final String TAG = "TeamsActivity$LoadDataTask";
		private static final String TEAMS_URL = "http://vandymobile.com/apps.json";
		
		private ProgressDialog dialog;
		private SQLiteDatabase db = new GeneralOpenHelper(TeamsActivity.this).getWritableDatabase();
		
		protected void onPreExecute()
		{
			if (isFirstLoad)
			{
				dialog = new ProgressDialog(TeamsActivity.this);
				dialog.setMessage("Loading...");
				dialog.show();
			}
		}
		
		@Override
		@SuppressWarnings("unchecked")
		protected Void doInBackground(Void... voids) 
		{
			
			HttpClient httpClient = new DefaultHttpClient();
			try
			{      
		        final HttpGet httpget = new HttpGet(TEAMS_URL);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String result =  httpClient.execute(httpget, responseHandler);
				
				Map<Integer, String> imageLinks = new HashMap<Integer, String>();
				JSONArray jsonArray = new JSONArray(result);
				String entriesToDelete = "server_id NOT IN (-1";
				for (int i = 0; i < jsonArray.length(); i++)
				{
					JSONObject o = jsonArray.getJSONObject(i);
					
					entriesToDelete += "," + o.getInt("id");
					
					Cursor c = db.query("teams", new String[] { "server_id" }, "server_id="+o.getInt("id"),
										null, null, null, null);
					
					ContentValues cv = new ContentValues(8);
					cv.put("name", o.getString("name"));
					cv.put("os", o.getString("os"));
					cv.put("team", o.getJSONObject("team").getString("name"));
					cv.put("tagline", o.getString("tagline"));
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
					
					imageLinks.put(o.getInt("id"), o.getString("image_url"));
					c.close();
				}
				
				entriesToDelete += ")";
				Log.v(TAG, "deleting: " + entriesToDelete);
				db.delete("teams", entriesToDelete, null);
				new FetchImageTask().execute(imageLinks);
			}
			catch (Exception e)
			{
				Log.w(TAG, e.toString());
				e.printStackTrace();
			}
			finally
			{
				httpClient.getConnectionManager().shutdown();
			}
			return null;
		}
		
		protected void onPostExectute(Void v)
		{
			adapter.swapCursor(db.query("teams", PROJECTION, null, null, null, null, null));
			db.close();
			if (isFirstLoad && dialog.isShowing())
			{
				dialog.dismiss();
			}
		}
	}
	
	private class FetchImageTask extends AsyncTask<Map<Integer, String>, Void, Void>
	{
		private static final String TAG = "TeamsActivity$FetchImageTask";
		private SQLiteDatabase db = new GeneralOpenHelper(TeamsActivity.this).getWritableDatabase();
		
		@Override
		protected Void doInBackground(Map<Integer, String>... params) 
		{
			Map<Integer, String> images = params[0];
			Set<Integer> keys = images.keySet();
			for (Integer k : keys)
			{
				try
				{
					URL url = new URL(images.get(k));
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					Bitmap b = BitmapFactory.decodeStream(conn.getInputStream());
					
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					b.compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] byteArray = stream.toByteArray();
					
					ContentValues cv = new ContentValues(1);
					cv.put("app_icon", byteArray);
					int i = db.update("teams", cv, "server_id"+"="+k, null);
					Log.v(TAG, "placed icon for " + i + " item(s)");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			return null;
		}
		
		protected void onPostExecute(Void v)
		{
			adapter.swapCursor(db.query("teams", PROJECTION, null, null, null, null, null));
			db.close();
		}
		
	}
}
