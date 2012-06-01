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
                       double xcoordinate, double ycoordinate)
    {
        ContentValues cv = new ContentValues();
        cv.put("created_at", created_at);
        cv.put("date", date);
        cv.put("day", day);
        cv.put("description", description);
        cv.put("food", food);
        cv.put("speaker", speaker);
        cv.put("speaker_name", speaker_name);
        cv.put("topic", topic);
        cv.put("updated_at", updated_at);
        cv.put("xcoordinate", xcoordinate);
        cv.put("ycoordinate", ycoordinate);
        getWritableDatabase().insert("meetings", null, cv);

    }
}
