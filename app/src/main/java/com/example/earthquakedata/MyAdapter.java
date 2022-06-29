package com.example.earthquakedata;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends ArrayAdapter<quakes> {
    private List<quakes> dataSet;
    Context mContext;

    //paste this class inside the adapter class
    private static class ViewHolder {
        TextView locationName;
        TextView magnitude;
        TextView date;
        LinearLayout mainListLayout;
    }

    public MyAdapter(ArrayList<quakes> data, Context context) {
        super(context, R.layout.main_list_items, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        quakes currentQuake = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.main_list_items, parent, false);
            viewHolder.locationName = (TextView) convertView.findViewById(R.id.locationName);
            viewHolder.magnitude = (TextView) convertView.findViewById(R.id.magnitude);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.mainListLayout = (LinearLayout)convertView.findViewById(R.id.mainList_layout);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        String mlocation = currentQuake.getLocation();
        if(mlocation.indexOf("of")!=-1) {
            String[] parts = mlocation.split("of");
            mlocation=parts[1];
        }

        viewHolder.locationName.setText(mlocation);
        String str = String.valueOf(currentQuake.getMag());
        viewHolder.magnitude.setText(str);
        if(currentQuake.getMag() >= 7){
            viewHolder.magnitude.setTextColor(Color.parseColor("#ff1744"));
        }else{
            if(currentQuake.getMag() >=6){
                viewHolder.magnitude.setTextColor(Color.parseColor("#ff6d00"));
            }else{
                viewHolder.magnitude.setTextColor(Color.parseColor("#64dd17"));
            }
        }
        viewHolder.date.setText(currentQuake.getDate());

        // Return the completed view to render on screen
        return convertView;
    }
}
