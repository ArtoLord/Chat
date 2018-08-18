package com.example.root.forhelp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.root.forhelp.ChatActivity;
import com.example.root.forhelp.Config.Config;
import com.example.root.forhelp.MainActivity;
import com.example.root.forhelp.R;
import com.example.root.forhelp.StaticCl;
import com.example.root.forhelp.User;
import com.github.nkzawa.socketio.client.Socket;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomAdapter extends ArrayAdapter<ArrayList<String>>{

    private final Context context;
    private ArrayList<ArrayList<String>> values;
    private ArrayList<ArrayList<String>> mCleanCopyDataset;
    Socket mSocket;
    User user;



    public RoomAdapter(@NonNull Context context, ArrayList<ArrayList<String>> resource, Socket mSocket,User user) {
        super( context, R.layout.activity_main,resource);
        this.context = context;
        this.values = resource;
        mCleanCopyDataset = values;
        this.mSocket =  mSocket;
        this.user = user;
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
        iv.setPadding(5,5,20,5);
        iv.setImageDrawable(context.getResources().getDrawable(R.color.other_message));
        textView.setPadding(20,5,5,5);
        iv.setLayoutParams(new LinearLayout.LayoutParams(150,150));
            toolbar.inflateMenu(R.menu.room_menu);
        if(values.get(position).get(1)!=null) {

            Glide
                    .with(context)
                    .load(Config.SocketUrl + "/" + values.get(position).get(1))
                    .placeholder(R.drawable.plhldr)
                    .into(iv);
        }
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==R.id.action_leave){
                        AlertDialog.Builder builder = new AlertDialog.Builder(rowView.getContext());
                        builder.setTitle("Dialog window");
                        builder.setMessage("Вы хотите покинуть комнату?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                StaticCl.leave(mSocket,values.get(position).get(0));
                                dialogInterface.cancel();
                            }
                        });
                        builder.setNegativeButton("Нет",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });


                        AlertDialog dlg = builder.create();
                        dlg.show();
                    }
                    return false;
                }
            });
            toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    Intent intent = new Intent(context, ChatActivity.class);

                    intent.putExtra(User.class.getCanonicalName(), user);
                    String room = values.get(position).get(0);
                    intent.putExtra("Room", room);
                    intent.putExtra("type", "");
                    context.startActivity(intent);



                }
            });

        toolbar.addView(iv);
        toolbar.addView(textView);

        return rowView;
    }

    // put below code (method) in Adapter class




    }
