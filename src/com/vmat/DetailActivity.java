package com.vmat;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MyLocationOverlay;
import android.content.Intent;
import android.content.ContentValues;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.graphics.drawable.Drawable;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import android.net.Uri;

import java.text.SimpleDateFormat;
import android.text.format.DateFormat;
import java.util.Date;
import java.util.Calendar;
import java.text.ParseException;

/**
 * DetailActivity is called when an item from the main meetings viewer
 * is selected. An intent is passed in to DetailActivity containing the 
 * _id field from the corresponding item in the database with which to 
 * populate the activity. 
 */
public class DetailActivity extends SherlockMapActivity{

	private boolean alarmActive; 
	private EventsDB hasDatabase; 
	private Cursor myInfo;
	private TextView topic;
	private TextView speaker;
	private TextView date;
	private TextView time;
	private TextView food_speaker;
	private TextView description;
	private TextView details;
	private MapView mapthumb;
	private GeoPoint center;
	private MyLocationOverlay me = null;
	

    @Override
	public void onCreate(Bundle savedInstantState){
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstantState);
        setContentView(R.layout.detail_activity);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Initialize Textviews
		topic = (TextView)findViewById(R.id.topic);
		speaker = (TextView)findViewById(R.id.speaker);
		date = (TextView)findViewById(R.id.date);
		time = (TextView)findViewById(R.id.time);
		food_speaker = (TextView)findViewById(R.id.food_speaker);
		description = (TextView)findViewById(R.id.description);
		mapthumb = (MapView)findViewById(R.id.mapthumb);

		// Initialize database and cursor
		startManagingData();

		alarmActive = (1==myInfo.getInt(myInfo.getColumnIndex(EventsDB.ALARM_ACTIVE)));

		// Find center geopoint and create map
		center = getCenter();
		initMap();

		fillTextViews(myInfo);

		// If this activity is started in response to the alarm, 
		// show a dialog box saying so.
		if (getIntent().getIntExtra("alarmReceived", 0) != 0){
			// make dialog for receiving alarm
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		me.enableMyLocation();
		if (myInfo.isClosed()){
			// Initialize database and cursor
			startManagingData();
		}
	}

	@Override
	public void onPause(){
		super.onPause();
		// Clean up
		me.disableMyLocation();
		myInfo.close();
		hasDatabase.close();
	}



	@Override
	protected boolean isRouteDisplayed(){
		return false;
	}

	private void startManagingData(){
		hasDatabase = new EventsDB(this);
		String[] index = new String[1];
		index[0] = "" + getIntent().getIntExtra("id", -1);
		myInfo = hasDatabase.getReadableDatabase()
				.rawQuery("SELECT * FROM meetings WHERE _id=? LIMIT 1", index);
        myInfo.moveToFirst();

	}

	///////////////////////////////////////
	// Button click listeners
	///////////////////////////////////////

	/**
	 * Called when the "Set Alarm" button is selected
	 */
	public void alarmSet(View view){
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);	

		// Reads in the stored date, parses it, and stores it in a Calendar object
		// for the alarm service.
		String UTCdate = myInfo.getString(myInfo.getColumnIndex(EventsDB.DATE));
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date parsedDate = new Date();
		try{
			parsedDate = format.parse(UTCdate);
		}catch(ParseException e){
			e.printStackTrace();
		}
		Calendar alarmCalendar = Calendar.getInstance();
		alarmCalendar.setTime(parsedDate);

		Intent intent = new Intent(this, DetailActivity.class);
		PendingIntent pi = PendingIntent.getActivity(
			getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (!alarmActive){
			alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pi);
			Button btn = (Button)findViewById(R.id.alarm_button);
			btn.setText("Cancel Alarm");
			alarmActive = true;
		
		}
		else{
			pi.cancel();
			Button btn = (Button)findViewById(R.id.alarm_button);
			btn.setText("Set Alarm");
			alarmActive = false;
		}

		// Set the database ALARM_ACTIVE value to 1 if alarm is set, 
		// otherwise, set to 0.
		EventsDB db = new EventsDB(this);
		int numChanged = db.updateAlarm(getIntent().getIntExtra("id", -1), alarmActive);
		Log.i("DetailActivity", "Number of alarms changed: " + numChanged);
	}

	/**
	 * Called when the user opens a map for directions.
	 * Since google maps should be optional, this will attempt to open 
	 * a browser and populate the address field in Maps with the destination
	 * if not available.
	 */
	public void openMap(View view){
		String url = new String("http://maps.google.com/maps?daddr=" +
			center.getLatitudeE6()/1000000.0 + "," + center.getLongitudeE6()/1000000.0);
		Log.i("DetailActivity", url);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
			Uri.parse(url));
		startActivity(intent);
	}

	///////////////////////////////////////
	// Helper functions and classes
	///////////////////////////////////////

	/**
	 * Private helper function used for updating the values of the TextViews
	 * with the values in the cursor. 
	 * This assumes that the cursor points to the currect data.
	 */
	private void fillTextViews(Cursor c){
		// Fill Strings with cursor data
		String topicText, speakerText, dateText, descrText;
		boolean isFood;
		topicText = c.getString(c.getColumnIndex(EventsDB.TOPIC));
		speakerText = c.getString(c.getColumnIndex(EventsDB.SPEAKER_NAME));
		dateText = c.getString(c.getColumnIndex(EventsDB.DATE));
		isFood = (1==c.getInt(c.getColumnIndex(EventsDB.FOOD)));
		descrText = c.getString(c.getColumnIndex(EventsDB.DESCRIPTION));

		// Fill TextViews with Strings
		topic.setText(topicText);
		speaker.setText(speakerText);
		description.setText(descrText);	
		if (isFood)
			food_speaker.setText("There's food too!");

		// Format the date
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date parsed = new Date();
		try{
			parsed = format.parse(dateText);
		}catch(ParseException e){
			e.printStackTrace();
		}

		// ex - Wednesday, January 10 @ 7:30 PM
		date.setText(DateFormat.format("EEEE, MMMM d", parsed));
		time.setText(DateFormat.format("h:mm a", parsed));

		if (alarmActive){
			Button btn = (Button)findViewById(R.id.alarm_button);
			btn.setText("Cancel Alarm");
		}
	}

	/**
	 * Initializes the map with the destination and the current location overlays.
	 */
	private void initMap(){
		mapthumb.getController().setCenter(center);
		mapthumb.getController().setZoom(16);
		//add destination marker
		Drawable marker = getResources().getDrawable(R.drawable.pushpin);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		mapthumb.getOverlays().add(new SiteOverlay(marker));
		// Add location marker
		me = new MyLocationOverlay(this, mapthumb);
		mapthumb.getOverlays().add(me);

	}

	private GeoPoint getCenter(){
		double latitude, longitude;
		latitude = myInfo.getDouble(myInfo.getColumnIndex(EventsDB.XCOORD));
		longitude = myInfo.getDouble(myInfo.getColumnIndex(EventsDB.YCOORD));
		return new GeoPoint((int)(latitude*1000000.0), (int)(longitude*1000000.0));
	}


	/**
	 * Contains the logic for displaying an overlay for the destination
	 * and the current position on the minimap
	 */
	private class SiteOverlay extends ItemizedOverlay<OverlayItem>{
		private OverlayItem location;

		public SiteOverlay(Drawable marker){
			super(marker);
			boundCenterBottom(marker);
			location = new OverlayItem(center, "Destination", 
				topic.getText().toString());
			populate();
		}

		@Override
		public int size(){
			return 1;
		}

		@Override
		protected OverlayItem createItem(int index){
			return location;
		}
	}
}
