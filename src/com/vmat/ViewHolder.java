package com.vmat;

import android.widget.TextView;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: richard
 * Date: 5/30/12
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class ViewHolder {
    TextView text = null;
    ViewHolder(View base){
        this.topic = (TextView)base.findViewById(R.id.topic);
        this.speaker = (TextView)base.findViewById(R.id.speaker);
        this.date = (TextView)base.findViewById(R.id.date);
    }
}
