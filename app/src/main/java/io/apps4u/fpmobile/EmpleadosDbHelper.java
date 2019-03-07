package io.apps4u.fpmobile;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class EmpleadosDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Empleados.db";
    public EmpleadosDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Comandos SQL
        db.execSQL("CREATE TABLE " + EmpleadoDB.EmpleadoRow.TABLE_NAME + " ("
                + EmpleadoDB.EmpleadoRow.Nombre + " TEXT NOT NULL,"
                + EmpleadoDB.EmpleadoRow.Legajo + " TEXT NOT NULL,"
                + EmpleadoDB.EmpleadoRow.Huella + " TEXT NOT NULL" + ")");



    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No hay operaciones




    }
    public long saveEmpleado(Empleado empleado) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                EmpleadoDB.EmpleadoRow.TABLE_NAME,
                null,
                empleado.toContentValues());

    }
    public Cursor getTodosEmpleados() {


        return getReadableDatabase().query(
                EmpleadoDB.EmpleadoRow.TABLE_NAME,
                null,
                null,null,null,null,null
        );

    }


}
