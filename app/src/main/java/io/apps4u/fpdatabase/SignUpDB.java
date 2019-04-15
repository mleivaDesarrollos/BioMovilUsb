package io.apps4u.fpdatabase;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.apps4u.fpmobile.Session;

public class SignUpDB extends SQLiteOpenHelper {

    private Context _context;


    public SignUpDB(Context context){
        super(context, Database.NAME, null, Database.VERSION);
        _context = context;
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
                    signUpData.get_empleado().get_legajo(), // 0
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

    // Registramos una fichada como ya enviada a los servidores en la base local
    public void SetRegistered(SignUp paramSignUp) throws Exception{
        // Validamos si el elemento viene con los valores requeridos
        if(paramSignUp == null || paramSignUp.get_id() < 0) throw new Exception("Error al registrar fichada exitosa en base local. La fichada es nula o el id es incorrecto.");
        // Levantamos una instancia de escritura en base de datos
        SQLiteDatabase db = getWritableDatabase();
        // Ejecutamos la actualizacion sobre la base de datos
        db.execSQL("UPDATE " + TableDefinition.NAME
                + " SET " + TableDefinition.REGISTERED_ON_SERVER
                + "='true' WHERE " + TableDefinition._ID + "=?", new String[]{Integer.toString(paramSignUp.get_id())});
    }

    // Metodo que devuelve un listado correspondiente a las fichadas del dia de la fecha
    public ArrayList<SignUp> GetTodaySignups(Manager paramManager){
        // Preparamos el array a devolver
        ArrayList<SignUp> lstSignupsOfToday = null;
        try{
            // Obtenemos una instancia de lectura de base de datos
            SQLiteDatabase db = getReadableDatabase();
            // Ejecutamos la consulta sobre la base de datos
            Cursor c = db.rawQuery("SELECT emps." + EmpleadoDB.TableDefinition.FULLNAME + " as employee_fullname, " +
                    "sgps." + TableDefinition.TIMESTAMP +" as signup_time FROM " + TableDefinition.NAME + " as sgps" +
                    " INNER JOIN " + EmpleadoDB.TableDefinition.NAME + " as emps " +
                    " ON sgps." + TableDefinition.LEGAJO + " = emps." + EmpleadoDB.TableDefinition.LEGAJO +
                    " WHERE strftime('%Y-%m-%d', sgps." + TableDefinition.TIMESTAMP + ") = date('now', 'localtime') AND " +
                    " emps." + EmpleadoDB.TableDefinition.MANAGER_ID + " IN " +
                        " (SELECT " + ManagerDB.TableDefinition.LEGAJO +" FROM "
                        + ManagerDB.TableDefinition.NAME + " WHERE " + ManagerDB.TableDefinition.COMPANY_ID + " = " + paramManager.get_companyId() +")", null);
            // Generamos una nueva instancia del listado
            lstSignupsOfToday = new ArrayList<>();
            // Iteramos sobre el cursor
            while(c.moveToNext()){
                // Generamos una nueva instancia de fichada
                SignUp todaySignup = new SignUp();
                // Generamos una nueva instancia de empleado
                Empleado sgnEmployee = new Empleado();
                // Establecemos el nombre del empleado
                sgnEmployee.set_fullname(c.getString(c.getColumnIndex("employee_fullname")));
                // Establecemos el horario de fichada
                todaySignup.set_timestamp(c.getString(c.getColumnIndex("signup_time")));
                // Ubicamos el empleado dentro del item de fichado
                todaySignup.set_empleado(sgnEmployee);
                // Agregamos la fichada al listado
                lstSignupsOfToday.add(todaySignup);
            }
        } catch(Exception e){
            Log.e("TodaySignup", e.getMessage());
        }
        // Devolvemos el listado procesado
        return lstSignupsOfToday;
    }

    // Devuelve una lista de fichadas que aun no hayan sido registradas en el servidor
    public ArrayList<SignUp> GetUnregisteredSignups(Manager paramManager){
        try{
            // Preparamos el objeto a devolver
            ArrayList<SignUp> lstSignUps = new ArrayList<>();
            // Obtenemos una instancia de consulta de la base de datos
            SQLiteDatabase db = getReadableDatabase();
            // Preparamos un cursor para recorrer los elementos encontrados
            Cursor c = db.rawQuery("SELECT * FROM " + TableDefinition.NAME + " as sgps " +
                    "INNER JOIN " + EmpleadoDB.TableDefinition.NAME + " as emps " +
                    "ON sgps." + TableDefinition.LEGAJO + " = emps." + EmpleadoDB.TableDefinition.LEGAJO +
                    " WHERE " + TableDefinition.REGISTERED_ON_SERVER + " LIKE '%false%' AND " +
                    " emps." + EmpleadoDB.TableDefinition.MANAGER_ID + "=" + paramManager.get_legajoId(), null);
            // Iteramos sobre todos los elementos obtenidos
            while(c.moveToNext()){
                // Generamos una nueva variable signup
                SignUp signToRegister = new SignUp();
                // Levantamos el ID unico de la fichada
                signToRegister.set_id(c.getInt(c.getColumnIndex(TableDefinition._ID)));
                // Procesamos los valores obtenidos y los almacenamos en la entidad a devolver
                Empleado employee = new Empleado();
                employee.set_legajo(c.getString(c.getColumnIndex(TableDefinition.LEGAJO)));
                employee.set_managerid(paramManager.get_legajoId());
                // Guardamos el empleado en la entidad firma
                signToRegister.set_empleado(employee);
                // Separamos latitud y longitud
                double dblLatitude = c.getDouble(c.getColumnIndex(TableDefinition.LATITUDE));
                double dblLongitude = c.getDouble(c.getColumnIndex(TableDefinition.LONGITUDE));
                // Establecemos las coordenadas nuevas
                signToRegister.set_coordinates(new Coordinate(dblLatitude, dblLongitude));
                // Levantamos la direccion de fichada
                signToRegister.set_address(c.getString(c.getColumnIndex(TableDefinition.ADDRESS)));
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
