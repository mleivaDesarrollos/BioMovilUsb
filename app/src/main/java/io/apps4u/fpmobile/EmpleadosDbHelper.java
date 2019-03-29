package io.apps4u.fpmobile;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


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
                + EmpleadoDB.EmpleadoRow.Empresa + " TEXT NOT NULL,"
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

    public boolean DeleteEmpleado(Empleado empleado){
        // Obtenemos una instancia de lectura de base de datos
        SQLiteDatabase db = getWritableDatabase();
        // Ejecutamos el proceso de eliminación del empleado
        return db.delete(EmpleadoDB.EmpleadoRow.TABLE_NAME, "Legajo = ?", new String[] {empleado.getLegajo()}) > 0;
    }

    // Actualiza un empleado en la base de datos
    public boolean UpdateEmpleado(Empleado empleado){
        try {
            // Obtenemos una instancia de escritura de base de datos
            SQLiteDatabase db = getWritableDatabase();
            // Ejecutamos la consulta sobre la base de datos
            String queryUpdate = "UPDATE " + EmpleadoDB.EmpleadoRow.TABLE_NAME +
                    " SET " + EmpleadoDB.EmpleadoRow.Nombre + "=?, "
                    + EmpleadoDB.EmpleadoRow.Empresa + "=?"
                    + " WHERE " + EmpleadoDB.EmpleadoRow.Legajo + "=?";
            // Preparamos el array de string con parametros
            String[] empleadoParameters = new String[] {empleado.getNombre(), empleado.getEmpresa(), empleado.getLegajo()};
            // Ejecutamos la consulta sobre la base de datos
            db.execSQL(queryUpdate, empleadoParameters);
            return true;
        } catch(Exception e){
            return false;
        }
    }

    // Verifica en base de datos si existe un empleado cargado y lo devuelve
    public Empleado GetEmpleado(String employeeId){
        // Generamos una nueva instancia de empleado
        Empleado employeeResponse = null;
        // Obtenemos la instancia de lectura de la base
        SQLiteDatabase db = getReadableDatabase();
        // Ejecutamos el query
        Cursor c = db.rawQuery("SELECT * FROM " + EmpleadoDB.EmpleadoRow.TABLE_NAME + " WHERE " + EmpleadoDB.EmpleadoRow.Legajo + "=?", new String[]{ employeeId });
        // Recorremos los resultados del cursor
        if(c.moveToNext()) {
            // Almacenamos los resultados de la consulta en strings
            String strEmpresa = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Empresa));
            String strNombre = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Nombre));
            String strHuella = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Huella));
            employeeResponse = new Empleado(strEmpresa, strNombre, employeeId, strHuella);
        }
        // Devolvemos el valor procesado
        return employeeResponse;
    }

    public Cursor getTodosEmpleados() {

        return getReadableDatabase().query(
                EmpleadoDB.EmpleadoRow.TABLE_NAME,
                null,
                null,null,null,null,null
        );
    }


}
