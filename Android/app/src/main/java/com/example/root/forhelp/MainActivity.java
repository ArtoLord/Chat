package com.example.root.forhelp;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.forhelp.Adapters.JustRoomAdapter;
import com.example.root.forhelp.Adapters.RoomAdapter;
import com.example.root.forhelp.Config.Config;
import com.example.root.forhelp.Requests.Items;
import com.example.root.forhelp.Requests.LoginApi;
import com.example.root.forhelp.Requests.Message;
import com.example.root.forhelp.Requests.Res;
import com.example.root.forhelp.Table.Contract;
import com.example.root.forhelp.Table.DbHelper;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import com.github.nkzawa.socketio.client.Socket;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    ListView lv;
    User user;
    EditText inputSearch;
    RoomAdapter adapter;
    JustRoomAdapter Adapter;
    ArrayList<ArrayList<String>> items;
    Context ctxt;
    ArrayList<ArrayList<String>> itemss;
    SharedPreferences sPref;
    DbHelper mDbHelper;


    private Socket mSocket;


    private static LoginApi Login;
    private Retrofit retrofit;
    String url = Config.url;

    @Override
    public void onBackPressed(){
        finishAffinity();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainactivitymenu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View view = inflater.inflate(R.layout.room_add_or_input, null);
            builder.setView(view);
            builder.setCancelable(true);

            final String[] types = {
                    "open",
                    "private"
            };
            StaticCl.types = types[0];
            builder.setSingleChoiceItems(types, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    StaticCl.types = types[i];
                }
            });
            final EditText et = (EditText) view.findViewById(R.id.add_edit_text);
            Button btn = (Button) view.findViewById(R.id.btn);
            final AlertDialog dlg = builder.create();
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = et.getText().toString();
                    if (text.length() != 0) {
                        StaticCl.newroom(mSocket, text,StaticCl.types);
                        writetable();
                        dlg.dismiss();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Нельзя добавить комнату без названия", Toast.LENGTH_LONG);
                        toast.show();
                    }

                }
            };
            btn.setOnClickListener(listener);

            dlg.show();
        }
        if (item.getItemId() == R.id.action_go_to_room) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View view = inflater.inflate(R.layout.users_allrooms, null);
            builder.setView(view);
            ListView listView = (ListView) view.findViewById(R.id.listview);
            itemss = new ArrayList<>();
            final AlertDialog dlg = builder.create();
            Adapter = new JustRoomAdapter(view.getContext(), itemss,mSocket,user,dlg);
            getallrooms();
            EditText itemSearch = (EditText)view.findViewById(R.id.search_edit_text);
            listView.setAdapter(Adapter);
            dlg.show();



        }
        if (item.getItemId() == R.id.action_exit) {
            SharedPreferences.Editor ed = sPref.edit();
            mDbHelper = new DbHelper(ctxt);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            db.execSQL("DROP TABLE " + Contract.messages.TABLE_NAME);
            db.execSQL("DROP TABLE " + Contract.images.TABLE_NAME);
            mDbHelper.onCreate(db);
            // Создаём новую таблицу



            sPref = getSharedPreferences("MyPref", MODE_PRIVATE);
            ed.putString(StaticCl.IS_ENTER, "False"); //String not boolean
            ed.apply();
            mSocket.disconnect();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                writetable();
                refreshLayout.setRefreshing(false);
            }
        });
        inputSearch = (EditText) findViewById(R.id.search_edit_text);

        user = (User) getIntent().getParcelableExtra(
                User.class.getCanonicalName());

        sPref = getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(StaticCl.EMAIL, user.email);
        ed.putString(StaticCl.JWT, user.jwt);
        ed.putString(StaticCl._ID, user._id);
        ed.putString(StaticCl.IS_ENTER, "True"); //String not boolean
        ed.apply();
        items = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listview);




        {
            try {
                mSocket = IO.socket(Config.SocketUrl);
                StaticCl.socket = mSocket;

            } catch (Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Сonnection error", Toast.LENGTH_LONG);
                toast.show();

            }
        }
        adapter = new RoomAdapter(this, items,mSocket,user);
        lv.setAdapter(adapter);
        ctxt = this;


        getSupportActionBar().setTitle("Loading...");
        getSupportActionBar().setSubtitle("Please wait");
        mSocket.connect();
        if (mSocket.connected()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Connected", Toast.LENGTH_LONG);
            toast.show();
        }


        retrofit = new Retrofit.Builder()
                .baseUrl(url) //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        Login = retrofit.create(LoginApi.class);

        StaticCl.conn(mSocket, user.email);

        writetable();




        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // Когда, юзер изменяет текст он работает
                writesearchtable(inputSearch.getText().toString());

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub


            }
        });


    }

    public void writetable() {


        Login.getroomlist(user.jwt).enqueue(new Callback<ArrayList<ArrayList<String>>>() {
            @Override
            public void onFailure(Call<ArrayList<ArrayList<String>>> call, Throwable t) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error connection", Toast.LENGTH_LONG);
                Log.e("Error", t.toString() + '\n' + call);
                toast.show();

                getSupportActionBar().setTitle("Connection error");
                getSupportActionBar().setSubtitle("Please reload the activity");


            }

            @Override
            public void onResponse(Call<ArrayList<ArrayList<String>>> call, Response<ArrayList<ArrayList<String>>> response) {
                items.clear();
                getSupportActionBar().setTitle("Rooms");
                getSupportActionBar().setSubtitle("");
                items.addAll(response.body());
                adapter.notifyDataSetChanged();

            }

            ;

            // Listview Adapter


        });
    }

    public void writesearchtable(String text) {
        ArrayList<ArrayList<String>> arr = new ArrayList<>();
        // Listview Adapter

        // EditText в котором будем искать
        inputSearch = (EditText) findViewById(R.id.search_edit_text);
        // ArrayList для Listview


        if (text.length() == 0) {
            writetable();
        } else {

            for (ArrayList<String> c : items) {


                // Используем индекс для получения строки или числа
                ArrayList<String> a = new ArrayList<>();
                a.add(c.get(0));
                a.add(c.get(1));

                if (a.get(0).contains(text)) {
                    arr.add(a);
                }
            }

            items.clear();
            items.addAll(arr);

            adapter.notifyDataSetChanged();

        }
    }

    void getallrooms() {


        Login.getallroom(user.jwt).enqueue(new Callback<ArrayList<ArrayList<String>>>() {
            @Override
            public void onFailure(Call<ArrayList<ArrayList<String>>> call, Throwable t) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error connection", Toast.LENGTH_LONG);
                Log.e("Error", t.toString() + '\n' + call);
                toast.show();


            }

            @Override
            public void onResponse(Call<ArrayList<ArrayList<String>>> call, Response<ArrayList<ArrayList<String>>> response) {
                itemss.addAll(response.body());
                Adapter.notifyDataSetChanged();


            }

        });

    };


}



