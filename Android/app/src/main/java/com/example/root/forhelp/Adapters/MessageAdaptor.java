package com.example.root.forhelp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.root.forhelp.R;
import com.example.root.forhelp.StaticCl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MessageAdaptor extends ArrayAdapter<ArrayList<String>> {

    private final Context context;
    private ArrayList<ArrayList<String>> values;



    public MessageAdaptor(@NonNull Context context, ArrayList<ArrayList<String>> resource) {
        super( context, R.layout.activity_main,resource);
        this.context = context;
        this.values = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.message_item, parent, false);
        LinearLayout lr = rowView.findViewById(R.id.tl1);
        ImageView iv = new ImageView(rowView.getContext());
        iv.setLayoutParams(new LinearLayout.LayoutParams(500,500));
        LinearLayout tl = (LinearLayout) rowView.findViewById(R.id.tl);
        if (values.get(position).size() == 4 && values.get(position).get(3)!=null){


            Glide
                    .with(rowView.getContext())
                    .load(new File(values.get(position).get(3)))
                    .into(iv);

            lr.addView(iv);
        }

        TextView textView = (TextView) rowView.findViewById(R.id.message_text);
        textView.setText(values.get(position).get(0));
        TextView textView1 = (TextView) rowView.findViewById(R.id.message_from);
        textView1.setText(values.get(position).get(1)+": ");
        if(values.get(position).get(2).equals("form_to")){
            lr.setBackgroundColor(rowView.getResources().getColor(R.color.my_message));
            tl.setGravity(Gravity.RIGHT);
            ViewGroup.LayoutParams params = textView.getLayoutParams();
            params.width =(int)(textView.getResources().getDisplayMetrics().density*200);
            textView.setLayoutParams(params);
            this.notifyDataSetChanged();
        }
        else if (values.get(position).get(2).equals("to")){
            lr.setBackgroundColor(rowView.getResources().getColor(R.color.other_message));
            ViewGroup.LayoutParams params = textView.getLayoutParams();
            params.width =(int)(textView.getResources().getDisplayMetrics().density*200);
            textView.setLayoutParams(params);
        }
        else{
            tl.setGravity(Gravity.CENTER);
            textView.setTextSize(15);
            textView1.setTextSize(15);
            textView1.setText("");
        }
        return rowView;
    }
}
