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

        EmpleadosDbHelper em = new EmpleadosDbHelper(getApplicationContext());
        Cursor c = em.getTodosEmpleados();
        String  nombre[] = new String[c.getCount()];
        String  huella[] = new String[c.getCount()];
        String legajo[] = new String[c.getCount()];
        while (c.moveToNext()){
            nombre[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Nombre));
            huella[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Huella));
            legajo[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Legajo));
        }

        vistaListaEmpleados adapter = new
                vistaListaEmpleados(VerEmpleados.this, nombre, huella,legajo);
        ListView list=findViewById(R.id.empleados_list);
        list.setAdapter(adapter);


    }

}
