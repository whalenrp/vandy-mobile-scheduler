package com.vmat;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends Activity{
	private EventsDB hasDataBase; 
	private SQLiteDatabase myDataBase;
	private Cursor myInformation;
	private String[] colNames;
	

    @Override
	public void onCreate(Bundle savedInstantState){
        super.onCreate(savedInstantState);
        setContentView(R.layout.detail_activity);
		hasDataBase = new EventsDB(this);
		myDataBase = hasDataBase.getReadableDatabase();
		String[] index = new String[1];
		index[0] = "" + getIntent().getIntExtra("id", -1);

		myInformation = myDataBase.rawQuery("SELECT * FROM meetings WHERE _id=? LIMIT 1", index);
		colNames = myInformation.getColumnNames();
		int topicNum = search(colNames,"topic");
		TextView topic=(TextView)findViewById(R.id.topic);
		topic.setText(myInformation.getString(topicNum));
		TextView details=(TextView)findViewById(R.id.details);
		details.setText(getText(details));
	}
	
	
	
	private String getText(TextView details) {
		// TODO Auto-generated method stub
		String information = "";
		information += myInformation.getString(search(colNames, "speaker_name")) + "\n";
		information += myInformation.getString(search(colNames, "date")) + "\n";
		information += myInformation.getString(search(colNames, "food")) + "\n";
		return information;
	}



	private int search(String[] colNames, String string) {
		// TODO Auto-generated method stub
		for(int i = 0; i < colNames.length; ++i){
			if(colNames[i].equals(string))
				return i;
		}
		return -1;
	}

    @Override
	public void onDestroy(){
        super.onDestroy();
		hasDataBase.close();
	}
}
