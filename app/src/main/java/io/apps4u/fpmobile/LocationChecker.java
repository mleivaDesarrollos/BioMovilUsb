package io.apps4u.fpmobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class LocationChecker {

    private final long MIN_TIME_BW_UPDATES = 10000L; // Milisegundos
    private final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // Metros

    public static final int PERMISSION_FINE_LOCATION_CODE = 203;
    public static final int PERMISSION_COARSE_LOCATION_CODE = 204;

    private LocationManager locationManager;

    private Activity callerActivity;

    public LocationChecker(Activity activity){
        callerActivity = activity;
        // Iniciamos la tarea de busqueda de coordenadas
        ValidateGPSPermissionAndStartRequestCoords();
    }

    @SuppressLint("NewApi")
    private void ValidateGPSPermissionAndStartRequestCoords(){
        if(!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            callerActivity.requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE_LOCATION_CODE);
        }
        else if(!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)){
            callerActivity.requestPermissions( new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_COARSE_LOCATION_CODE);
        } else {
            // Get location manager
            getNewLocationManager();
            // Requerimos las nuevas coordenadas
            requestCoordinates();
        }
    }

    public void StartRequestLocation(){
        // Generamos una nueva instancia del administrador de ubicaciones
        getNewLocationManager();
        // Solicitamos las coordenadas
        requestCoordinates();
    }


    private void getNewLocationManager(){
        // Validamos si ya hay una instancia de location manager generada
        if(locationManager != null) {
            // La destruimos
            stopAndDestroyLocationManager();
        }
        // Generamos una nueva
        locationManager = (LocationManager)callerActivity.getSystemService(Context.LOCATION_SERVICE);

    }

    private void stopAndDestroyLocationManager(){
        // Verificamos que el location manager este iniciado
        if(locationManager != null){
            // Detenemos el chequeo de direccion
            locationManager.removeUpdates(locListener);
            locationManager = null;
        }
    }

    private void requestCoordinates(){
        try{
            // Validamos si los proveedores de servicio se encuentran activos
            boolean isGPSActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkActive = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            // Orden de consulta, primero validamos el servicio de red, luego el servicio GPS
            if(isNetworkActive){
                // Tratamos de obtener la direccion de parte del proveedor de servicios de red
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locListener);
                return;
            } else if(isGPSActive) {
                // Iniciamos la tarea de requerir las direcciones por medio del GPS
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locListener);
                return;
            }
        } catch(SecurityException e){ }
        Toast.makeText(callerActivity.getApplicationContext(), "No es posible tomar las coordenadas.", Toast.LENGTH_LONG).show();
    }

    public final LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            reportReceivedCoordinates(location.getLatitude(), location.getLongitude());
            // Detenemos el proceso de busqueda de nueva direccion
            stopAndDestroyLocationManager();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    // Dentro de este metodo se debe informar que hacer o a donde comunicar la información recibida de los servicios de ubicacion
    private void reportReceivedCoordinates(Double paramLatitude, Double paramLongitude){
        // Casteamos la actividad al tipo ActivityMain
        ActivityMain main = (ActivityMain) callerActivity;
        // Establecemos los parametros de longitud y latitude
        main.LATITUDE = paramLatitude;
        main.LONGITUDE = paramLongitude;
        // Indicamos a la actividad principal que vuelva a chequear las coordenadas
        main.CheckIsReadyToFingerPrint();
        // Debugueamos el mensaje
        //if(Session.DEBUG) Toast.makeText(callerActivity.getApplicationContext(), "Latitude: " + main.LATITUDE +". Longitud: " + main.LONGITUDE , Toast.LENGTH_LONG).show();
    }


    @SuppressLint("NewApi")
    private boolean hasPermission(String perm){
        return (PackageManager.PERMISSION_GRANTED == callerActivity.checkSelfPermission(perm));
    }
}
