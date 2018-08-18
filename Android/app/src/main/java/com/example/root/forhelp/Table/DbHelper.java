package com.example.root.forhelp.Table;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = DbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "rooms.db";
    public static final int DATABASE_VERSION = 8;


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_MESSAGES = "CREATE TABLE " + Contract.messages.TABLE_NAME + " ("
                + Contract.messages._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Contract.messages.TO + " TEXT NOT NULL, "
                + Contract.messages.FROM + " TEXT NOT NULL, "
                + Contract.messages.MESS_ID + " TEXT NOT NULL UNIQUE, "
                + Contract.messages.DATE + " INTEGER NOT NULL, "
                + Contract.messages.DATA + " TEXT , "
                + Contract.messages.TEXT + " TEXT NOT NULL );";

        // Запускаем создание таблицы
        db.execSQL(SQL_MESSAGES);

        String SQL_IMAGES = "CREATE TABLE " + Contract.images.TABLE_NAME + " ("
                + Contract.images._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Contract.images.IM_ID + " TEXT NOT NULL, "
                + Contract.images.PATH + " TEXT NOT NULL); ";


        // Запускаем создание таблицы
        db.execSQL(SQL_IMAGES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Запишем в журнал
        Log.w("SQLite", "Обновляемся с версии " + i + " на версию " + i1);

        // Удаляем старую таблицу и создаём новую
        db.execSQL("DROP TABLE IF EXISTS " + Contract.messages.TABLE_NAME);
        // Создаём новую таблицу
        onCreate(db);

    }
}
