package com.vmat;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.CursorAdapter;

import android.widget.SimpleCursorAdapter;

import android.text.format.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends SherlockActivity implements
	ActionBar.OnNavigationListener
{
    private ListView listView;
    private Cursor meetingList;
    private EventsDB db;
	private String[] mTabs;
    private JSON_Parse background;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.headerlayout);
        listView = (ListView)findViewById(R.id.meetings);

		// Set up the list navigation using ActionBarSherlock.
		Context ctxt = getSupportActionBar().getThemedContext();
		mTabs = getResources().getStringArray(R.array.tabs);
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				ctxt, R.array.tabs, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);

		// While the background thread updates, fill the list with items from 
		// previous session.
        db = new EventsDB(this);
        meetingList = db.getReadableDatabase().rawQuery("SELECT * FROM " +
                EventsDB.TABLE_NAME + " ORDER BY " + EventsDB.DATE, null);

		// Start background thread that updates the list of objects.
        background = new JSON_Parse(); 
        background.execute();

        listView.setAdapter(new EventsCursorAdapter());

		// Launch Detail View on list item click. Pass through the id number from 
		// the local DB.
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
        	public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                Cursor tempCursor = ((SimpleCursorAdapter)listView.getAdapter()).getCursor();
                tempCursor.moveToPosition(position);
                intent.putExtra("id", tempCursor.getInt(tempCursor.getColumnIndex("_id")));
                startActivity(intent);

            }
          });

        
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        //mSelected.setText("Selected: " + mLocations[itemPosition]);
        return true;
    }

    @Override
    public void onDestroy(){
        super.onPause();
        background.cancel(false);
        meetingList.close();
        db.close();
    }

    class JSON_Parse extends AsyncTask<Void, Void, Cursor>{
        AtomicBoolean isRunning;

        @Override
        protected void onPreExecute(){
            isRunning = new AtomicBoolean(true);
        }

        @Override
        protected Cursor doInBackground(Void... unsused){
            Cursor newCursor = db.refreshDB(isRunning);
            return newCursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            // In the UI thread, make a new cursor with the updated db entries.
            meetingList.close();
            if (isRunning.get() && cursor!= null){
                meetingList = cursor;
                listView.setAdapter(new EventsCursorAdapter());
            }
         }

        @Override
        protected void onCancelled(){
            isRunning.set(false);
        }

    }

    class EventsCursorAdapter extends CursorAdapter{
        EventsCursorAdapter(){
            super(MainActivity.this, meetingList, false);
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

            holder.date.setText(DateFormat.format("EEEE, MMMM d '@' h:mm a", parsed));
        }

    }
}
