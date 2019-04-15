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

public class ViewEmployeeList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] nombre;
    private final String[] legajo;
    private final String[] imageId;
    public ViewEmployeeList(Activity context,
                            String[] nombre, String[] imageId, String[] legajo) {
        super(context, R.layout.empleado_list, nombre);
        this.context = context;
        this.nombre = nombre;
        this.imageId = imageId;
        this.legajo = legajo;
    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        final View rowView= inflater.inflate(R.layout.empleado_list, null, true);
        TextView txtTitle = rowView.findViewById(R.id.txtNombre);
        TextView txtLeg = rowView.findViewById(R.id.txtLegajo);
        txtTitle.setText(nombre[position]);
        txtLeg.setText(legajo[position]);
        // Disponemos de las imagenes de edicion y eliminado para controlar el usuario
        ImageView ivDelete = rowView.findViewById(R.id.imgDelete);
        // Configuramos el event listener
        ivDelete.setOnClickListener(new View.OnClickListener() {
            final int pos = position;
            public void onClick(View v){
                // Separamos el employeeId para comunicarseló al dialogo
                String employeeId = legajo[pos];
                // Instanciamos una clase de Fragmento
                DeleteEmployeeFragment deletingFragment = new DeleteEmployeeFragment();
                // Generamos el bundle de información
                Bundle arguments = new Bundle();
                // Anexamos el empleado al bundle
                arguments.putString("employeeId", employeeId);
                // Pasamos el argumento por parametro
                deletingFragment.setArguments(arguments);
                // Mostramos el fragmento en pantalla
                deletingFragment.show(context.getFragmentManager(), "DeleteEmployee");
            }
        });
        return rowView;
    }
}
