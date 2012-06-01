package com.vmat;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
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
		myInformation = myDataBase.rawQuery("SELECT 1 FROM meetings WHERE id = ?", index);
//		int columns = myInformation.getColumnCount();
//		colNames = new String[columns];
		colNames = myInformation.getColumnNames();
		int topicNum = search(colNames,"topic");
		TextView topic=(TextView)findViewById(R.id.topic);
		topic.setText(myInformation.getString(topicNum));
		setText((TextView)findViewById(R.id.details));
	}
	
	
	
	private void setText(TextView details) {
		// TODO Auto-generated method stub
		String information = "";
		information += myInformation.getString(search(colNames, "speaker")) + "\n";
		information += myInformation.getString(search(colNames, "date")) + "\n";
		information += myInformation.getString(search(colNames, "location")) + "\n";
		information += myInformation.getString(search(colNames, "food")) + "\n";
		
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
