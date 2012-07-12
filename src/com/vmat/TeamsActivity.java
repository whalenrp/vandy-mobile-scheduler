package com.vmat;

import com.actionbarsherlock.app.ActionBar;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TeamsActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener
{
	private ListView listView;
	private String[] tabs;
	
	public void onCreate(Bundle savedInstanceState)
	{
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teams);
		
		listView = (ListView)findViewById(R.id.list);
		
		
		String[] apps = { "Apple", "Google", "Microsoft", "Professional Archives" };
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, apps));
		
		// Set up Action Bar Sherlock
		Context context = getSupportActionBar().getThemedContext();
		tabs = getResources().getStringArray(R.array.tabs);
		ArrayAdapter<CharSequence> navList = ArrayAdapter.createFromResource(context, 
				R.array.tabs, R.layout.sherlock_spinner_item);
		navList.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(navList, this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	// ActionBar.OnNavigationListener Method
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) 
	{
		// Launch intents based on selected tab.
		return false;
	}
}
