package io.apps4u.fpdatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class CompanyDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Companys.db";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CompanyDB.Entry.TABLE_NAME + "("
        + CompanyDB.Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + CompanyDB.Entry.ID + " TEXT NOT NULL,"
        + CompanyDB.Entry.NAME + " TEXT NOT NULL, "
        + "UNIQUE (" + CompanyDB.Entry.ID +"));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // No hacer modificaciones en cambio de version
    }

    // Instanciamiento de constructor
    public CompanyDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Insertamos una empresa en la base de datos
    public boolean Insert(Company savingCompany){
        try{
            // Obtenemos la instancia de la base de datos
            SQLiteDatabase db = getWritableDatabase();
            // Procesamos la inserción utilizando el método dedicado para esto
            db.insert(CompanyDB.Entry.TABLE_NAME, null, savingCompany.ToContentValues());
            // Si no hay errores, el proceso se completo correctamente
            return true;
        } catch(Exception e){
            // En caso de existir errores se devuelve false
            return false;
        }
    }

    // Eliminamos empresa de la base de datos
    public boolean Delete(Company deletingCompany){
        try{
            // Obtenemos una instancia de la base
            SQLiteDatabase db = getWritableDatabase();
            // Procedemos a realizar la eliminación de la base de datos de la empresa solicitada
            db.delete(CompanyDB.Entry.TABLE_NAME, "ID =?", new String[]{ deletingCompany.get_id()});
            // Devolvemos true si el proceso se completa correctamente
            return true;
        } catch (Exception e){
            // Informamos false si falla
            return false;
        }
    }

    // Devolvemos las companias cargadas en base de datos
    public ArrayList<Company> GetAll(){
        // Generamos el nuevo listado a devolver
        ArrayList<Company> lstCompanies = new ArrayList<>();
        try{
            // Obtenemos una instancia de la variable de base de datos
            SQLiteDatabase db = getReadableDatabase();
            // Devolvemos la consulta
            Cursor c = db.rawQuery("SELECT * FROM " + CompanyDB.Entry.TABLE_NAME, null);
            // Iteramos sobre todos los cursores
            while(c.moveToNext()){
                // Obtenemos los valores de la actual interación
                String strId = c.getString(c.getColumnIndex(CompanyDB.Entry.ID));
                String strName = c.getString(c.getColumnIndex(CompanyDB.Entry.NAME));
                // Generamos una nueva instancia de company
                Company company = new Company(strId, strName);
                // Agregamos la compania al listado a devolver
                lstCompanies.add(company);
            }
            // Devolvemos el valor procesado
            return lstCompanies;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
