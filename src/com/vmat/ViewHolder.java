package com.vmat;

import android.view.View;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: richard
 * Date: 5/30/12
 * Time: 11:37 AM
 * A simple implementation of the Holder pattern for the custom cursor adapter used in MainActivity.
 */
public class ViewHolder {
    TextView topic = null;
    TextView speaker = null;
    TextView date = null;
    ViewHolder(View base){
        this.topic = (TextView)base.findViewById(R.id.topic);
        this.speaker = (TextView)base.findViewById(R.id.speaker);
        this.date = (TextView)base.findViewById(R.id.date);
    }
}
