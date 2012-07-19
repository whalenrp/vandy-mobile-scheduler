package com.vmat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GeneralOpenHelper extends SQLiteOpenHelper 
{
	private static final String TAG = "GeneralOpenHelper";
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "vm_general.db";
	private static final String CREATE_TABLE_TEAMS = 
			"CREATE TABLE teams (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
		  + "name TEXT, team TEXT, os TEXT, tagline TEXT, description TEXT, "
		  + "server_id INTEGER, team_id INTEGER, "
		  + "created_at INTEGER, updated_at INTEGER);";
	
//	private static final String CREATE_TABLE_GITHUB_PROJECTS = 
//			"CREATE TABLE github_projects (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
//		  + "title TEXT, project_id INTEGER);";
	
	private static final String CREATE_TABLE_GITHUB_COMMITS = 
			"CREATE TABLE github_commits (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
		  + "project_id INTEGER, message TEXT, author TEXT, timestamp TEXT, sha TEXT);";

	public GeneralOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		Log.v(TAG, "Creating table 'teams'");
		db.execSQL(CREATE_TABLE_TEAMS);
//		db.execSQL(CREATE_TABLE_GITHUB_PROJECTS);
		db.execSQL(CREATE_TABLE_GITHUB_COMMITS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) 
	{
		Log.v(TAG, "Signal to upgrade, ignoring.");
	}
}
