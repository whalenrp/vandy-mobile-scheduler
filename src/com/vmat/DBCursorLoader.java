package com.vmat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader.ForceLoadContentObserver;

/**
 * Custom Cursor Loader that will sync with a database rather than 
 * a ContentProvider. 
 */
public class DBCursorLoader extends AsyncTaskLoader<Cursor>{
	final ForceLoadContentObserver mObserver;
	private Cursor mCursor = null;
	private SQLiteOpenHelper db = null;
	Uri uri;
	String table;
	String[] columns;
	String selection;
	String[] selectionArgs;
	String groupBy;
	String having;
	String orderBy;
	String limit;

	public DBCursorLoader(Context context, String uri, SQLiteOpenHelper db, String table, 
		String[] columns, String selection, String[] selectionArgs, 
		String groupBy, String having, String orderBy, String limit)
	{
		super(context);
		mObserver = new ForceLoadContentObserver();
		this.uri = Uri.parse(uri);
		this.db = db;
		this.table = table;
		this.columns = columns;
		this.selection = selection;
		this.selectionArgs = selectionArgs;
		this.groupBy = groupBy;
		this.having = having;
		this.orderBy = orderBy;
		this.limit = limit;
	}


	/**
	 * Called on background thread. Queries the db for a new 
	 * cursor that will be returned by the loader.
	 */
	@Override
	public Cursor loadInBackground(){

        Cursor cursor = db.getReadableDatabase().query(table, columns, 
			selection, selectionArgs, groupBy, having, orderBy, limit);
		if (cursor != null){
			// Not sure why this is necessary, but google does it
			// in their CursorLoader class. "Ensure the cursor
			// window is filled"
			cursor.getCount(); 
			// register observer to get notifications when db is changed
			cursor.registerContentObserver(mObserver);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return cursor;
	}

	/* Runs on the UI thread */
	@Override
	public void deliverResult(Cursor cursor){
		if (isReset()){
			if (cursor != null) cursor.close();
			return;
		}

		Cursor oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted())
			super.deliverResult(cursor);

		if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed())
			oldCursor.close();
	}

	/**
	 * Starts asynchronous load of the data. 
	 * Must be called from the UI thread.
	 */
	@Override
	protected void onStartLoading(){
		if (mCursor != null)
			deliverResult(mCursor);
		if (takeContentChanged() || mCursor == null)
			forceLoad();
	}


	/** 
	 * Must be called from the UI thread.
	 */
	@Override
	protected void onStopLoading(){
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	public void onCanceled(Cursor cursor){
		if (cursor != null && !cursor.isClosed())
			cursor.close();
	}

	@Override
	protected void onReset(){
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		if (mCursor != null && !mCursor.isClosed())
			mCursor.close();

		mCursor = null;
	}
}
