package com.vmat;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TwitterCursorAdapter extends CursorAdapter 
{
	public TwitterCursorAdapter(Context context)
	{
		super(context, null, 0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) 
	{
		TextView tweetText = (TextView)view.findViewById(R.id.text);
		
		tweetText.setText(cursor.getString(cursor.getColumnIndex(TwitterDB.TEXT)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.twitterrowlayout, null);
	}

}
