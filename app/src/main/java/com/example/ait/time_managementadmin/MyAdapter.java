package com.example.ait.time_managementadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by AIT context 12/1/16.
 */

public class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private ArrayList<String> users;
    private LayoutInflater inflater;

    public MyAdapter(Context context, ArrayList<String> users) {
        inflater = LayoutInflater.from(context);
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_layout, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.event);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(users.get(position).split("-")[0]);

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        String check = users.get(position).split("-")[1];
        if ( check.equals("user")) {
            String headerText = "Users";
            holder.text.setText(headerText);
        } else {

            String headerText = "Editors";
            holder.text.setText(headerText);
        }

        //set header text as first char in name
//        String headerText = "" + countries[position].subSequence(0, 1).charAt(0);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
       // return Long.valueOf(users.get(position).split("-")[1]).longValue();

        //return Long.parseLong(users.get(position).split("-")[1], 10);
        String x = users.get(position).split("-")[1];
        char[] y = x.toCharArray();
        long z = 0;

        for (int i =0; i<y.length;i++)
        {
            z += (long) y[i];
        }
        return z;
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView text;
    }

}