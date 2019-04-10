package io.apps4u.fpdatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

public class SignUpDB extends SQLiteOpenHelper {
    public SignUpDB(Context context){
        super(context, Database.NAME, null, Database.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) { }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {    }

    public static abstract class TableDefinition implements BaseColumns {
        public static final String NAME = "Signups";
        public static final String LEGAJO = "LEGAJO_ID";
        public static final String LATITUDE = "LATITUDE";
        public static final String LONGITUDE = "LONGITUDE";
        public static final String DETAILS = "DETAILS";
        public static final String ADDRESS = "ADDRESS";
        public static final String TIMESTAMP = "TIMESTAMP";
        public static final String REGISTERED_ON_SERVER = "REGISTERED_ON_SERVER";
    }

    // Método que almacena una fichada
    public void Add(SignUp signUpData){
        try{
            // Recolectamos la instancia escritura de la base de datos
            SQLiteDatabase db = getWritableDatabase();
            // Levantamos los parametros
            String params[] = new String[] {
                    signUpData.get_legajo(), // 0
                    Double.toString(signUpData.get_coordinates().get_latitude()), // 1
                    Double.toString(signUpData.get_coordinates().get_longitude()), // 2
                    signUpData.get_details(), // 3
                    signUpData.get_address(), // 4
                    signUpData.get_timestamp(), // 5
                    Boolean.toString(signUpData.is_registered_on_server()) // 6
            };
            // Ejecutamos la consulta contra la base de datos
            db.execSQL("INSERT INTO " + TableDefinition.NAME + "("
                    + TableDefinition.LEGAJO + ", "
                    + TableDefinition.LATITUDE + ", "
                    + TableDefinition.LONGITUDE + ", "
                    + TableDefinition.DETAILS + ", "
                    + TableDefinition.ADDRESS + ", "
                    + TableDefinition.TIMESTAMP + ", "
                    + TableDefinition.REGISTERED_ON_SERVER + ") VALUES(?, ?, ?, ?, ?, ?, ?)", params);
        } catch(Exception e){
            Log.e("AddSignup", e.getMessage());
        }
    }
}
