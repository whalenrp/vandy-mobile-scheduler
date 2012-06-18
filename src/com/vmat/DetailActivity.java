package com.vmat;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class DetailActivity extends Activity{
	private EventsDB hasDatabase; 
	private Cursor myInfo;
	private TextView topic;
	private TextView speaker;
	private TextView date;
	private TextView time;
	private TextView food_speaker;
	private TextView description;
	private TextView details;
	

    @Override
	public void onCreate(Bundle savedInstantState){
        super.onCreate(savedInstantState);
        setContentView(R.layout.detail_activity);

		// Initialize Textviews
		topic = (TextView)findViewById(R.id.topic);
		speaker = (TextView)findViewById(R.id.speaker);
		date = (TextView)findViewById(R.id.date);
		time = (TextView)findViewById(R.id.time);
		food_speaker = (TextView)findViewById(R.id.food_speaker);
		description = (TextView)findViewById(R.id.description);

		// Initialize database and cursor
		hasDatabase = new EventsDB(this);
		String[] index = new String[1];
		index[0] = "" + getIntent().getIntExtra("id", -1);
		myInfo = hasDatabase.getReadableDatabase()
				.rawQuery("SELECT * FROM meetings WHERE _id=? LIMIT 1", index);
        myInfo.moveToFirst();

		fillTextViews(myInfo);

		// Clean up
		myInfo.close();
		hasDatabase.close();
	}

	/**
	 * Private helper function used for updating the values of the TextViews
	 * with the values in the cursor. 
	 * This assumes that the cursor points to the currect data.
	 */
	private void fillTextViews(Cursor c){
		// Fill Strings with cursor data
		String topicText, speakerText, dateText, timeText, foodSpeakText, descrText;
		topicText = c.getString(c.getColumnIndex(EventsDB.TOPIC));
		speakerText = c.getString(c.getColumnIndex(EventsDB.SPEAKER_NAME));
		dateText = c.getString(c.getColumnIndex(EventsDB.DATE));
		timeText = "timeText";
		foodSpeakText = "foodSpeakText";
		descrText = c.getString(c.getColumnIndex(EventsDB.DESCRIPTION));

		// Fill TextViews with Strings
		topic.setText(topicText);
		speaker.setText(speakerText);
		date.setText(dateText);
		time.setText(timeText);
		food_speaker.setText(foodSpeakText);
		description.setText(descrText);	
	}
}
