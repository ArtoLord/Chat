package com.example.root.forhelp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import org.apache.commons.io.FileUtils;

import com.example.root.forhelp.Config.Config;
import com.example.root.forhelp.Requests.LoginApi;
import com.example.root.forhelp.Requests.RoomAvatar;
import com.example.root.forhelp.Table.Contract;
import com.example.root.forhelp.Table.DbHelper;
import com.github.nkzawa.socketio.client.Socket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StaticCl {

    public static Socket socket;
    public static String upload_id = null;

    public static String EMAIL = "email";
    public static String types;
    public static String JWT = "jwt";
    public static String _ID = "_id";
    public static String IS_ENTER = "is_enter";
    public static ArrayList<ArrayList<String>> items;

    public static void conn(Socket s, String usname) {
        s.emit("conn", usname);
    }

    public static void sendto(Socket s, String msg, String to, int type,String data) {
        s.emit("send to", msg, to, type,data);
    }

    public static void newroom(Socket s, String room, String type) {
        s.emit("new room", room, type);
    }

    public static void toroom(Socket s, String room) {
        s.emit("to the room", room);
    }

    public static void invite(Socket s, String user, String room) {
        s.emit("inwite user", user, room);
    }

    public static void leave(Socket s, String room) {
        s.emit("leave room", room);
    }

    static void insertMessage(String text, String from, String to, String id, long date, String data, Context context) {

        DbHelper mDbHelper = new DbHelper(context);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Создаем объект ContentValues, где имена столбцов ключи,
        // а информация о госте является значениями ключей
        ContentValues values = new ContentValues();
        values.put(Contract.messages.TEXT, text);
        values.put(Contract.messages.TO, to);
        values.put(Contract.messages.FROM, from);
        values.put(Contract.messages.DATE, date);
        values.put(Contract.messages.DATA, data);
        values.put(Contract.messages.MESS_ID, id);
        db.insert(Contract.messages.TABLE_NAME, null, values);
    }

    public static long getLastDate(Context context, String to) {
        DbHelper mDbHelper = new DbHelper(context);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String[] a = {
                " MAX(" + Contract.messages.DATE + ")"
        };
        Cursor cursor = db.query(
                Contract.messages.TABLE_NAME,
                a,
                Contract.messages.TO + " = ?",
                new String[]{to},
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            Log.e("", "" + cursor.getInt(0));
            return cursor.getLong(0);
        } else {
            return 0;
        }
    }


    public static ArrayList<ArrayList<String>> getMessages(Context context, String to, String email) {
        DbHelper mDbHelper = new DbHelper(context);

        // Gets the database in write mode
        String[] a = {Contract.messages.TEXT,
                Contract.messages.FROM,
                Contract.messages.DATA
        };
        String[] toArgs = {to};
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.query(
                Contract.messages.TABLE_NAME,
                a,
                Contract.messages.TO + " = ?",
                toArgs,
                null,
                null,
                null
        );
        ArrayList<ArrayList<String>> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            ArrayList<String> ar = new ArrayList<>();
            String text = cursor.getString(cursor.getColumnIndex(Contract.messages.TEXT));
            String from = cursor.getString(cursor.getColumnIndex(Contract.messages.FROM));
            String data = cursor.getString(cursor.getColumnIndex(Contract.messages.DATA));
            ar.add(text);
            ar.add(from);
            if (from.equals(email)) {
                ar.add("form_to");
            } else {
                if (from.equals("Server")) {
                    ar.add("Server");
                } else {
                    ar.add("to");
                }
            }
            if (data!=null){
            Cursor c = db.query(Contract.images.TABLE_NAME,
                    new String[]{Contract.images.PATH},
                    Contract.images.IM_ID+" = ?",
                    new String[]{data},
                    null,
                    null,
                    null
                    );
            if(c.moveToFirst()){
            String path = c.getString(c.getColumnIndex(Contract.images.PATH));
            Log.e("",path);
            ar.add(path);
            }}
            else{
                ar.add(null);
            }




            items.add(ar);
        }
        return items;
    }
    public static void checkPermissions(Activity context){
        if((ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED)){
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.READ_EXTERNAL_STORAGE)){

            }
            else{
                ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        2);
            }
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    3);
        }
    }
    public static String uploadfile(String path, String old, final boolean is, final String name){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.SocketUrl) //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        LoginApi Login = retrofit.create(LoginApi.class);
        File file = new File(path);
        final RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("picture",new File(old).getName(),requestFile);
        String desc = "Hello from user";
        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"),desc);
        Call<String> call = Login.upload(description,body);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                upload_id = response.body();
                Log.e("",response.body());
                if(is){
                    StaticCl.setAvatar(name,response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Upload error",t.getMessage());

            }
        });
        return upload_id;
    }

    public static void setAvatar(String name,String id){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.url) //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        LoginApi Login = retrofit.create(LoginApi.class);
        Call<ResponseBody> call = Login.setavatar(new RoomAvatar(name,id));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error",t.getMessage());

            }
        });

    }

    public static File getPublicAlbumDir(){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"MyMessenger");
        if(!file.mkdirs()){
        }
        return file;
    }
    public static void insertImage(String id,String path,Context context){
        DbHelper mDbHelper = new DbHelper(context);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Создаем объект ContentValues, где имена столбцов ключи,
        // а информация о госте является значениями ключей
        ContentValues values = new ContentValues();
        values.put(Contract.images.IM_ID, id);
        values.put(Contract.images.PATH, path);
        db.insert(Contract.images.TABLE_NAME, null, values);
    }
    public static void decodeSampleBitmap (String newpath ,String path, int reqWidth, int reqHeight,int qualiti) throws FileNotFoundException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        BitmapFactory.decodeFile(path,options).compress(Bitmap.CompressFormat.JPEG,qualiti,new
                FileOutputStream(newpath));
    }

    public static Bitmap decodeSampleBitmap (String path, int reqWidth, int reqHeight) throws FileNotFoundException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height>reqHeight ||width>reqWidth){
            final  int halfHeight = height/2;
            final int halfWidth = width/2;
            while((halfHeight/inSampleSize)>reqHeight
                    && (halfWidth/inSampleSize)>reqWidth){
                inSampleSize*=2;
            }
        };
        return inSampleSize;
    }
}





