package com.vmat;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class TeamsDetailActivity extends SherlockActivity
{
	private ImageView appIcon;
	private TextView textTitle;
	private TextView textTagline;
	private TextView textOs;
	private TextView textTeam;
	private TextView textDescription;
	private ImageButton googlePlayButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teams_detail_activity);
        
        appIcon = (ImageView)findViewById(R.id.img_app_icon);
        textTitle = (TextView)findViewById(R.id.text_title);
    	textTagline = (TextView)findViewById(R.id.text_tagline);
    	textOs = (TextView)findViewById(R.id.text_os);
    	textTeam = (TextView)findViewById(R.id.text_team);
    	textDescription = (TextView)findViewById(R.id.text_description);
    	googlePlayButton = (ImageButton)findViewById(R.id.goog_play_button);
    	
    	googlePlayButton.setOnClickListener(new View.OnClickListener() 
    	{
			public void onClick(View v) 
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("https://play.google.com/store/apps"));
				startActivity(intent);
			}
		});
    	
    	int rowId = getIntent().getIntExtra("_id", -1);
    	SQLiteDatabase db = new GeneralOpenHelper(this).getReadableDatabase();
    	// Project could be more specific. Right now gets all columns.
    	Cursor c = db.query("teams", null, "_id="+rowId, null, null, null, null);
    	if (c.moveToFirst())
    	{
    		textTitle.setText(c.getString(c.getColumnIndex("name")));
        	textTagline.setText(c.getString(c.getColumnIndex("tagline")));
        	textOs.setText(c.getString(c.getColumnIndex("os")));
        	textTeam.setText(c.getString(c.getColumnIndex("team")));
        	textDescription.setText(c.getString(c.getColumnIndex("description")));
      
        	byte[] imageData = c.getBlob(c.getColumnIndex("app_icon"));
    		if (imageData != null)
    		{
    			Bitmap b = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
    			appIcon.setImageBitmap(b);
    		}
    	}
    	c.close();
    	db.close();
	}
}
