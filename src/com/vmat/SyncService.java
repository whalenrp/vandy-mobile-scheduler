package com.vmat;

import android.app.IntentService;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.lang.String;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class SyncService extends IntentService{
	static final int TWITTER_SYNC = 0;
	static final int MEETING_SYNC = 1;
	static final String TWITTER_URI = "content://com.vmat/twitter/0";
	static final String MEETING_URI = "content://com.vmat/meeting/0";
	private Uri twitterURI;
	private Uri meetingURI;
	private URL url = null;
	private HttpURLConnection conn = null;
	private String meetingString = "http://70.138.50.84/meetings.json";
	private String twitterString = "http://api.twitter.com/1/statuses/user_timeline.json?screen_name=VandyMobile&include_rts=1";

	public SyncService(){
		super("SyncService");
		twitterURI = Uri.parse(TWITTER_URI);
		meetingURI = Uri.parse(MEETING_URI);
	}

	@Override
	public void onHandleIntent(Intent i){
		int action = i.getIntExtra("action", -1);
		Log.i("SyncService", "Retrieved intent");
		switch (action){
			case TWITTER_SYNC:
				try{
					url = new URL(twitterString);
					conn = (HttpURLConnection)url.openConnection();
					syncTwitterDB();
					conn.disconnect();
				}catch(MalformedURLException e){
					Log.w("SyncService", "Twitter URL is no longer valid.");
					e.printStackTrace();
				}catch(IOException e){
					Log.w("SyncService", "Twitter connection could not be established.");
					e.printStackTrace();
				}
				break;
			case MEETING_SYNC:
				Log.i("SyncService", "Correctly entered MEETING_SYNC");
				try{
					url = new URL(meetingString);
					conn = (HttpURLConnection)url.openConnection();
					syncMeetingDB();
					conn.disconnect();
				}catch(MalformedURLException e){
					Log.w("SyncService", "Meetings URL is no longer valid.");
					e.printStackTrace();
				}catch(IOException e){
					Log.w("SyncService", "Meetings connection could not be established.");
					e.printStackTrace();
				}
				break;

			default:;
				// invalid request
		}
	}

	private void syncTwitterDB(){

	}

	private void syncMeetingDB(){

		// Read in response as one string
        String jsonString = "";
		InputStream in = null;
		try{
			in = new BufferedInputStream(conn.getInputStream());
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
		jsonString = convertStreamToString(in);

		if (jsonString.length() == 0) return;

        JSONArray jsonArray = null;
        try{
            jsonArray = new JSONArray(jsonString);
        }catch(JSONException e){
            e.printStackTrace();
        }

		EventsDB eventsdb = new EventsDB(this);
		//db.getWritableDatabase().delete(EventsDB.TABLE_NAME, null, null);
		SQLiteDatabase  mDB = eventsdb.getWritableDatabase();
		String[] columns = {EventsDB.ID, EventsDB.UPDATED_AT, EventsDB.CREATED_AT};
		try{
			String deleteEntries = EventsDB.ID + " NOT IN (-1";
			// Update/insert entries
			for (int i=0; i< jsonArray.length(); ++i){
				JSONObject object =  jsonArray.getJSONObject(i);
				
				deleteEntries += "," + object.getInt(EventsDB.ID);

				Cursor mCursor = mDB.query(EventsDB.TABLE_NAME, columns, 
					EventsDB.ID + "=?",
					new String[]{object.getString(EventsDB.ID)},
					null, null, null, "1");

				// The entry does not exist in the local database, so insert it.
				if (mCursor.getCount() == 0){ 
					eventsdb.insert(object.getString(EventsDB.CREATED_AT), 
						object.getString(EventsDB.DATE),
						object.getString(EventsDB.DAY), 
						object.getString(EventsDB.DESCRIPTION),
						object.getBoolean(EventsDB.FOOD), 
						object.getBoolean(EventsDB.SPEAKER),
						object.getString(EventsDB.SPEAKER_NAME), 
						object.getString(EventsDB.TOPIC),
						object.getString(EventsDB.UPDATED_AT), 
						object.getDouble(EventsDB.XCOORD),
						object.getDouble(EventsDB.YCOORD), 
						object.getInt(EventsDB.ID));
					Log.i("SyncService", "Inserted new entry");
				}
				// The entry does exist
				else{
					mCursor.moveToFirst();
					// check to see if it has been updated.
					String localTime = mCursor.getString(
						mCursor.getColumnIndex(EventsDB.UPDATED_AT));
					String serverTime = object.getString(EventsDB.UPDATED_AT);
					if (!localTime.equals(serverTime)){
						eventsdb.update(
							EventsDB.ID + "=?",
							new String[]{object.getString(EventsDB.ID)},
							object.getString(EventsDB.CREATED_AT), 
							object.getString(EventsDB.DATE),
							object.getString(EventsDB.DAY), 
							object.getString(EventsDB.DESCRIPTION),
							object.getBoolean(EventsDB.FOOD), 
							object.getBoolean(EventsDB.SPEAKER),
							object.getString(EventsDB.SPEAKER_NAME), 
							object.getString(EventsDB.TOPIC),
							object.getString(EventsDB.UPDATED_AT), 
							object.getDouble(EventsDB.XCOORD),
							object.getDouble(EventsDB.YCOORD), 
							object.getInt(EventsDB.ID),
							object.getInt("_id"),
							(1==mCursor.getInt(mCursor.getColumnIndex(EventsDB.ALARM_ACTIVE))));
						Log.i("SyncService", "Updated existing entry");
					}
				}
				mCursor.close();
			}
			deleteEntries += ")";

			// now delete entries that werent found in the JSON array
			int numDeleted = mDB.delete(EventsDB.TABLE_NAME, deleteEntries, null);
			Log.i("SyncService", "Deleted " + numDeleted + " entries from meetings");
		}catch(JSONException e){
			e.printStackTrace();
		}
		getContentResolver().notifyChange(meetingURI, null);
		eventsdb.close();
		Log.i("SyncService", "Database Refreshed");
    }

    // Helper function for reading input stream
    // retrieved from http://stackoverflow.com/a/5445161/793208
    private String convertStreamToString(InputStream is){
        try{
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        }catch(NoSuchElementException e){
            return "";
        }
    }
}
