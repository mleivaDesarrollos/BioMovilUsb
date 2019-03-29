package io.apps4u.fpmobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import io.apps4u.fpdatabase.Company;
import io.apps4u.fpdatabase.CompanyDbHelper;

public class ActivityShowCompanys extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_companys);
        // Cargamos las compañias
        LoadCompanys();
    }

    // Carga las empresas almacenadas en base de datos y renderiza la vista
    public void LoadCompanys(){
        // Cargamos el ayudante de base de datos
        CompanyDbHelper cDb = new CompanyDbHelper(getApplicationContext());
        // Obtenemos la lista de compañias
        ArrayList<Company> lstCompanies = cDb.GetAll();
        // Procemos con array cada uno de los elementos del listado
        String[] arrID = new String[lstCompanies.size()];
        String[] arrName = new String[lstCompanies.size()];
        // Iteramos sobre todos las compañias recibidas
        for(int companyIndex = 0; companyIndex < lstCompanies.size(); companyIndex++){
            // Agregamos el valor de compañia a cada uno de los arrays
            arrID[companyIndex] = lstCompanies.get(companyIndex).get_id();
            arrName[companyIndex] = lstCompanies.get(companyIndex).get_name();
        }
        // Generamos un adaptador de arrays para la vista
        ViewCompanyList companyListAdapter = new ViewCompanyList(this, arrID, arrName);
        // Recolectamos la vista
        ListView lvCompanys = (ListView) findViewById(R.id.lstCompanys);
        // Vaciamos el actual adaptador
        lvCompanys.setAdapter(null);
        // Cargamos el adaptador del listview con los datos
        lvCompanys.setAdapter(companyListAdapter);
    }
}
