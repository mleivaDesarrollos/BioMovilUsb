package io.apps4u.fpmobile;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import io.apps4u.fpdatabase.Database;
import io.apps4u.fpdatabase.Empleado;
import io.apps4u.fpdatabase.Manager;
import io.apps4u.fpdatabase.SignUp;
import io.apps4u.fpdatabase.SignUpDB;

public class APIRequests {
    // Request HEADERS
    private static final String EMPRESA = "empresaid";
    private static final String AUTHORIZATION = "Authorization";
    // Login JSON Body
    private static final String LOGIN_EMAIL = "email";
    private static final String LOGIN_PASSWORD = "password";
    // Legajo JSON Item
    private static final String LEGAJO = "legajo";
    // Enroll JSON Body
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "long";
    private static final String DETAILS = "motivo";
    private static final String ADDRESS = "direccion";
    private static final String TIMESTAMP = "fecha";

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

    // Mètodo que se encarga de interactuar con la API de Work 4U
    public static void Enroll(ArrayList<SignUp> paramSignups, Manager loggedManager, Context context) throws Exception {
        try{
            // Validamos que haya contenido en la lista
            if(paramSignups == null || paramSignups.size() <= 0) throw new Exception("Es necesario pasar al menos un enrolamiento para enviar la informacion la base de datos.");
            // Levantamos una instancia de ayudante de base de datos
            SignUpDB sDB = new SignUpDB(context);
            // Iteramos sobre todos los elementos contenidos en la lista
            for(SignUp sign: paramSignups){
                // Cargamos la dirección URL que se utilizará para enviar
                URL url = new URL("https://find4u.apps4u.io/api/v4/fichar");
                // Preparamos la conexión
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // Disponemos del método de conexión
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                // Configuramos las cabeceras
                conn.setRequestProperty(EMPRESA, loggedManager.get_companyId());
                conn.setRequestProperty(AUTHORIZATION, loggedManager.get_authorization());
                // Generamos el objeto JSON para Procesarlo en retorno
                List<AbstractMap.SimpleEntry> queryStringParams = new ArrayList<>();
                // Cargamos los parametros por query string
                queryStringParams.add(new AbstractMap.SimpleEntry(LEGAJO, sign.get_empleado().get_legajo()));
                queryStringParams.add(new AbstractMap.SimpleEntry(LATITUDE, sign.get_coordinates().get_latitude()));
                queryStringParams.add(new AbstractMap.SimpleEntry(LONGITUDE, sign.get_coordinates().get_longitude()));
                queryStringParams.add(new AbstractMap.SimpleEntry(DETAILS, ""));
                queryStringParams.add(new AbstractMap.SimpleEntry(ADDRESS, sign.get_address()));
                queryStringParams.add(new AbstractMap.SimpleEntry(TIMESTAMP, Database.SwitchFromDatabaseToW4UDateformat(sign.get_timestamp())));
                // Generamos un objeto DataOutputStream
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                // Escribimos los bytes de los datos en JSON
                writer.write(getQuery(queryStringParams));
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                int status = conn.getResponseCode();
                switch(status) {
                    case 200:
                    case 201:
                        // Una vez registrado y logrado el cambio de manera correcta, hay que actualizar la base local para que no vuelva a enviarlo en posteriores solicitudes
                        sDB.SetRegistered(sign);
                        break;
                }
            }
        } catch(Exception e){
            throw e;
        }

    }

    // Consulta a la API por la existencia del empleado. Devuelve nulo si no es encontrado
    public static Empleado GetEmpleado(String LegajoID, Application app){
        // Preparamos el objeto de retorno
        Empleado empleadoOnW4u = null;
        try{
            // Levantamos las variables de Session
            Session sessionInfo = (Session) app;
            // De la variable de session levantamos el administrador
            Manager manager = ((Session) app).loggedManager;
            // Cargamos la dirección URL que se utilizará para enviar
            URL url = new URL("https://find4u.apps4u.io/api/v4/validarLegajo");
            // Preparamos la conexión
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Disponemos del método de conexión
            conn.setRequestMethod("POST");
            // Configuramos las cabeceras
            conn.setRequestProperty("Content-Type", "application/json");
            // A modo de prueba, configuramos para levantar el legajo 1073 con empresa ID 1019
            if(Session.DEBUG) {
                conn.setRequestProperty(EMPRESA, "1019");
            } else {
                conn.setRequestProperty(EMPRESA, manager.get_companyId());
            }
            conn.setRequestProperty(AUTHORIZATION, manager.get_authorization());
            // Generamos el objeto JSON para Procesarlo en retorno
            JSONObject jsonData = new JSONObject();
            // Cargamos los valores en el objeto JSON
            jsonData.put(LEGAJO, LegajoID);
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
                    empleadoOnW4u = new Empleado();
                    // Realizamos la lectura de la información recibida
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    // Parseamos el JSON
                    JSONObject jsonRead = new JSONArray(sb.toString()).getJSONObject(0);
                    // Separamos el nombre completo y lo disponemos
                    String strFirstname = jsonRead.getString("NOMBRE");
                    String strLastname = jsonRead.getString("APELLIDO");
                    // Guardamos los valores dentro del empleado
                    empleadoOnW4u.set_managerid(manager.get_legajoId());
                    empleadoOnW4u.set_fullname(strFirstname + " " + strLastname);
                    empleadoOnW4u.set_legajo(LegajoID);
                    break;
            }
        }catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
        // Devolvemos el valor procesado
        return empleadoOnW4u;
    }

    // Code Snippet para convertir parametros a queryString y asi poder enviar solicitudes con este formato
    private static String getQuery(List<AbstractMap.SimpleEntry> params) throws UnsupportedEncodingException{
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(AbstractMap.SimpleEntry parameter: params){
            // El primer caracter en queryString va sin ampersand, el resto si.
            if(!first){
                sb.append("&");
            } else {
                first = false;
            }
            sb.append(URLEncoder.encode(parameter.getKey().toString(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(parameter.getValue().toString(), "UTF-8"));
        }
        return sb.toString();
    }
}
