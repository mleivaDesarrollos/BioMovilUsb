package io.apps4u.fpmobile;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ViewSignupAdapter extends ArrayAdapter<String> {
    private String[] _fullnames;
    private String[] _signups;
    private Activity _parentActivity;

    // Adaptador para generar el listado de fichadas del dia de la fecha
    public ViewSignupAdapter(Activity parentActivity, String[] paramFullnames, String[] paramSignups){
        // Llamamos al padre de la herencia
        super(parentActivity, R.layout.layout_signups_list, paramFullnames);
        // Guardamos todos los parametros en sus respectivos lugares
        _fullnames = paramFullnames;
        _signups = paramSignups;
        _parentActivity = parentActivity;
    }

    // Al solicitar la vista
    @Override
    public View getView(final int position, View view, ViewGroup parent){
        LayoutInflater inflater = _parentActivity.getLayoutInflater();
        final View rowview = inflater.inflate(R.layout.layout_signups_list, null, true);
        // Obtenemos los elementos de la interfaz
        TextView txtSignupListFullname = rowview.findViewById(R.id.txtSignupListFullname);
        TextView txtSignupListTimestamp = rowview.findViewById(R.id.txtSignupListTimestamp);
        // Establecemos el texto en el campo correspondiente
        txtSignupListFullname.setText(_fullnames[position]);
        txtSignupListTimestamp.setText(_signups[position]);
        // Devolvemos la fila nueva procesada
        return rowview;
    }
}
