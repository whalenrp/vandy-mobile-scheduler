package com.vmat;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created with IntelliJ IDEA.
 * User: richard
 * Date: 6/1/12
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventsDB extends SQLiteOpenHelper {
    private static final  String DATABASE_NAME="mobile_meetups.db";
    private static final int SCHEMA_VERSION=1;
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
        db.execSQL("CREATE TABLE meetings (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "created_at TEXT, date TEXT, day TEXT, description TEXT," +
                "food INTEGER, id INTEGER, speaker INTEGER, speaker_name TEXT," +
                "topic TEXT, updated_at TEXT, xcoordinate REAL, ycoordinate REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

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
}
