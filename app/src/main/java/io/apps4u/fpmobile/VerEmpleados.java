package io.apps4u.fpmobile;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ListView;

public class VerEmpleados extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_empleados);
        // Obtenemos la instancia de base de datos almacenada en el dispositivo
        EmpleadosDbHelper em = new EmpleadosDbHelper(getApplicationContext());
        // Llamamos a la función para levantar todos los empleados almacenados en la base local
        Cursor c = em.getTodosEmpleados();
        // Generamos un array de tres elementos con cada uno de los componentes
        String nombre[] = new String[c.getCount()];
        String huella[] = new String[c.getCount()];
        String legajo[] = new String[c.getCount()];
        // Iteramos sobre el total de componentes recolectados en la base de datos
        while (c.moveToNext()){
            nombre[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Nombre));
            huella[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Huella));
            legajo[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Legajo));
        }
        // Se genera un adaptador basado la vista de empleados
        vistaListaEmpleados adapter = new
                vistaListaEmpleados(VerEmpleados.this, nombre, huella,legajo);
        // Recolectamos el ListView del activity
        ListView list=findViewById(R.id.lvEmpleados);
        // Establecemos el adaptador sobre el listado
        list.setAdapter(adapter);


    }

}
