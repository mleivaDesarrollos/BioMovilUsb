package io.apps4u.fpmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

public class GPSRequiredFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        // Levantamos el elemento que construirá el dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Levantamos el mensaje de dialogo
        String message = getResources().getString(R.string.dialog_gps_needed);
        // Establecemos el mensaje
        builder.setMessage(message).setPositiveButton(R.string.dialog_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Id de proceso
                getActivity().finishAffinity();
                getActivity().finish();
                System.exit(0);
            }

        });
        // Devolvemos el builder procesado
        return builder.create();
    }

}
