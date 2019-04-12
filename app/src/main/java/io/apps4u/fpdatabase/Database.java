package io.apps4u.fpdatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Database {
    public static final String NAME = "FingerPrintForU.db";
    public static int VERSION = 1;

    public static final DateFormat FORMATDATE_W4U = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // Metodo dispuesto que segun configuracion devuelve el formato requerido para la base de datos
    public static String GetCurrentFormattedDateString(){
        return FORMATDATE_W4U.format(new Date());
    }

    public static String GetFormattedDate(Date paramDate){
        return FORMATDATE_W4U.format(paramDate);
    }
}
