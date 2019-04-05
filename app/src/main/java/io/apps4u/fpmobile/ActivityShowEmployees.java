package io.apps4u.fpmobile;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ListView;

import io.apps4u.fpdatabase.EmpleadoDB;

public class ActivityShowEmployees extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_employees);
        // Cargamos la lista de empleados
        LoadEmpleadosList();
    }

    public void LoadEmpleadosList(){
        // Obtenemos la instancia de base de datos almacenada en el dispositivo
        EmpleadoDB em = new EmpleadoDB(getApplicationContext());
        // Llamamos a la función para levantar todos los empleados almacenados en la base local
        Cursor c = em.getTodosEmpleados();
        // Generamos un array de tres elementos con cada uno de los componentes
        String nombre[] = new String[c.getCount()];
        String huella[] = new String[c.getCount()];
        String legajo[] = new String[c.getCount()];
        // Iteramos sobre el total de componentes recolectados en la base de datos
        while (c.moveToNext()){
            nombre[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.TableDefinition.Nombre));
            huella[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.TableDefinition.FINGERPRINT));
            legajo[c.getPosition()] = c.getString(c.getColumnIndex(EmpleadoDB.TableDefinition.LEGAJO));
        }
        // Se genera un adaptador basado la vista de empleados
        ViewEmployeeList adapter = new
                ViewEmployeeList(ActivityShowEmployees.this, nombre, huella,legajo);
        // Recolectamos el ListView del activity
        ListView list=findViewById(R.id.lvEmpleados);
        // Limpiamos el Adaptador del listado
        list.setAdapter(null);
        // Establecemos el adaptador sobre el listado
        list.setAdapter(adapter);
    }

}
