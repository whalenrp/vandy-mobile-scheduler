package com.vmat;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
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
import java.util.List;
import java.util.NoSuchElementException;
import android.util.Log;

public class MainActivity extends Activity
{
    private ListView listView;
//    private JSON_Adapter items=null;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = (ListView)findViewById(R.id.meetings);
//        items = new JSON_Adapter();
        new JSON_Parse().execute();
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

            return (jsonArray);
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            // For error checking
            Log.i(MainActivity.class.getName(), "Number of entries " + jsonArray.length());
//            try{
//
//                for (int i=0; i < jsonArray.length(); ++i){
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    items.add(jsonObject);
//                    Log.i(JSON_Parse.class.getName(), jsonObject.getString("text"));
//                }
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            Log.i(MainActivity.class.getName(), "Number of items " + items.size());
            String[] topics = new String[jsonArray.length()];
            try{
                for (int i=0; i< jsonArray.length(); ++i){
                    topics[i] = jsonArray.getJSONObject(i).getString("topic");
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, topics));
//            listView.setAdapter(new JSON_Adapter());
        }

    }

//    class JSON_Adapter extends ArrayAdapter<JSONObject>{
//        JSON_Adapter(){
//            super(MainActivity.this, R.layout.rowlayout);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent){
//            View row = convertView;
////            ViewHolder holder = (ViewHolder)row.getTag();
//            if (row == null){
//                LayoutInflater inflater = getLayoutInflater();
//                row = inflater.inflate(R.layout.rowlayout, parent, false);
//                ViewHolder holder = new ViewHolder(row);
//                row.setTag(holder);
//            }
//            ViewHolder holder = (ViewHolder)row.getTag();
//            try{
//                holder.topic.setText((String)items.get(position).get("topic"));
//            }catch(JSONException e){
//                e.printStackTrace();
//            }
//            return row;
//        }
//
//    }

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
