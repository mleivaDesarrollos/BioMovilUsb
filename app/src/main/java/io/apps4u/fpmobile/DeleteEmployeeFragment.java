package io.apps4u.fpmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class DeleteEmployeeFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // Obtenemos el employeeId basado en el bundle
        final String employeeId = getArguments().getString("employeeId");
        // Generamos un nuevo dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Almacenamos el mensaje en conjunto con el legajo
        String messageDialog = getResources().getString(R.string.dialog_employee_delete);
        messageDialog += "\n" + employeeId;
        // Establecemos el mensaje
        builder.setMessage(messageDialog)
                .setNegativeButton(R.string.dialog_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Generamos una nueva instancia de la base de datos
                        EmpleadosDbHelper empDb = new EmpleadosDbHelper(getActivity().getApplicationContext());
                        // Instanciamos un nuevo objeto empleado
                        Empleado deleteEmployee = new Empleado("", "", employeeId, "");
                        // Enviamos la petición de eliminación a la base de datos
                        if(empDb.DeleteEmpleado(deleteEmployee)){
                            // Recolectamos la actividad actual
                            Activity currentActivity = getActivity();
                            // Validamos si la actividad actual es instancia de VerEmpleados
                            if(currentActivity instanceof VerEmpleados){
                                // Obtenemos la instancia de ver empleados de la actividad
                                VerEmpleados actVerEmp = (VerEmpleados) currentActivity;
                                // Llamamos a la función de repoblar listado
                                actVerEmp.LoadEmpleadosList();
                            }
                        }
                    }
                })
                .setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        // Retornamos el constructor del dialogo
        return builder.create();
    }
}