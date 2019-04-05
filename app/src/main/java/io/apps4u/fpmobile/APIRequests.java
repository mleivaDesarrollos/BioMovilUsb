package io.apps4u.fpmobile;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.apps4u.fpdatabase.Empleado;
import io.apps4u.fpdatabase.Manager;
import io.apps4u.fpdatabase.SignUp;

public class APIRequests {
    private static final String LOGIN_EMAIL = "email";
    private static final String LOGIN_PASSWORD = "password";

    public static Manager Login(String paramUsername, String paramPassword){
        // Preparamos el objeto de retorno
        Manager loggedManager = null;
        try{
            // Cargamos la dirección URL que se utilizará para enviar
            URL url = new URL("https://find4u.apps4u.io/api/v4/login");
            // Preparamos la conexión
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Disponemos del método de conexión
            conn.setRequestMethod("POST");
            // Configuramos las cabeceras
            conn.setRequestProperty("Content-Type", "application/json");
            // Generamos el objeto JSON para Procesarlo en retorno
            JSONObject jsonData = new JSONObject();
            // Cargamos los valores en el objeto JSON
            jsonData.put(LOGIN_EMAIL, paramUsername);
            jsonData.put(LOGIN_PASSWORD, paramPassword);
            // Generamos un objeto DataOutputStream
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            // Escribimos los bytes de los datos en JSON
            os.writeBytes(jsonData.toString());
            os.flush();
            os.close();
            int status = conn.getResponseCode();
            switch(status) {
                case 200:
                case 201:
                    // En caso de recibir la respuesta, procedemos a generar el objeto sessiondata
                    loggedManager = new Manager();
                    // Realizamos la lectura de la información recibida
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    // Parseamos el JSON
                    JSONObject jsonRead = new JSONObject(sb.toString());
                    // Separamos el nombre completo y lo disponemos
                    String strFullname = jsonRead.getString("nombre");
                    String[] splitedFullname = strFullname.split(" ");
                    String strFirstname = splitedFullname[0];
                    String strLastname = splitedFullname[splitedFullname.length - 1];
                    // Conseguimos el nombre de la empresa y el id
                    String strCompanyId = jsonRead.getString("empresaid");
                    String strCompanyName = jsonRead.getJSONObject("empresa").getString("nombre");
                    // Levantamos el legajo
                    String strLegajo = jsonRead.getString("legajo");
                    // Cargamos los datos del usuario en la variable de Administrador
                    loggedManager.set_username(paramUsername);
                    loggedManager.set_password(paramPassword);
                    loggedManager.set_legajoId(strLegajo);
                    loggedManager.set_firstname(strFirstname);
                    loggedManager.set_lastname(strLastname);
                    loggedManager.set_companyId(strCompanyId);
                    loggedManager.set_companyName(strCompanyName);
                    break;
            }
        }catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
        // Devolvemos el valor procesado
        return loggedManager;
    }

    public static void Enroll(SignUp signUpData){

    }

    // Consulta a la API por la existencia del empleado. Devuelve nulo si no es encontrado
    public static Empleado GetEmpleado(String LegajoID){
        // Generamos el empleado a devolver
        Empleado employeeFromAPI;
        // Devolvemos el empleado procesado
        return null;


    }
}
