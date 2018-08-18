package com.example.root.forhelp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.root.forhelp.ChatActivity;
import com.example.root.forhelp.Config.Config;
import com.example.root.forhelp.R;
import com.example.root.forhelp.StaticCl;
import com.example.root.forhelp.User;
import com.github.nkzawa.socketio.client.Socket;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdaptor extends ArrayAdapter<ArrayList<String>> {

    private final Context context;
    private ArrayList<ArrayList<String>> values;
    private ArrayList<ArrayList<String>> mCleanCopyDataset;
    Socket mSocket;
    String room;
    AlertDialog dlg;



    public UserAdaptor(@NonNull Context context, ArrayList<ArrayList<String>> resource, Socket mSocket, String room,AlertDialog dlg) {
        super( context, R.layout.activity_main,resource);
        this.context = context;
        this.values = resource;
        mCleanCopyDataset = values;
        this.mSocket =  mSocket;
        this.room = room;
        this.dlg = dlg;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = new TextView(rowView.getContext());
        textView.setTextSize(32);
        textView.setText(values.get(position).get(0));
        Toolbar toolbar = rowView.findViewById(R.id.toolbar);

        CircleImageView iv = new CircleImageView(rowView.getContext());
        iv.setPadding(5,5,5,5);
        iv.setImageDrawable(context.getResources().getDrawable(R.color.other_message));
        textView.setPadding(5,5,5,5);
        iv.setLayoutParams(new LinearLayout.LayoutParams(150,150));
        toolbar.addView(iv);
        toolbar.addView(textView);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                StaticCl.invite(mSocket,values.get(position).get(0),room);
                dlg.dismiss();
            }
        });

        return rowView;
    }

    // put below code (method) in Adapter class




}
