package com.vmat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Richard Whalen
 * This EventsDB class will handle a local DB that has been synced with a web page containing raw JSON data.
 * Given the latest timestamp, it will refresh the database if out of sync with the server.
 */
public class EventsDB extends SQLiteOpenHelper {
    private static final  String DATABASE_NAME="mobile_meetups.db";
    public static final String TABLE_NAME = "meetings";
    private static final int SCHEMA_VERSION=1;
    private static final String WEB_ADDRESS = "http://70.138.50.84/meetings.json";
    static final String CREATED_AT = "created_at";
    static final String DATE = "date";
    static final String DAY = "day";
    static final String DESCRIPTION = "description";
    static final String FOOD = "food";
    static final String SPEAKER = "speaker";
    static final String SPEAKER_NAME = "speaker_name";
    static final String TOPIC = "topic";
    static final String UPDATED_AT = "updated_at";
    static final String XCOORD = "xcoordinate";
    static final String YCOORD = "ycoordinate";
    static final String ID = "id";

    public EventsDB(Context context){
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "created_at TEXT, date TEXT, day TEXT, description TEXT," +
                "food INTEGER, id INTEGER, speaker INTEGER, speaker_name TEXT," +
                "topic TEXT, updated_at TEXT, xcoordinate REAL, ycoordinate REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(db);
    }

    public void insert(String created_at, String date, String day,
                       String description, boolean food, boolean speaker,
                       String speaker_name, String topic, String updated_at,
                       double xcoordinate, double ycoordinate, int id)
    {
        ContentValues cv = new ContentValues();
        cv.put(CREATED_AT, created_at);
        cv.put(DATE, date);
        cv.put(DAY, day);
        cv.put(DESCRIPTION, description);
        cv.put(FOOD, food);
        cv.put(SPEAKER, speaker);
        cv.put(SPEAKER_NAME, speaker_name);
        cv.put(TOPIC, topic);
        cv.put(UPDATED_AT, updated_at);
        cv.put(XCOORD, xcoordinate);
        cv.put(YCOORD, ycoordinate);
        cv.put(ID, id);
        getWritableDatabase().insert("meetings", null, cv);

    }

    public Cursor refreshDB(){

        // Connect to webserver, Retrieve JSON objects in JSONArray.
        String jsonString = "";
        try{
            URL url = new URL(WEB_ADDRESS);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            try{
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                jsonString = convertStreamToString(in);
            }finally{
                urlConnection.disconnect();
            }
        }catch(MalformedURLException e){
            // Make a toast saying the site has moved?
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
		if (jsonString.length() == 0)
			return null;
        JSONArray jsonArray = null;
        try{
            jsonArray = new JSONArray(jsonString);

        }catch(JSONException e){
            // Do something more intelligent here..
            // This is hit when the connection is established, but there is no data
            // on the page.
            e.printStackTrace();
        }

		getWritableDatabase().delete(TABLE_NAME, null, null);
		try{
			for (int i=0; i< jsonArray.length(); ++i){
				JSONObject object =  jsonArray.getJSONObject(i);

				// Compare latest updated Timestamp to latest timestamp in db.
				// if different, delete local db and build table again.
				insert(object.getString(CREATED_AT), object.getString(DATE),
						object.getString(DAY), object.getString(DESCRIPTION),
						object.getBoolean(FOOD), object.getBoolean(SPEAKER),
						object.getString(SPEAKER_NAME), object.getString(TOPIC),
						object.getString(UPDATED_AT), object.getDouble(XCOORD),
						object.getDouble(YCOORD), object.getInt(ID));

			}
		}catch(JSONException e){
			e.printStackTrace();
		}
        // Format the date
//        String UTCdate = cursor.getString(cursor.getColumnIndex(EventsDB.DATE));
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//        Date parsed = new Date();
//        try{
//            parsed = format.parse(UTCdate);
//        }catch(ParseException e){
//            e.printStackTrace();
//        }
		return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + DATE, null);
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
