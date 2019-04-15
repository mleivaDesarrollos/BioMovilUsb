package io.apps4u.fpdatabase;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

public class EmpleadoDB extends SQLiteOpenHelper {

    public EmpleadoDB(Context context){
        super(context, Database.NAME, null, Database.VERSION);
    }

    public static abstract class TableDefinition implements BaseColumns{
        public static final String NAME ="Employees";
        public static final String LEGAJO = "LEGAJO_ID";
        public static final String FULLNAME = "FULLNAME";
        public static final String FINGERPRINT = "FINGERPRINT";
        public static final String MANAGER_ID = "MANAGER_ID";
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long Save(Empleado empleado) {
        try{
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            return sqLiteDatabase.insert(
                    TableDefinition.NAME,
                    null,
                    empleado.toContentValues());
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    // Validamos si el legajo está guardado en la base de datos
    public boolean isAlreadySaved(String paramLegajo) {
        // Recolectamos una instancia de la base de datos
        SQLiteDatabase db = getReadableDatabase();
        // Ejecutamos la consulta y validamos la existencia del empleado
        Cursor c = db.rawQuery("SELECT 1 FROM " + TableDefinition.NAME + " WHERE " + TableDefinition.LEGAJO + " =?", new String[]{ paramLegajo});
        if(c.moveToNext()){
            // Si encuentra un registro, el empleado ya existe
            return true;
        }
        // Si el registro no esta, el empleado no esta guardado
        return false;
    }

    public boolean Delete(Empleado empleado){
        // Obtenemos una instancia de lectura de base de datos
        SQLiteDatabase db = getWritableDatabase();
        // Ejecutamos el proceso de eliminación del empleado
        return db.delete(TableDefinition.NAME, TableDefinition.LEGAJO + " = ? AND " + TableDefinition.MANAGER_ID + " = ?", new String[] {empleado.get_legajo(), empleado.get_managerid()}) > 0;
    }

    // Verifica en base de datos si existe un empleado cargado y lo devuelve
    public Empleado GetOne(String employeeId, String managerId){
        // Generamos una nueva instancia de empleado
        Empleado employeeResponse = null;
        // Obtenemos la instancia de lectura de la base
        SQLiteDatabase db = getReadableDatabase();
        // Ejecutamos el query
        Cursor c = db.rawQuery("SELECT * FROM " + TableDefinition.NAME + " WHERE " + TableDefinition.LEGAJO + "= ? AND " + TableDefinition.MANAGER_ID + " = ?", new String[]{ employeeId, managerId });
        // Recorremos los resultados del cursor
        if(c.moveToNext()) {
            // Almacenamos los resultados de la consulta en strings
            String strNombre = c.getString(c.getColumnIndex(TableDefinition.FULLNAME));
            String strHuella = c.getString(c.getColumnIndex(TableDefinition.FINGERPRINT));
            employeeResponse = new Empleado();
            employeeResponse.set_legajo(employeeId);
            employeeResponse.set_fullname(strNombre);
            employeeResponse.set_managerid(managerId);
            employeeResponse.set_fingerprint(strHuella);
        }
        // Devolvemos el valor procesado
        return employeeResponse;
    }

    // Devuelve todos los empleados cargados según manager
    public ArrayList<Empleado> GetAll(String managerId) {
        // Generamos el nuevo listado de empleados
        ArrayList<Empleado> lstEmpleados = null;
        try{
            // Obtenemos una instancia de lectura de base de datos
            SQLiteDatabase db = getReadableDatabase();
            // Realizamos la consulta y la almacenamos en un cursor
            Cursor c = db.rawQuery("SELECT * FROM " + TableDefinition.NAME + " WHERE " + TableDefinition.MANAGER_ID + " = ? ", new String[]{managerId});
            // Iniciamos una nueva instancia de base de datos
            lstEmpleados = new ArrayList<>();
            // Iteramos sobre todos los resultados
            while(c.moveToNext()){
                // Generamos un nuevo empleado
                Empleado fetchEmployee = new Empleado();
                // Almacenamos todos los valores obtenidos
                fetchEmployee.set_managerid(managerId);
                fetchEmployee.set_legajo(c.getString(c.getColumnIndex(TableDefinition.LEGAJO)));
                fetchEmployee.set_fullname(c.getString(c.getColumnIndex(TableDefinition.FULLNAME)));
                fetchEmployee.set_fingerprint(c.getString(c.getColumnIndex(TableDefinition.FINGERPRINT)));
                // Agregamos el empleado a la lista
                lstEmpleados.add(fetchEmployee);
            }
        } catch (Exception e){
            Log.e("GetAllEmployee", e.getMessage());
        }
        // Devolvemos el valor procesado
        return lstEmpleados;
    }
}
