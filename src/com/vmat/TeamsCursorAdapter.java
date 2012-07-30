package com.vmat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TeamsCursorAdapter extends CursorAdapter 
{
	public TeamsCursorAdapter(Context context)
	{
		super(context, null, 0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) 
	{
		ImageView appIcon = (ImageView)view.findViewById(R.id.img_app_icon);
		TextView titleText = (TextView)view.findViewById(R.id.text_app_title);
		TextView taglineText = (TextView)view.findViewById(R.id.text_app_tagline);
		
		byte[] imageData = cursor.getBlob(cursor.getColumnIndex("app_icon"));
		if (imageData != null)
		{
			Bitmap b = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
			appIcon.setImageBitmap(b);
		}
		
		// temporary fix until icons are added to server data
//		Drawable icon = context.getResources().getDrawable(R.drawable.ic_dialog_alert_holo_light);
//		appIcon.setImageDrawable(icon);
		
		titleText.setText(cursor.getString(cursor.getColumnIndex("name")));
		taglineText.setText(cursor.getString(cursor.getColumnIndex("tagline")));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.teams_row, null);
	}

}
