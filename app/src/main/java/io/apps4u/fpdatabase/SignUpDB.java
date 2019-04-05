package io.apps4u.fpdatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

public class SignUpDB extends SQLiteOpenHelper {
    public SignUpDB(Context context){
        super(context, Database.NAME, null, Database.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) { }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {    }

    public static abstract class TableDefinition implements BaseColumns {
        public static final String NAME = "SIGNUPS";
        public static final String LEGAJO = "LEGAJO_ID";
        public static final String LATITUDE = "LATITUDE";
        public static final String LONGITUDE = "LONGTIUD";
        public static final String DETAILS = "DETAILS";
        public static final String ADDRESS = "ADDRESS";
        public static final String TIMESTAMP = "TIMESTAMP";
    }
}
