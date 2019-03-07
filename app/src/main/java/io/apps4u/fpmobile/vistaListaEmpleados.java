package io.apps4u.fpmobile;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class vistaListaEmpleados extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] nombre;
    private final String[] legajo;
    private final String[] imageId;
    public vistaListaEmpleados(Activity context,
                      String[] nombre, String[] imageId, String[] legajo) {
        super(context, R.layout.empreado_list, nombre);
        this.context = context;
        this.nombre = nombre;
        this.imageId = imageId;
        this.legajo = legajo;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.empreado_list, null, true);
        TextView txtTitle = rowView.findViewById(R.id.txtNombre);
        TextView txtLeg = rowView.findViewById(R.id.txtLegajo);
        ImageView imageView = rowView.findViewById(R.id.img);
        txtTitle.setText(nombre[position]);
        txtLeg.setText(legajo[position]);
        byte[] decodedString = Base64.decode(imageId[position], Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);
        return rowView;
    }
}
