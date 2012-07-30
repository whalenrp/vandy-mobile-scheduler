package com.vmat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TwitterActivity extends SherlockFragmentActivity  
							 implements LoaderManager.LoaderCallbacks<Cursor>, ActionBar.OnNavigationListener
{
	private static final String TAG = "TwitterActivity";
	private static final String[] PROJECTION = new String[] {TwitterDB.ID, TwitterDB.TEXT};
    private CursorAdapter tAdapter = null;
    private TwitterDB db = null;
    private String[] mTabs = null;
    private ListView tweets = null;
    
    @Override
	public void onCreate(Bundle savedInstanceState)
	{
	    setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.twitter);
	    
	    db = new TwitterDB(this);
	    
	    SQLiteDatabase sqliteDB =  db.getReadableDatabase();
	    Cursor c = sqliteDB.query(TwitterDB.TABLE_NAME, PROJECTION, null, null, null, null, null);
	    
	    tweets = (ListView) findViewById(R.id.listtwitter);
	    tAdapter = new TwitterCursorAdapter(this);
	    tAdapter.swapCursor(c);
	    
	    tweets.setAdapter(tAdapter);
	    	    
	    absInit();
	   
	    Intent intent = new Intent(this, SyncService.class);
		intent.putExtra("action", SyncService.TWITTER_SYNC);
		this.startService(intent);
	
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
		// Avoids switching back to Meetings
		getSupportActionBar().setSelectedNavigationItem(1);
	}    

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		Log.i("TwitterActivity", "Starting Load in background");
		return new DBCursorLoader(this, 
				SyncService.TWITTER_URI,
				db, // SQLiteOpenHelper
				TwitterDB.TABLE_NAME, // Table name
				PROJECTION, // columns
				null, // selection
				null, // selectionArgs
				null, // groupBy
				null, // having
				TwitterDB.DEFAULT_ORDER, // orderBy
				null); // limit
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor curCursor) {
		Log.i("TwitterTwoAcitivity", "cursor count: "+curCursor.getCount());
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
    	else if (selectedTab.equals("myVM"))
    	{
    		Intent i = new Intent(this, MyVmMain.class);
	    	startActivity(i);
    	}
    	
        return true;
	}	
}
    
    
