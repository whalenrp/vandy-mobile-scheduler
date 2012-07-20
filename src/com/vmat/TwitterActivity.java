package com.vmat;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TwitterActivity extends SherlockFragmentActivity  implements LoaderManager.LoaderCallbacks<Cursor>,
ActionBar.OnNavigationListener
{
    private SimpleCursorAdapter tAdapter = null;
    private TwitterDB db = null;
    private String[] mTabs = null;
    private ListView tweets = null;
    
    @Override
	public void onCreate(Bundle savedInstanceState)
	{
	    setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.twitter);
	    
	    Log.i("TwitterTwoActivity", "Reached this Point: 1");
	    db = new TwitterDB(this);
	    String[] columns = new String[1];
	    columns[0] = TwitterDB.TEXT;
	    Log.i("TwitterTwoActivity", "Reached this Point: 2");
	    int [] to = new int[1];
	    TextView myView = null;
	    to[0] = R.id.text;
	  //  Cursor cursor = db.getReadableDatabase().query(TwitterDB.TABLE_NAME, columns, null, null, null, null, TwitterDB.DEFAULT_ORDER);
	    tAdapter = new SimpleCursorAdapter(this, R.layout.twitterrowlayout, null, columns, to, 0); 
	    Log.i("TwitterTwoActivity", "Reached this Point: 3");
	    tweets = (ListView) findViewById(R.id.listtwitter);
	    tweets.setAdapter(tAdapter);
	    
	    absInit();
	    
	    Log.i("TwitterTwoActivity", "Reached this Point: 4");

	   
	    Intent intent = new Intent(this, SyncService.class);
		intent.putExtra("action", SyncService.TWITTER_SYNC);
		this.startService(intent);
		

	    Log.i("TwitterTwoActivity", "Reached this Point: 5");
	    getSupportLoaderManager().initLoader(0, null, (android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>) this);
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
		getSupportActionBar().setListNavigationCallbacks(list, (OnNavigationListener) this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}    

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		Log.i("TwitterTwoAcitivity TwitterTwoActivity TwitterTwoActivity", "Starting Load in background");
		return new DBCursorLoader(this, 
				SyncService.TWITTER_URI,
				db, // SQLiteOpenHelper
				TwitterDB.TABLE_NAME, // Table name
				new String[] {TwitterDB.ID, TwitterDB.TEXT}, // columns
				null, // selection
				null, // selectionArgs
				null, // groupBy
				null, // having
				TwitterDB.DEFAULT_ORDER, // orderBy
				null); // limit
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor curCursor) {
		Log.i("TwitterTwoAcitivity", "LOOK AT ME NOW");
		Log.i("TwitterTwoAcitivity", ""+curCursor.getCount());
		tAdapter.swapCursor(curCursor);
	//	tAdapter.bindView(findViewById(R.id.text), this, curCursor);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		tAdapter.swapCursor(null);		
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    	// Temporary code to let me see the teams activity:
    	String selectedTab = mTabs[itemPosition];
    	if (selectedTab.equals("Teams"))
    	{
    		Intent i = new Intent(this, TeamsActivity.class);
    		startActivity(i);
    	}
    	else if (selectedTab.equals("Meetings"))
    	{
    		Intent i = new Intent(this, MainActivity.class);
    		startActivity(i);
    	}
    	else if (selectedTab.equals("News"))
    	{
	    //Intent i = new Intent(this, NewsActivity.class);
	    //	startActivity(i);
    	}

    	else if (selectedTab.equals("myVM"))
    	{
	    //Intent i = new Intent(this, myVMActivity.class);
	    //	startActivity(i);
    	}
    	
        return true;
	}	
}
    
    
