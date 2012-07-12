package com.vmat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TeamsOpenHelper extends SQLiteOpenHelper 
{
	private static final String TAG = "TeamsOpenHelper";
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "vm_teams.db";
	private static final String CREATE_TABLE = "CREATE TABLE teams (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
											 + "name TEXT, team TEXT, os TEXT, tagline TEXT, description TEXT, "
											 + "server_id INTEGER, team_id INTEGER, "
											 + "created_at INTEGER, updated_at INTEGER)";

	public TeamsOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		Log.v(TAG, "Creating table 'teams'");
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) 
	{
		Log.v(TAG, "Signal to upgrade, ignoring.");
	}

}
