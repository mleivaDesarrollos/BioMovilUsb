package io.apps4u.fpdatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context){
        super(context, Database.NAME,  null, Database.VERSION);
        // Para inicializar todas las tablas en un punto unificado, generamos un ayudante por fuera de los demas ayudantes
        getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + ManagerDB.TableDefinition.NAME + "(" +
                ManagerDB.TableDefinition._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ManagerDB.TableDefinition.LEGAJO + " TEXT NOT NULL, " +
                ManagerDB.TableDefinition.FIRSTNAME + " TEXT, " +
                ManagerDB.TableDefinition.LASTNAME + " TEXT, " +
                ManagerDB.TableDefinition.COMPANY_ID + " TEXT NOT NULL, " +
                ManagerDB.TableDefinition.COMPANY_NAME + " TEXT, " +
                ManagerDB.TableDefinition.USERNAME + " TEXT NOT NULL, " +
                ManagerDB.TableDefinition.PASSWORD + " TEXT NOT NULL, " +
                ManagerDB.TableDefinition.TIMESTAMP + " DATE, " +
                "UNIQUE(" + ManagerDB.TableDefinition.LEGAJO + ", " + ManagerDB.TableDefinition.USERNAME + ")" +
                ")");
        // Comandos SQL
        sqLiteDatabase.execSQL("CREATE TABLE " + EmpleadoDB.TableDefinition.NAME + " ("
                + EmpleadoDB.TableDefinition.LEGAJO + " TEXT NOT NULL,"
                + EmpleadoDB.TableDefinition.FULLNAME + " TEXT NOT NULL,"
                + EmpleadoDB.TableDefinition.FINGERPRINT + " TEXT NOT NULL, " +
                EmpleadoDB.TableDefinition.MANAGER_ID + " TEXT NOT NULL, " +
                "PRIMARY KEY ("+ EmpleadoDB.TableDefinition.LEGAJO + "," + EmpleadoDB.TableDefinition.MANAGER_ID + ")," +
                "CONSTRAINT FK_MANAGER " +
                "FOREIGN KEY("+ EmpleadoDB.TableDefinition.MANAGER_ID + ") " +
                "REFERENCES " + ManagerDB.TableDefinition.NAME + "(" + ManagerDB.TableDefinition.LEGAJO + "))");
        sqLiteDatabase.execSQL("CREATE TABLE " + SignUpDB.TableDefinition.NAME + "(" +
                SignUpDB.TableDefinition._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SignUpDB.TableDefinition.LEGAJO + " TEXT NOT NULL, " +
                SignUpDB.TableDefinition.LATITUDE + " TEXT NOT NULL, " +
                SignUpDB.TableDefinition.LONGITUDE + " TEXT NOT NULL, " +
                SignUpDB.TableDefinition.DETAILS + " TEXT NOT NULL," +
                SignUpDB.TableDefinition.ADDRESS + " TEXT NOT NULL, " +
                SignUpDB.TableDefinition.TIMESTAMP + " DATE NOT NULL, " +
                SignUpDB.TableDefinition.REGISTERED_ON_SERVER + " TEXT NOT NULL, " +
                "CONSTRAINT FK_EMPLOYEE FOREIGN KEY(" + SignUpDB.TableDefinition.LEGAJO +") " +
                "REFERENCES " + EmpleadoDB.TableDefinition.NAME + "(" + EmpleadoDB.TableDefinition.LEGAJO + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }
}
