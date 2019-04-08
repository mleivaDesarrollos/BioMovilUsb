package io.apps4u.fpmobile;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ListView;

import java.util.ArrayList;

import io.apps4u.fpdatabase.Empleado;
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
        // Levantamos el objeto session almacenado en aplicacion
        Session sessionInfo = (Session) getApplication();
        // Llamamos a la función para levantar todos los empleados almacenados en la base local
        ArrayList<Empleado> lstEmpleados = em.GetAll(sessionInfo.loggedManager.get_legajoId());
        // Recolectamos el ListView del activity
        ListView list=findViewById(R.id.lvEmpleados);
        // Disponemos las variables para modificar
        if(lstEmpleados != null){
            // Generamos un array de tres elementos con cada uno de los componentes
            String nombre[] = new String[lstEmpleados.size()];
            String huella[] = new String[lstEmpleados.size()];
            String legajo[] = new String[lstEmpleados.size()];
            // Iteramos sobre el total de componentes recolectados en la base de datos
            for(int empleadoIndex = 0; empleadoIndex < lstEmpleados.size(); empleadoIndex++){
                nombre[empleadoIndex] = lstEmpleados.get(empleadoIndex).get_fullname();
                legajo[empleadoIndex] = lstEmpleados.get(empleadoIndex).get_legajo();
            }
            // Se genera un adaptador basado la vista de empleados
            ViewEmployeeList adapter = new
                    ViewEmployeeList(ActivityShowEmployees.this, nombre, huella,legajo);
            // Limpiamos el Adaptador del listado
            list.setAdapter(null);
            // Establecemos el adaptador sobre el listado
            list.setAdapter(adapter);
        } else {
            // Limpiamos el Adaptador del listado
            list.setAdapter(null);
        }
    }

}
