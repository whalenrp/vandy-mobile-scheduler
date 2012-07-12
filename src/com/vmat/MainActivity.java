package com.vmat;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.support.v4.app.FragmentActivity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.support.v4.app.LoaderManager;
import android.app.ProgressDialog;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.support.v4.widget.CursorAdapter;
import android.widget.Toast;

import android.text.format.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import android.util.Log;


public class MainActivity extends SherlockFragmentActivity 
	implements LoaderManager.LoaderCallbacks<Cursor>,
	ActionBar.OnNavigationListener
{
	private EventsCursorAdapter mAdapter = null;
	private ProgressDialog progress = null;
	private EventsDB db = null;
	private String[] mTabs = null;
	private ListView meetings = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		// required for ActionBarSherlock
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		db = new EventsDB(this);

		mAdapter = new EventsCursorAdapter();

		meetings = (ListView)findViewById(R.id.list);
		meetings.setAdapter(mAdapter);

		scheduleAlarms();
		absInit();

		// Launch Detail View on list item click. Pass through the id number from 
		// the local DB.
		meetings.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView parent, View v, int pos, long id){
				Intent intent = new Intent(MainActivity.this, DetailActivity.class);

				Cursor tempCursor = mAdapter.getCursor();
				tempCursor.moveToPosition(pos);
				intent.putExtra("id", tempCursor.getInt(tempCursor.getColumnIndex("_id")));
				startActivity(intent);
			}
		});

		// Prepare the loader to manage cursor to db.
		getSupportLoaderManager().initLoader(0, null, this);// getSupportLoaderManager?
	}

	// Start the AlarmManager if it hasn't already been started. 
	// The AlarmManager will take care of syncing the local database
	// in the background periodically.
	public void scheduleAlarms(){
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(this, SyncReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(
			getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setInexactRepeating(
			AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 
			AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
	}

	// Set up the list navigation using ActionBarSherlock.
	// Make sure that the necessary libraries are linked for this.
	public void absInit(){
		Context ctxt = getSupportActionBar().getThemedContext();
		mTabs = getResources().getStringArray(R.array.tabs);
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				ctxt, R.array.tabs, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args){
		return new DBCursorLoader(this, 
			SyncService.MEETING_URI,
			db, // SQLiteOpenHelper
			EventsDB.TABLE_NAME, // Table name
			null, // columns
			null, // selection
			null, // selectionArgs
			null, // groupBy
			null, // having
			EventsDB.DATE, // orderBy
			null); // limit
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data){
		// Swap the new cursor in. The Framework will take care of closing old cursor.
		mAdapter.swapCursor(data);
		Log.i("MainActivity", "Swapping Cursor");
	}

	public void onLoaderReset(Loader<Cursor> loader){
		mAdapter.swapCursor(null);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu){
//		//MenuInflater inflater = getSupportMenuInflater();
//		//inflater.inflate(R.menu.main_menu, menu);
//		menu.add("Refresh")
//			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item){
//		if (item.getTitle().toString().equals("Refresh")){
//			Intent i = new Intent(this, SyncService.class);
//			i.putExtra("action", SyncService.MEETING_SYNC);
//			startService(i);
//		}
//		return true;
//	}

	// Launch separate activities based on the item selected from the 
	// String array R.array.tabs
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    	
    	// Temporary code to let me see the teams activity:
    	String selectedTab = mTabs[itemPosition];
    	if (selectedTab.equals("Teams"))
    	{
    		Intent i = new Intent(this, TeamsActivity.class);
    		startActivity(i);
    	}
    	
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
    }

    class EventsCursorAdapter extends CursorAdapter{
        EventsCursorAdapter(){
            super(MainActivity.this, null, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent){

            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.rowlayout, parent, false);
            ViewHolder holder = new ViewHolder(row);
            row.setTag(holder);
            return row;
        }

        @Override
        public void bindView(View row, Context context, Cursor cursor){
            ViewHolder holder = (ViewHolder)row.getTag();
            holder.topic.setText(
                    cursor.getString(cursor.getColumnIndex(EventsDB.TOPIC)));
            holder.speaker.setText(
                    cursor.getString(cursor.getColumnIndex(EventsDB.SPEAKER_NAME)));

            // Format the date
            String UTCdate = cursor.getString(cursor.getColumnIndex(EventsDB.DATE));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date parsed = new Date();
            try{
                parsed = format.parse(UTCdate);
            }catch(ParseException e){
                e.printStackTrace();
            }

			// ex - Wednesday, January 10 @ 7:30 PM
            holder.date.setText(DateFormat.format("EEEE, MMMM d '@' h:mm a", parsed));
        }
    }
}
