package io.apps4u.fpmobile;
import android.provider.BaseColumns;

public class EmpleadoDB {
    public static abstract class EmpleadoRow implements BaseColumns{
        public static final String TABLE_NAME ="empleados";
        public static final String Nombre = "nombre";
        public static final String Legajo = "legajo";
        public static final String Huella = "huella";

    }



}