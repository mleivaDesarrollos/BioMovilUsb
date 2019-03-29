package io.apps4u.fpmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import io.apps4u.fpdatabase.Company;
import io.apps4u.fpdatabase.CompanyDbHelper;

public class ActivityCompanyABM extends Activity {

    private static boolean modify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_abm);
    }

    // Control de guardado de cambios
    public void btnSaveChangesClick(View view){
        if(modify == false){
            // Recolectamos los items de vistas
            EditText txtNewCompany_id = (EditText) findViewById(R.id.txtNewCompanyID);
            EditText txtNewCompany_name = (EditText) findViewById(R.id.txtNewCompanyName);
            // Recolectamos los strings para guardarlos en variables
            String strId = txtNewCompany_id.getText().toString();
            String strName = txtNewCompany_name.getText().toString();
            // Generamos una nueva instancia de company
            Company newCompany = new Company(strId, strName);
            // Generamos un objeto CompanyHelper
            CompanyDbHelper cDb = new CompanyDbHelper(getApplicationContext());
            // Usando el metodo insert, cargamos una compañia
            cDb.Insert(newCompany);
        } else {

        }
        // Una vez finalizado el procedimiento requerido, se deriva a la actividad correspondiente
        Intent intentToShowCompanies = new Intent(this, ActivityShowCompanys.class);
        // Iniciamos la actividad
        startActivity(intentToShowCompanies);
    }
}
