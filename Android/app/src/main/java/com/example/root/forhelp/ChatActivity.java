package com.example.root.forhelp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.forhelp.Adapters.MessageAdaptor;
import com.example.root.forhelp.Adapters.UserAdaptor;
import com.example.root.forhelp.Config.Config;
import com.example.root.forhelp.Requests.Id;
import com.example.root.forhelp.Requests.LoginApi;
import com.example.root.forhelp.Requests.Message;
import com.example.root.forhelp.Requests.To;
import com.example.root.forhelp.Table.Contract;
import com.example.root.forhelp.Table.DbHelper;
import com.example.root.forhelp.Table.SeeImageActivity;
import com.github.nkzawa.emitter.Emitter;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {
    User user;
    private DbHelper mDbHelper;
    String room;
    String Type;
    MessageAdaptor adapter;
    ArrayList<ArrayList<String>> items;
    ListView lv;
    private static LoginApi Login;
    private Retrofit retrofit;
    String url = Config.url;
    Context ctx;
    ArrayList<ArrayList<String>> itemss;
    UserAdaptor Adapter;
    static final int GALARY_REQUEST = 1;
    static final int AVATAR_REQUEST = 3;




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatmenu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_add_avatar){
            Intent picker = new Intent(Intent.ACTION_PICK);
            picker.setType("image/*");
            startActivityForResult(picker,AVATAR_REQUEST);
        }
        if (item.getItemId()==R.id.action_invite){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View view = inflater.inflate(R.layout.users_allrooms, null);
            builder.setView(view);
            ListView listView = (ListView) view.findViewById(R.id.listview);
            itemss = new ArrayList<>();
            final AlertDialog dlg = builder.create();
            Adapter = new UserAdaptor(view.getContext(), itemss,StaticCl.socket,room,dlg);
            listView.setAdapter(Adapter);
            EditText itemSearch = (EditText)view.findViewById(R.id.search_edit_text);
            itemSearch.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // Когда, юзер изменяет текст он работает
                    if(cs.length()!=0){
                    getallrooms(cs.toString());

                }
                else{
                        itemss.clear();
                        Adapter.notifyDataSetChanged();
                    }}

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

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position,
                                        long id) {
                    StaticCl.invite(StaticCl.socket,itemss.get(position).get(0),room);
                    writeMessages(room);
                    dlg.dismiss();
                }
            });


            dlg.show();
        }

        return true;
    }

    private void getallrooms(final String email) {
        itemss.clear();


        Login.getusers(user.jwt,new To(email,1)).enqueue(new Callback<ArrayList<ArrayList<String>>>() {
            @Override
            public void onFailure(Call<ArrayList<ArrayList<String>>> call, Throwable t) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error connection", Toast.LENGTH_LONG);
                Log.e("Error", t.toString() + '\n' + call);
                toast.show();


            }

            @Override
            public void onResponse(Call<ArrayList<ArrayList<String>>> call, Response<ArrayList<ArrayList<String>>> response) {
                if(response.body()!=null) {

                    itemss.addAll(response.body());
                    Adapter.notifyDataSetChanged();
                }
                else{
                    getallrooms(email);
                }

            }

        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final Intent intent = getIntent();



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        user = (User) intent.getParcelableExtra(
                User.class.getCanonicalName());
        room = intent.getStringExtra("Room").toString();
        Type = intent.getStringExtra("type").toString();
        retrofit = new Retrofit.Builder()
                .baseUrl(url) //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        Login = retrofit.create(LoginApi.class);
        ctx = this;
        StaticCl.socket.on("chat message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (args[3].toString().equals("Server")) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    args[0].toString(), Toast.LENGTH_LONG);
                            toast.show();
                        }
                        else if (args[2].toString().equals(room)){
                            if (args[6]!=null){
                            StaticCl.insertMessage(args[0].toString(),args[1].toString(),args[2].toString(),args[4].toString(),Long.parseLong(args[5].toString()),args[6].toString(),ctx);}
                            else{StaticCl.insertMessage(args[0].toString(),args[1].toString(),args[2].toString(),args[4].toString(),Long.parseLong(args[5].toString()),null,ctx);}

                            if(args[6]!=null && !args[6].toString().equals("Hello")){
                                final String data = args[6].toString();
                                Retrofit retrofi = new Retrofit.Builder()
                                        .baseUrl(Config.SocketUrl) //Базовая часть адреса
                                        .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                                        .build();
                                LoginApi login = retrofi.create(LoginApi.class);
                                login.imageload(data).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        InputStream inputStream = response.body().byteStream();
                                        try {
                                            File file = new File(StaticCl.getPublicAlbumDir().getPath(),data+".jpeg");
                                            OutputStream outputStream = new FileOutputStream(file);
                                            IOUtils.copy(inputStream,outputStream);
                                            inputStream.close();
                                            outputStream.close();
                                            StaticCl.insertImage(data,file.getPath(),ctx);
                                            adapter.notifyDataSetChanged();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                                    }
                                });
                            }
                            ArrayList<String> arr = new ArrayList<>();
                            arr.add(args[0].toString());
                            arr.add(args[1].toString());
                            String from = args[1].toString();
                            if(from.equals(user.email)){
                                arr.add("form_to");
                            }
                            else{
                                if(from.equals("Server")){
                                    arr.add("Server");
                                }
                                else{
                                    arr.add("to");}
                            }

                            if (args[6]!=null){
                                arr.add(StaticCl.getPublicAlbumDir().getAbsolutePath()+"/"+args[6].toString()+".jpeg");
                                }
                            else{
                                arr.add(null);
                            }
                            items.add(arr);
                            adapter.notifyDataSetChanged();
                            lv.smoothScrollToPosition(items.size()-1);

                        }


                    }
                });
            }
        });



        mDbHelper = new DbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        items = new ArrayList<>();



        getSupportActionBar().setTitle(room);
        getSupportActionBar().setSubtitle(Type);

        adapter = new MessageAdaptor(this, items);

        lv = (ListView) findViewById(R.id.messagelist);
        lv.setAdapter(adapter);

        writeMessages(room);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(items.get(i).get(3)!=null){
                    Intent intent1 = new Intent(ChatActivity.this, SeeImageActivity.class);
                    intent1.putExtra("path",items.get(i).get(3));
                    startActivity(intent1);
                }
            }
        });

    }


    public void writeMessages(final String to) {
        items.clear();
        long LastDate = StaticCl.getLastDate(ctx,to);
        Login.getmessages(user.jwt,new To(to,LastDate)).enqueue(new Callback<List<Message>>() {
            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                getSupportActionBar().setTitle("Connection error");
                getSupportActionBar().setSubtitle("Please reload the activity");


            }

            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                items.clear();
                getSupportActionBar().setTitle(to);
                getSupportActionBar().setSubtitle("");


try{
                for (Message c : response.body()) {

                    StaticCl.insertMessage(c.text, c.from, c.to,c.id,Long.parseLong(c.date), c.data, ctx);
                    if (c.data!=null){
                        final String data = c.data;
                        Retrofit retrofi = new Retrofit.Builder()
                                .baseUrl(Config.SocketUrl) //Базовая часть адреса
                                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                                .build();
                        LoginApi login = retrofi.create(LoginApi.class);
                        login.imageload(data).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                InputStream inputStream = response.body().byteStream();
                                try {
                                    File file = new File(StaticCl.getPublicAlbumDir().getPath(),data+".jpeg");
                                    Log.e("",file.getPath());
                                    OutputStream outputStream = new FileOutputStream(file);
                                    IOUtils.copy(inputStream,outputStream);
                                    inputStream.close();
                                    outputStream.close();
                                    StaticCl.insertImage(data,file.getPath(),ctx);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                            }
                        });
                    };
                }}
                catch (Exception e){}



                items.addAll(StaticCl.getMessages(ctx,to,user.email));
                adapter.notifyDataSetChanged();

            }

        });




            }







public  void send(View view){
    EditText send = (EditText) findViewById(R.id.send_edit_text);
    if((send.getText().toString().length()!=0)||(StaticCl.upload_id!=null)){
    StaticCl.sendto(StaticCl.socket,send.getText().toString(),room,1,StaticCl.upload_id);
    StaticCl.upload_id = null;
    ImageView iv = (ImageView)findViewById(R.id.image);
    iv.setImageBitmap(null);

    send.setText("");}
}

public void addimage(View v){
        Intent picker = new Intent(Intent.ACTION_PICK);
        picker.setType("image/*");
        startActivityForResult(picker,GALARY_REQUEST);
}


@Override
protected void onActivityResult(int requestCode,int resultCode, Intent imageResultIntent){
        super.onActivityResult(requestCode,resultCode,imageResultIntent);
        ImageView iv = (ImageView)findViewById(R.id.image);
        switch (requestCode){
            case GALARY_REQUEST:{
                if(resultCode==RESULT_OK){
                    Uri selectImage = imageResultIntent.getData();
                    try{
                        StaticCl.checkPermissions(this);
                        ContentResolver cr = this.getContentResolver();
                        Cursor cursor = cr.query(
                                selectImage,
                                new String[]{MediaStore.Images.Media.DATA},
                                null,
                                null,
                                null
                        );
                        cursor.moveToFirst();
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        Log.e("path",path);
                        if(cursor!=null){
                            cursor.close();
                        }
                        StaticCl.decodeSampleBitmap(new File(StaticCl.getPublicAlbumDir().getAbsolutePath(),"new.jpeg").getAbsolutePath(),path,700,700,50);
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(path,options);
                        options.inSampleSize = StaticCl.calculateInSampleSize(options,300,300);
                        options.inJustDecodeBounds = false;
                        Bitmap newbitb = BitmapFactory.decodeFile(path,options);

                                iv.setImageBitmap(newbitb);
                        View.OnClickListener listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ImageView  imview = (ImageView)findViewById(R.id.image);
                                imview.setImageBitmap(null);
                            }
                        };
                        iv.setOnClickListener(listener);



                        String id  = StaticCl.uploadfile(StaticCl.getPublicAlbumDir().getAbsolutePath()+"/new.jpeg",path,false,null);
                        if (id!=null){
                            Log.e("",id);


                        }

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
            case AVATAR_REQUEST:{
                if(resultCode==RESULT_OK){
                    Uri selectImage = imageResultIntent.getData();
                    try{
                        StaticCl.checkPermissions(this);
                        ContentResolver cr = this.getContentResolver();
                        Cursor cursor = cr.query(
                                selectImage,
                                new String[]{MediaStore.Images.Media.DATA},
                                null,
                                null,
                                null
                        );
                        cursor.moveToFirst();
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        Log.e("path",path);
                        if(cursor!=null){
                            cursor.close();
                        }
                        StaticCl.decodeSampleBitmap(new File(StaticCl.getPublicAlbumDir().getAbsolutePath(),"new.jpeg").getAbsolutePath(),path,60,60,30);
                        String id  = StaticCl.uploadfile(StaticCl.getPublicAlbumDir().getAbsolutePath()+"/new.jpeg",path,true,room);
                            StaticCl.setAvatar(room,StaticCl.upload_id);
                            Log.e("",StaticCl.upload_id);




                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }
}


}

