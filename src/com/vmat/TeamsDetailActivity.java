package com.vmat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.TextView;

public class TeamsDetailActivity extends Activity
{
	ImageView appIcon;
	TextView textTitle;
	TextView textTagline;
	TextView textOs;
	TextView textTeam;
	TextView textDescription;
	
	public void onCreate(Bundle savedInstanceState)
	{
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teams_detail_activity);
        
        Drawable icon = getResources().getDrawable(R.drawable.ic_jog_dial_answer);
        appIcon = (ImageView)findViewById(R.id.img_app_icon);
        appIcon.setImageDrawable(icon);
        textTitle = (TextView)findViewById(R.id.text_title);
    	textTagline = (TextView)findViewById(R.id.text_tagline);
    	textOs = (TextView)findViewById(R.id.text_os);
    	textTeam = (TextView)findViewById(R.id.text_team);
    	textDescription = (TextView)findViewById(R.id.text_description);
    	
    	int rowId = getIntent().getIntExtra("_id", -1);
    	SQLiteDatabase db = new TeamsOpenHelper(this).getReadableDatabase();
    	// Project could be more specific. Right now gets all columns.
    	Cursor c = db.query("teams", null, "_id="+rowId, null, null, null, null);
    	if (c.moveToFirst())
    	{
    		textTitle.setText(c.getString(c.getColumnIndex("name")));
//        	textTagline.setText(c.getString(c.getColumnIndex("tagline")));
        	textOs.setText(c.getString(c.getColumnIndex("os")));
        	textTeam.setText(c.getString(c.getColumnIndex("team")));
        	textDescription.setText(c.getString(c.getColumnIndex("description")));
    	}
    	c.close();
	}
}
