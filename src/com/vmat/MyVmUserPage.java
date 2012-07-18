package com.vmat;

import com.actionbarsherlock.app.SherlockActivity;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.json.JSONObject;
import org.json.JSONArray;

public class MyVmUserPage extends SherlockActivity
{
	TextView userInfo;
	ListView teamList;
	JSONObject[] teams;

    @Override
	public void onCreate(Bundle savedInstantState){
		super.onCreate(savedInstantState);
		setContentView(R.layout.my_vm_user_page);

		userInfo = (TextView)findViewById(R.id.userInfo);
		teamList = (ListView)findViewById(R.id.teams);
		
		fillTextViews();
		teamList.setAdapter(new JSONAdapter());

		teamList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView parent, View childView, int pos, long id){
				Intent i = new Intent(this, GithubDetailActivity.java);
				i.putExtra("teamJSON", teams[pos].toString());
				startActivity(i);
			}
		});
	}

	private void fillTextViews(){
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		JSONObject userJSON = new JSONObject(prefs.getString("userInfo", ""));

		String username = userJSON.getString("username");
		String email = userJSON.getString("email");

		JSONArray userTeams = userJSON.getJSONArray("teams");
		if ( userTeams.length() == 1 ){
			startActivity(new Intent(this, GithubDetailActivity.java));
			finish();
		}
		else{
			
			// fill the teamNames array with the teamnames in the JSONArray
			teams = new JSONObject[userTeams.length()];
			for (int i = 0; i < userTeams.length(); ++i)
				//teams[i] = new JSONObject(userTeams.getJSONObject(i).toString());
				teams[i] = userTeams.getJSONObject(i);
		}
	}

	private class JSONAdapter extends ArrayAdapter<JSONObject>{
		public JSONAdapter(){
			super(MyVmUserPage.this, android.R.layout.two_line_list_item, teams);
		}

		// set text1's text to the team name and 
		// set text2's text to the app name
		public View getView(int pos, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(android.R.layout.two_line_list_item, parent, false);
			TextView text1 = (TextView)row.findViewById(android.R.id.text1);
			TextView text2 = (TextView)row.findViewById(android.R.id.text2);
			text1.setText(teams[pos].getString("name"));
			text2.setText(teams[pos].getJSONObject("app").getString("name"));
		}

	}
}
