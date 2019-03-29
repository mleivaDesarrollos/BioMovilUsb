package io.apps4u.fpmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import io.apps4u.fpdatabase.Company;
import io.apps4u.fpdatabase.CompanyDbHelper;

public class DeleteCompanyFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // Obtenemos el employeeId basado en el bundle
        final String companyID = getArguments().getString("companyID");
        // Generamos un nuevo dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Almacenamos el mensaje en conjunto con el legajo
        String messageDialog = getResources().getString(R.string.dialog_delete);
        messageDialog += "\n" + companyID;
        // Establecemos el mensaje
        builder.setMessage(messageDialog)
                .setNegativeButton(R.string.dialog_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Generamos una nueva instancia de la base de datos
                        CompanyDbHelper cDb = new CompanyDbHelper(getActivity().getApplicationContext());
                        // Instanciamos un nuevo objeto empleado
                        Company companyToDelete = new Company( companyID, null);
                        // Enviamos la petición de eliminación a la base de datos
                        if(cDb.Delete(companyToDelete)){
                            // Recolectamos la actividad actual
                            Activity currentActivity = getActivity();
                            // Validamos si la actividad actual es instancia de
                            if(currentActivity instanceof ActivityShowCompanys){
                                // Obtenemos la instancia de ver empleados de la actividad
                                ActivityShowCompanys updateCompanyList = (ActivityShowCompanys) currentActivity;
                                // Llamamos a la función de repoblar listado
                                updateCompanyList.LoadCompanys();
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