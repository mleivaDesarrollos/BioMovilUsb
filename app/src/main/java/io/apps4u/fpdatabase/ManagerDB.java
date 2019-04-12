package io.apps4u.fpdatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Base64;
import android.util.Log;

import java.util.ArrayList;

public class ManagerDB extends SQLiteOpenHelper {
    public ManagerDB(Context context){
        super(context, Database.NAME, null, Database.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) { }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { }

    public static abstract class TableDefinition implements BaseColumns {
        public static final String NAME = "Managers";
        public static final String LEGAJO = "LEGAJO_ID";
        public static final String FIRSTNAME = "FIRSTNAME";
        public static final String LASTNAME = "LASTNAME";
        public static final String COMPANY_ID = "COMPANY_ID";
        public static final String COMPANY_NAME = "COMPANY_NAME";
        public static final String USERNAME = "USERNAME";
        public static final String PASSWORD = "PASSWORD";
    }

    public ArrayList<Manager> GetAll(){
        // Preparamos el array que se devolverá en el proceso
        ArrayList<Manager> lstManangers = new ArrayList<>();
        try{
            // Obtenemos una instancia de la base de datos
            SQLiteDatabase db = getReadableDatabase();
            // Iteramos sobre todos los cursores
            Cursor c = db.rawQuery("SELECT * FROM " + TableDefinition.NAME, null);
            while(c.moveToNext()){
                // Generamos un nuevo Manager
                Manager newManager = new Manager();
                // Agregamos los parametros
                newManager.set_legajoId(c.getString(c.getColumnIndex(TableDefinition.LEGAJO)));
                newManager.set_firstname(c.getString(c.getColumnIndex(TableDefinition.FIRSTNAME)));
                newManager.set_companyId(c.getString(c.getColumnIndex(TableDefinition.COMPANY_ID)));
                newManager.set_companyName(c.getString(c.getColumnIndex(TableDefinition.COMPANY_NAME)));
                newManager.set_username(c.getString(c.getColumnIndex(TableDefinition.USERNAME)));
                String password = c.getString(c.getColumnIndex(TableDefinition.PASSWORD));
                password = new String(Base64.decode(password, Base64.DEFAULT), "UTF-8");
                newManager.set_password(password);
                // Agregamos el maanger a la base de datos
                lstManangers.add(newManager);
            }
        } catch(Exception e){
            Log.e("ManagerDBHelperError", e.getMessage());
            lstManangers = null;
        }
        // Devolvemos el valor procesado de la lista
        return lstManangers;
    }

    // Metodo que devuelve un administrador, devuelve null si no encuentra ninguno
    public Manager GetByUsername(String paramUsername, String paramPassword) {
        // Generamos un nuevo elemento manager para devolver en el proceso
        Manager manager = null;
        // Obtenemos una instancia de lectura de base de datos
        SQLiteDatabase db = getReadableDatabase();
        // Encriptamos la contraseña
        String encriptedPassword = Base64.encodeToString(paramPassword.getBytes(), Base64.DEFAULT);
        // Ejecutamos la consulta a la base de datos
        Cursor c = db.rawQuery("SELECT * FROM " + TableDefinition.NAME + " where " + TableDefinition.USERNAME + "=? and "+ TableDefinition.PASSWORD + " =?", new String[] {paramUsername, encriptedPassword});
        if(c.moveToNext()) {
            // Generamos una nueva instancia de Manager
            manager = new Manager();
            // Cargamos los datos del administrador
            manager.set_legajoId(c.getString(c.getColumnIndex(TableDefinition.LEGAJO)));
            manager.set_firstname(c.getString(c.getColumnIndex(TableDefinition.FIRSTNAME)));
            manager.set_lastname(c.getString(c.getColumnIndex(TableDefinition.LASTNAME)));
            manager.set_companyId(c.getString(c.getColumnIndex(TableDefinition.COMPANY_ID)));
            manager.set_companyName(c.getString(c.getColumnIndex(TableDefinition.COMPANY_NAME)));
            manager.set_username(paramUsername);
            manager.set_password(paramPassword);
        }
        // Devolvemos el valor procesado
        return manager;
    }

    public boolean IsAlreadySaved(String paramUsername){
        // Obtenemos un elemento SQLite a modo lectura
        SQLiteDatabase db = getReadableDatabase();
        // Preparamos la ejecución del query
        Cursor c = db.rawQuery("SELECT 1 FROM " + TableDefinition.NAME + " WHERE " + TableDefinition.USERNAME + "=?", new String[]{paramUsername});
        // Si hay un cursor para iterar significa que se ha encontrado el usuario
        if(c.moveToNext())return true;
        else return false;
    }

    public void Update(Manager managerToUpdate){
        // Obtenemos un elemento de escritura de base de datos
        SQLiteDatabase db = getWritableDatabase();
        // Encriptamos la contraseña previa a la carga de datos
        String encodedPassword = Base64.encodeToString(managerToUpdate.get_password().getBytes(), Base64.DEFAULT);
        // Preparamos los parametros a ingresar
        String[] params = new String[]{
                managerToUpdate.get_firstname(),
                managerToUpdate.get_lastname(),
                managerToUpdate.get_companyId(),
                managerToUpdate.get_companyName(),
                encodedPassword,
                managerToUpdate.get_username()
        };
        // Ejecutamos el SQL
        db.execSQL("UPDATE " + TableDefinition.NAME + " SET "
                + TableDefinition.FIRSTNAME + "=?, "
                + TableDefinition.LASTNAME  + "=?, "
                + TableDefinition.COMPANY_ID + "=?, "
                + TableDefinition.COMPANY_NAME + "=?, "
                + TableDefinition.PASSWORD + "=? "
                + " WHERE " + TableDefinition.USERNAME + "=?", params);
    }

    public void Add(Manager paramManager){
        // Obtenemos un valor escribible de base de datos
        SQLiteDatabase db = getWritableDatabase();
        // Controlamos la seccion de codigo
        try{
            // Convertimos el password plano a Base64
            String encodedPassword = Base64.encodeToString(paramManager.get_password().getBytes(), Base64.DEFAULT);
            // Preparamos todos los parametros a ingresar
            String[] params = new String[]{
                    paramManager.get_legajoId(), // 0
                    paramManager.get_firstname(), // 1
                    paramManager.get_lastname(), // 2
                    paramManager.get_companyId(), // 3
                    paramManager.get_companyName(), // 4
                    paramManager.get_username(), // 5
                    encodedPassword // 6
            };
            // Ejecutamos el Query
            db.execSQL("INSERT INTO " + TableDefinition.NAME + " " +
                    "(" + TableDefinition.LEGAJO + ","
                    + TableDefinition.FIRSTNAME + ", "
                    + TableDefinition.LASTNAME + ", "
                    + TableDefinition.COMPANY_ID + ", "
                    + TableDefinition.COMPANY_NAME + ", "
                    + TableDefinition.USERNAME + ", "
                    + TableDefinition.PASSWORD +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?)", params);
        } catch(Exception e){
            Log.e("AddManagerError", e.getMessage());
        }
    }
}
