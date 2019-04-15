package io.apps4u.fpmobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

import io.apps4u.fpdatabase.Manager;
import io.apps4u.fpdatabase.SignUp;
import io.apps4u.fpdatabase.SignUpDB;

public class ActivityShowSignups extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_signups);
    }

    private void LoadSignupListOfToday(){
        // Levantamos el administrador que se encuentra logueado en la session
        Manager loggedManager = ((Session) getApplication()).loggedManager;
        // Generamos una instancia de consulta de base de datos de fichadas
        SignUpDB sDB = new SignUpDB(getApplicationContext());
        // Solicitamos el listado de fichadas del dia
        List<SignUp> lstSignups = sDB.GetTodaySignups(loggedManager);
        // Levantamos la lista de fichadas
        ListView lvSignups = findViewById(R.id.lsvTodaySignups);
        // TODO iterar sobre el listado de fichadas obtenidas y pasar los parametros para que lo verifique el adaptador
    }
}
