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
import android.widget.CursorAdapter;
import android.widget.Toast;

import android.text.format.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import android.util.Log;


public class TwitterActivity extends SherlockFragmentActivity 
	implements LoaderManager.LoaderCallbacks<Cursor>,
	ActionBar.OnNavigationListener
{
    private TwitterCursorAdapter mAdapter = null;
    private TwitterDB db = null;
    private String[] mTabs = null;
    private ListView tweets = null;
    
    @Override
	public void onCreate(Bundle savedInstanceState)
	{
	    
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    db = new TwitterDB(this);
	    









    class TwitterCursorAdapter extends CursorAdapter{
        EventsCursorAdapter(){
            super(TwitterActivity.this, null, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent){

            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.twitterrowlayout, parent, false);
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
