package com.vmat;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog;
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
import android.widget.Toast;

import android.text.format.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends SherlockActivity implements
	ActionBar.OnNavigationListener
{
    private ListView listView = null;
	private ProgressDialog progress = null;
    private Cursor meetingList = null;
    private EventsDB db = null;
	private String[] mTabs = null;
    private DBsync background = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		// required for ActionBarSherlock
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

		// Set up the list navigation using ActionBarSherlock.
		// Make sure that the necessary libraries are linked for this.
		Context ctxt = getSupportActionBar().getThemedContext();
		mTabs = getResources().getStringArray(R.array.tabs);
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				ctxt, R.array.tabs, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		// While the background thread updates, fill the list with items from 
		// previous session.
        db = new EventsDB(this);
        meetingList = db.getReadableDatabase().rawQuery("SELECT * FROM " +
                EventsDB.TABLE_NAME + " ORDER BY " + EventsDB.DATE, null);

		// Start background thread that updates the list of objects.
		// 	First check to see if the screen has rotated and the update
		//  has already been started. 
		background = (DBsync)getLastNonConfigurationInstance();
		if (background == null){
			progress = ProgressDialog.show(this, "", "Loading...");
			background = new DBsync(this); 
			background.execute();
		}else{
			background.attach(this);
			if (!background.finished()){
				// set indefinite progress bar
				progress = ProgressDialog.show(this, "", "Loading...");
			}
		}

        listView = (ListView)findViewById(R.id.meetings);
        listView.setAdapter(new EventsCursorAdapter());

		// Launch Detail View on list item click. Pass through the id number from 
		// the local DB.
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
        	public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                Cursor tempCursor = ((EventsCursorAdapter)listView.getAdapter()).getCursor();
                tempCursor.moveToPosition(position);
                intent.putExtra("id", tempCursor.getInt(tempCursor.getColumnIndex("_id")));
                startActivity(intent);

            }
          });
    }

	// Detach the static ASyncTask from this activity so that it may 
	// be reattached to the new one after rotation.
	@Override
	public Object onRetainNonConfigurationInstance(){
		background.detach();
		return background;
	}

	// Launch separate activities based on the item selected from the 
	// String array R.array.tabs
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        meetingList.close(); 
        db.close();
    }

	/**
	 * Allows the static DBsync to access an instance of EventsCursorAdapter.
	 */
	public EventsCursorAdapter getEventsAdapter(){
		return new EventsCursorAdapter();
	}

    static class DBsync extends AsyncTask<Void, Void, Cursor>{
		MainActivity activity = null;
		boolean finished = false;
		
		DBsync(MainActivity activity){
			attach(activity);
		}

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected Cursor doInBackground(Void... unsused){
			Cursor newCursor = null;
			if (activity != null)
				newCursor = activity.db.refreshDB();
            return newCursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            // In the UI thread, make a new cursor with the updated db entries.
			if (activity != null){
				if (cursor!= null){
					activity.meetingList.close();
					activity.meetingList = cursor;
					activity.listView.setAdapter(
						activity.getEventsAdapter());
				}
				activity.progress.dismiss();

				// make a dialog here about no internet connection if cursor == null
				if (cursor == null){
					activity.showAlert();
				}
			}
			finished = true;
         }

		void detach(){
			activity = null;
		}

		void attach(MainActivity activity){
			this.activity = activity;
		}

		boolean finished(){
			return finished;
		}
    }

	public void showAlert(){
		new AlertDialog.Builder(this)
			.setMessage("Woops! A connection to our server could"+
					" not be established! Please reconnect to the internet to receive"+
					" the most recent updates. In the meantime, your most recent list"+
					" of meetings will be shown.")
			.setNeutralButton("Okay", null)
			.show();
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

			// ex - Wednesday, January 10 @ 7:30 PM
            holder.date.setText(DateFormat.format("EEEE, MMMM d '@' h:mm a", parsed));
        }

    }
}
