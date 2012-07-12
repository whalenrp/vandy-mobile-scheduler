package com.vmat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Richard Whalen
 * This EventsDB class will handle a local DB that has been synced with a web page containing raw JSON data.
 * Given the latest timestamp, it will refresh the database if out of sync with the server.
 */
public class EventsDB extends SQLiteOpenHelper {
	private Context context;
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
    static final String ALARM_ACTIVE = "alarmActive";

    public static final String DEFAULT_ORDER = DATE;

    public EventsDB(Context context){
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
		this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "created_at TEXT, date TEXT, day TEXT, description TEXT," +
                "food INTEGER, id INTEGER, speaker INTEGER, speaker_name TEXT," +
                "topic TEXT, updated_at TEXT, xcoordinate REAL, ycoordinate REAL, " +
				"alarmActive INTEGER);");
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
		cv.put(ALARM_ACTIVE, 0);
        getWritableDatabase().insert("meetings", null, cv);

    }

    public void update(String where, String[] whereArgs, String created_at, String date,
					   String day, String description, boolean food, boolean speaker,
                       String speaker_name, String topic, String updated_at,
                       double xcoordinate, double ycoordinate, int id, int _id, 
					   boolean alarmIsSet)
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

		if (alarmIsSet){
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);	
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date parsedDate = new Date();
			try{
				parsedDate = format.parse(date);
			}catch(ParseException e){
				e.printStackTrace();
			}
			Calendar alarmCalendar = Calendar.getInstance();
			alarmCalendar.setTime(parsedDate);

			Intent intent = new Intent(context, DetailActivity.class);
			intent.putExtra("alarmReceived", 1)
				.putExtra("id", _id);
			PendingIntent pi = PendingIntent.getActivity(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pi);

		}
        getWritableDatabase().update(TABLE_NAME, cv, where, whereArgs);
    }

	public int updateAlarm(int id, boolean alarmActive){
		ContentValues cv = new ContentValues();
		cv.put(EventsDB.ALARM_ACTIVE, alarmActive);
		return getWritableDatabase().update(
			TABLE_NAME, 
			cv, "_id=?", 
			new String[]{"" + id});
	}
}
