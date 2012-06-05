package com.vmat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.CursorAdapter;

import android.widget.SimpleCursorAdapter;

import android.text.format.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class MainActivity extends Activity
{
    private ListView listView;
    private Cursor meetingList;
    private EventsDB db;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = (ListView)findViewById(R.id.meetings);
        db = new EventsDB(this);
        meetingList = db.getReadableDatabase().rawQuery("SELECT * FROM meetings", null);
        new JSON_Parse().execute();

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
        	public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                Cursor tempCursor = ((SimpleCursorAdapter)listView.getAdapter()).getCursor();
                tempCursor.moveToPosition(position);
                intent.putExtra("id", tempCursor.getInt(tempCursor.getColumnIndex("_id")));
                startActivity(intent);

                finish();
            }
          });

        listView.setAdapter(new EventsCursorAdapter());
        
    }
    
//    @Override
//    public void onPause(){
//    	super.onPause();
//    	meetingList.close();
//    }
    


    @Override
    public void onDestroy(){
        super.onPause();
        meetingList.close();
    }

    class JSON_Parse extends AsyncTask<Void, Void, Cursor>{

        @Override
        protected Cursor doInBackground(Void... unsused){
            Cursor newCursor = db.refreshDB();
            return newCursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            // In the UI thread, make a new cursor with the updated db entries.
            meetingList.close();
            meetingList = cursor;
            listView.setAdapter(new EventsCursorAdapter());
            db.close();
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
//            holder.date.setText(SimpleDateFormat("E, L dd '@' hh:mm a",));
        }

    }
}
