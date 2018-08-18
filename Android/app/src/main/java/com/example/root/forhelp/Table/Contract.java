package com.example.root.forhelp.Table;

import android.provider.BaseColumns;

public final class Contract {
    public static final class images implements BaseColumns {
        public final static String TABLE_NAME = "images";
        public final static String _ID = BaseColumns._ID;
        public final static String IM_ID = "image_id";
        public final static String PATH = "path";
    }
    public static final class messages implements BaseColumns {
        public final static String TABLE_NAME = "messages";
        public final static String _ID = BaseColumns._ID;
        public final static String TEXT = "message_text";
        public final static String FROM = "message_from";
        public final static String TO = "message_to";
        public final static String DATE = "date";
        public final static String DATA = "mess_data";
        public final static String MESS_ID = "MESS_ID";
    }
}
