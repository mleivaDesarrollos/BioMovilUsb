package io.apps4u.fpdatabase;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

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

    // Devuelve una lista de fichadas que aun no hayan sido registradas en el servidor
    public ArrayList<SignUp> GetUnregisteredSignups(){
        try{
            // Preparamos el objeto a devolver
            ArrayList<SignUp> lstSignUps = new ArrayList<>();
            // Obtenemos una instancia de consulta de la base de datos
            SQLiteDatabase db = getReadableDatabase();
            // Preparamos un cursor para recorrer los elementos encontrados
            Cursor c = db.rawQuery("SELECT * FROM " + TableDefinition.NAME + " WHERE " + TableDefinition.REGISTERED_ON_SERVER + " LIKE '%false%'", null);
            // Iteramos sobre todos los elementos obtenidos
            while(c.moveToNext()){
                // Generamos una nueva variable signup
                SignUp signToRegister = new SignUp();
                // Procesamos los valores obtenidos y los almacenamos en la entidad a devolver
                signToRegister.set_legajo(c.getString(c.getColumnIndex(TableDefinition.LEGAJO)));
                // Separamos latitud y longitud
                double dblLatitude = c.getDouble(c.getColumnIndex(TableDefinition.LATITUDE));
                double dblLongitude = c.getDouble(c.getColumnIndex(TableDefinition.LONGITUDE));
                // Establecemos las coordenadas nuevas
                signToRegister.set_coordinates(new Coordinate(dblLatitude, dblLongitude));
                // Agregamos los detalles de fichada
                signToRegister.set_details(c.getString(c.getColumnIndex(TableDefinition.DETAILS)));
                // Levantamos la timestamp
                signToRegister.set_timestamp(c.getString(c.getColumnIndex(TableDefinition.TIMESTAMP)));
                // Agregamos el registro al listado
                lstSignUps.add(signToRegister);
            }
            // Cerramos la conexión del cursor
            c.close();
            // Devolvemos una excepción si no se encuentra el recurso
            if(lstSignUps.size() == 0) throw new Resources.NotFoundException();
            // Devolvemos el objeto procesado
            return lstSignUps;
        } catch (Exception e){
            throw e;
        }

    }
}
