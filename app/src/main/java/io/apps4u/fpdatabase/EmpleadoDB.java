package io.apps4u.fpdatabase;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class EmpleadoDB extends SQLiteOpenHelper {
    public EmpleadoDB(Context context){
        super(context, Database.NAME, null, Database.VERSION);
    }

    public static abstract class TableDefinition implements BaseColumns{
        public static final String NAME ="Employees";
        public static final String FULLNAME = "FULLNAME";
        public static final String LEGAJO = "LEGAJO_ID";
        public static final String FINGERPRINT = "FINGERPRINT";
        public static final String MANAGER_ID = "empresa";
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long saveEmpleado(Empleado empleado) {
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

    public boolean DeleteEmpleado(Empleado empleado){
        // Obtenemos una instancia de lectura de base de datos
        SQLiteDatabase db = getWritableDatabase();
        // Ejecutamos el proceso de eliminación del empleado
        return db.delete(TableDefinition.NAME, "LEGAJO = ?", new String[] {empleado.getLegajo()}) > 0;
    }

    // Actualiza un empleado en la base de datos
    public boolean UpdateEmpleado(Empleado empleado){
        try {
            // Obtenemos una instancia de escritura de base de datos
            SQLiteDatabase db = getWritableDatabase();
            // Ejecutamos la consulta sobre la base de datos
            String queryUpdate = "UPDATE " + TableDefinition.NAME +
                    " SET " + TableDefinition.FULLNAME + "=?, "
                    + TableDefinition.FULLNAME + "=?"
                    + " WHERE " + TableDefinition.LEGAJO + "=?";
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
        Cursor c = db.rawQuery("SELECT * FROM " + TableDefinition.NAME + " WHERE " + TableDefinition.LEGAJO + "=?", new String[]{ employeeId });
        // Recorremos los resultados del cursor
        if(c.moveToNext()) {
            // Almacenamos los resultados de la consulta en strings
            
            String strNombre = c.getString(c.getColumnIndex(TableDefinition.FULLNAME));
            String strHuella = c.getString(c.getColumnIndex(TableDefinition.FINGERPRINT));
            employeeResponse = new Empleado(strEmpresa, strNombre, employeeId, strHuella);
        }
        // Devolvemos el valor procesado
        return employeeResponse;
    }

    public Cursor getTodosEmpleados() {

        return getReadableDatabase().query(
                TableDefinition.NAME,
                null,
                null,null,null,null,null
        );
    }



}
