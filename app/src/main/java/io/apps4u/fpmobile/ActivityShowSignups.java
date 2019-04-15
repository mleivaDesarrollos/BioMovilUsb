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
        LoadSignupListOfToday();
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
        if(lstSignups != null){
            // Preparamos los arrays de strings
            String lstFullnames[] = new String[lstSignups.size()];
            String lstTimes[] = new String[lstSignups.size()];
            // Iteramos sobre el listado completo
            for(int signupsIndex = 0; signupsIndex < lstSignups.size(); signupsIndex++ ){
                // Almacenamos los nombres y horarios
                lstFullnames[signupsIndex] = lstSignups.get(signupsIndex).get_empleado().get_fullname();
                lstTimes[signupsIndex] = lstSignups.get(signupsIndex).get_timestamp();
            }
            // Creamos un adaptador
            ViewSignupAdapter adapter = new ViewSignupAdapter(this, lstFullnames, lstTimes);
            // Vaciamos el listado que exista de fichadas
            lvSignups.setAdapter(null);
            // Disponemos el nuevo adaptador
            lvSignups.setAdapter(adapter);
        } else {
            // Establecemos el adaptador a nulo
            lvSignups.setAdapter(null);
        }
    }
}
