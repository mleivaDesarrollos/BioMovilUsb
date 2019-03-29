package io.apps4u.fpmobile;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewCompanyList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] company_id;
    private final String[] company_name;

    public ViewCompanyList(Activity context,
                           String[] company_id, String[] company_name) {
        super(context, R.layout.empleado_list, company_name);
        this.context = context;
        this.company_id = company_id;
        this.company_name = company_name;
    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View rowView= inflater.inflate(R.layout.companys_list, null, true);
        TextView txtCompanyID = rowView.findViewById(R.id.lblCompanyID);
        TextView txtCompanyName = rowView.findViewById(R.id.lblCompanyName);
        txtCompanyID.setText(company_id[position]);
        txtCompanyName.setText(company_name[position]);
        // Disponemos de las imagenes de edicion y eliminado para controlar el usuario
        ImageView ivDeleteCompany = rowView.findViewById(R.id.imgDeleteCompany);
        ImageView ivModifyCompany = rowView.findViewById(R.id.imgEditCompany);
        // Configuramos el event listener
        ivDeleteCompany.setOnClickListener(new View.OnClickListener() {
            final int pos = position;
            public void onClick(View v){
                // Separamos el companyID para comunicarseló al dialogo
                String companyID = company_id[pos];
                // Instanciamos una clase de Fragmento
                DeleteCompanyFragment deletingFragment = new DeleteCompanyFragment();
                // Generamos el bundle de información
                Bundle arguments = new Bundle();
                // Anexamos el empleado al bundle
                arguments.putString("companyID", companyID);
                // Pasamos el argumento por parametro
                deletingFragment.setArguments(arguments);
                // Mostramos el fragmento en pantalla
                deletingFragment.show(context.getFragmentManager(), "DeleteEmployee");
            }
        });
        // Controlamos el click de la imagen en modificacion
        ivModifyCompany.setOnClickListener(new View.OnClickListener() {
            final int pos = position;
            @Override
            public void onClick(View view) {
                // Generamos un intent para comunicarle la información requerida a la actividad nueva que se abrira
                Intent intent = new Intent(context, ActivityEmployeeABM.class);
                // Separamos la variable legajo para levantar el empleado en la página de enrolamiento
                String companyID = company_id[pos];
                // Ingresamos el valor del legajo sobre el intent
                intent.putExtra("companyId", companyID);
                // Iniciamos la actividad usando el intent
                context.startActivity(intent);
            }
        });
        return rowView;
    }
}
