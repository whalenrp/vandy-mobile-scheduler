package com.vmat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import android.util.Log;

public class MainActivity extends Activity
{
    private ListView listView;
    private JSONObject[] items;
    private EventsDB db;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = (ListView)findViewById(R.id.meetings);
        db = new EventsDB(this);
        new JSON_Parse().execute();
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
        	public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);      
                finish();
            }
          });
        
    }

    class JSON_Parse extends AsyncTask<Void, Void, JSONArray>{

        @Override
        protected JSONArray doInBackground(Void... unsused){
            String jsonString = "";
            try{
                URL url = new URL("http://70.138.50.84/meetings.json");
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                try{
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    jsonString = convertStreamToString(in);
                }finally{
                    urlConnection.disconnect();
                }
            }catch(MalformedURLException e){
                // Make a toast saying the site has moved?
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
            JSONArray jsonArray = null;
            try{
                jsonArray = new JSONArray(jsonString);

            }catch(JSONException e){
                // Do something more intelligent here..
                // This is hit when the connection is established, but there is no data
                // on the page.
                e.printStackTrace();
            }


            try{
                for (int i=0; i< jsonArray.length(); ++i){
                    JSONObject object =  jsonArray.getJSONObject(i);
                    db.insert(object.getString("created_at"),object.getString("date"),
                            object.getString("day"), object.getString("description"),
                            object.getBoolean("food"), object.getBoolean("speaker"),
                            object.getString("speaker_name"), object.getString("topic"),
                            object.getString("updated_at"), object.getDouble("xcoordinate"),
                            object.getDouble("ycoordinate"));

                }
            }catch(JSONException e){
                e.printStackTrace();
            }

            return (jsonArray);
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            // For error checking
            Log.i(MainActivity.class.getName(), "Number of entries " + jsonArray.length());
            items = new JSONObject[jsonArray.length()];
            try{
                for (int i=0; i< jsonArray.length(); ++i){
                    items[i] = jsonArray.getJSONObject(i);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            listView.setAdapter(new JSON_Adapter());
            Log.i(MainActivity.class.getName(), "Total number of list entries " + listView.getAdapter().getCount());
        }

    }

    class JSON_Adapter extends ArrayAdapter<JSONObject>{
        JSON_Adapter(){
            super(MainActivity.this, R.layout.rowlayout);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View row = convertView;
            if (row == null){
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.rowlayout, parent, false);
                ViewHolder holder = new ViewHolder(row);
                row.setTag(holder);
            }
            ViewHolder holder = (ViewHolder)row.getTag();
            try{
                holder.topic.setText(items[position].getString("topic"));
                holder.speaker.setText(items[position].getString("speaker_name"));
                holder.date.setText(items[position].getString("date"));
            }catch(JSONException e){
                e.printStackTrace();
            }
            return row;
        }

        @Override
        public int getCount(){
            return items.length;
        }

    }

    // Helper function for reading input stream
    // retrieved from http://stackoverflow.com/a/5445161/793208
    private String convertStreamToString(InputStream is){
        try{
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        }catch(NoSuchElementException e){
            return "";
        }
    }
}
