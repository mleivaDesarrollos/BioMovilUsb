package io.apps4u.fpdatabase;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Database {
    public static final String NAME = "FingerPrintForU.db";
    public static int VERSION = 1;

    public static final DateFormat FORMATDATE_W4U = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static final DateFormat FORMATDATE_DB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final DateFormat GETTIMEONLY = new SimpleDateFormat("HH:mm");

    // Metodo dispuesto que segun configuracion devuelve el formato requerido para la base de datos
    public static String GetCurrentFormattedDateString(){
        return FORMATDATE_W4U.format(new Date());
    }

    public static String SwitchFromDatabaseToW4UDateformat(String paramSourceDate){
        try{
            // Parseamos a date la fecha en primera instancia
            Date sourceDate = FORMATDATE_DB.parse(paramSourceDate);
            // Devolvemos la fecha en formato string
            return FORMATDATE_W4U.format(sourceDate);
        } catch (Exception e){
            Log.e("ParseDate", e.getMessage());
        }
        return null;
    }

    public static String GetHourOnlyFromDatabaseTime(String paramDBTime){
        try{
            // Levantamos la fecha en primera instancia
            Date sourceDate = FORMATDATE_DB.parse(paramDBTime);
            // Devolvemos solamente la hora
            return GETTIMEONLY.format(sourceDate);
        } catch(Exception e){
            Log.e("ParseDate", e.getMessage());
        }
        return null;
    }

    public static String GetFormattedDate(Date paramDate){
        return FORMATDATE_W4U.format(paramDate);
    }

    public static String GetCurrentFormattedDatabaseDate(){
        return FORMATDATE_DB.format(new Date());
    }
}
