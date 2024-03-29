package io.apps4u.fpmobile;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.apps4u.fpdatabase.Coordinate;
import io.apps4u.fpdatabase.Database;
import io.apps4u.fpdatabase.Empleado;
import io.apps4u.fpdatabase.EmpleadoDB;
import com.fgtit.fpcore.FPMatch;
import fgtit.fpengine.fpdevice;
import io.apps4u.fpdatabase.Manager;
import io.apps4u.fpdatabase.SignUp;
import io.apps4u.fpdatabase.SignUpDB;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

public class ActivityMain extends Activity {

    public static final int WORKTYPE_NULL = 0;
    public static final int WORKTYPE_ENROL = 1;
    public static final int WORKTYPE_MATCH = 2;
    public static final int ENROL_NUM = 4;

    private static final String ADDRESS_NOT_FOUND = "Dirección no localizada";
    private static final String SOURCE_ENROLL = "Fichada bajo W4U Bio Movíl";

    public LocationChecker locationChecker;

    private static final Double NO_COORDINATES = 0D;
    public Double LATITUDE = NO_COORDINATES;
    public Double LONGITUDE = NO_COORDINATES;

    private static fpdevice fpdev = new fpdevice();

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private int workType = 0;
    private int enrolCount = 0;
    private int totalCount = 0;

    private static boolean isopening = false;

    private TextView tvStatus = null;
    private ImageView ivImage = null;

    private byte bmpdata[] = new byte[74806];
    public byte rawdata[] = new byte[73728];
    public int rawsize[] = new int[1];

    private byte tpdata[] = new byte[512];
    private int tpsize[] = new int[1];

    private byte refdata[] = new byte[ENROL_NUM * 256];
    private byte matdata[] = new byte[256];

    private Handler handler = null;

    public static final String ACTION_USB_PERMISSION = "io.apps4u.fpmobile.USB";

    String sDirectory = Environment.getExternalStorageDirectory() + "/FingerprintReader";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Controlamos el evento de inicio, funciona cuando se levanta la aplicación y cuando se reinicia la aplicación en pausa
    @Override
    protected void onStart(){
        super.onStart();
        locationChecker = new LocationChecker(this);
        // Validamos si el dispositivo esta listo para la fichada
        CheckIsReadyToFingerPrint();
        // Chequeamos las fichadas que aun no han sido enviadas a servidor
        checkAndRegisterEnrollsOnServer();
        // Chequeamos la ultima fichada
        checkTodaySignups();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onStop() {
        super.onStop();
        // Detenemos el timer de recoleccion de huellas
        TimerStop();
        // Eliminamos las coordenadas
        LATITUDE = NO_COORDINATES;
        LONGITUDE = NO_COORDINATES;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Intent it = getIntent();
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(it);
                        }
                    } else {
                    }
                }
            }
        }
    };

    public void requestPermission() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            return;
        }
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> devlist = usbManager.getDeviceList();
        Iterator<UsbDevice> deviter = devlist.values().iterator();
        while (deviter.hasNext()) {
            UsbDevice tmpusbdev = deviter.next();
            if ((tmpusbdev.getVendorId() == 0x0453) && (tmpusbdev.getProductId() == 0x9005)) {
                usbDevice = tmpusbdev;
                break;
            } else if ((tmpusbdev.getVendorId() == 0x2009) && (tmpusbdev.getProductId() == 0x7638)) {
                usbDevice = tmpusbdev;
                break;
            } else if ((tmpusbdev.getVendorId() == 0x2109) && (tmpusbdev.getProductId() == 0x7638)) {
                usbDevice = tmpusbdev;
                break;
            } else if ((tmpusbdev.getVendorId() == 0x0483) && (tmpusbdev.getProductId() == 0x5720)) {
                usbDevice = tmpusbdev;
                break;
            }
        }
        if (!usbManager.hasPermission(usbDevice)) {
            synchronized (mUsbReceiver) {
                usbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    }

    private void initHandler() {
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1: {
                        switch (workType) {
                            case WORKTYPE_ENROL: {
                                TimerStop();
                                int k = totalCount % 2;
                                int ret = fpdev.FPGetImage(0xffffffff);
                                if (ret != 0) {
                                    if (k != 0) {
                                        totalCount++;
                                        Toast.makeText(getApplicationContext(), R.string.txt_fp1, Toast.LENGTH_SHORT).show();
                                    }
                                    TimerStart();
                                } else {
                                    if (k == 0) {
                                        if (true) {
                                            if (fpdev.FPUpImage(0xffffffff, rawdata, rawsize) == 0) {
                                                fpdev.FPImageToBitmap(rawdata, bmpdata);
                                                Bitmap bm1 = BitmapFactory.decodeByteArray(bmpdata, 0, 74806);
                                                ivImage.setImageBitmap(bm1);
                                            }
                                        }
                                        if (fpdev.FPGenChar(0xffffffff, 0x01) != 0) {
                                            Toast.makeText(getApplicationContext(), "Falla en leer la huella", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (fpdev.FPUpChar(0xffffffff, 0x01, tpdata, tpsize) != 0) {
                                            Toast.makeText(getApplicationContext(), "Falla en leer la huella", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        System.arraycopy(tpdata, 0, refdata, enrolCount * 256, 256);
                                        enrolCount++;
                                        totalCount++;

                                        writeTxtToFile(Base64.encodeToString(refdata, 0), "one");

                                        if (enrolCount >= (ENROL_NUM)) {
                                            Toast.makeText(getApplicationContext(), "Se ha tomado la huella con exito", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Quite el Dedo", Toast.LENGTH_SHORT).show();
                                            TimerStart();
                                        }
                                    } else {
                                        TimerStart();
                                    }
                                }
                            }
                            break;
                            case WORKTYPE_MATCH: {
                                try{
                                    TimerStop();
                                    int ret = fpdev.FPGetImage(0xffffffff);
                                    if (ret != 0) {
                                        TimerStart();
                                    } else {
                                        if (fpdev.FPGenChar(0xffffffff, 0x01) != 0) {
                                            Toast.makeText(getApplicationContext(), "Error al generar plantilla", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (fpdev.FPUpChar(0xffffffff, 0x01, tpdata, tpsize) != 0) {
                                            Toast.makeText(getApplicationContext(), "Error al generar plantilla", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        System.arraycopy(tpdata, 0, matdata, 0, 256);
                                        // Tratamos de obtener el empleado relacionado a la consulta
                                        Empleado employee = FPMatch.getInstance().MatchTemplateW4u(refdata, matdata, 60, getApplicationContext(), getApplication());
                                        if (employee != null) {
                                            try{
                                                // Registramos la fichada
                                                registerMatchEmployee(employee);
                                                // Actualizamos la ultima fichada
                                                checkTodaySignups();
                                                // agregar el post a la fichada
                                                Toast.makeText(getApplicationContext(), R.string.txt_fichaok, Toast.LENGTH_SHORT).show();
                                            } catch(SecurityException e) {


                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), R.string.txt_fichafail, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch(Exception e) {
                                    Log.e("ErrorMatch", e.getMessage());
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void CheckIsReadyToFingerPrint(){
        // Levantamos el boton
        Button btnFichar = findViewById(R.id.btnFichar);
        if(isFingerPrintReady()){
            // Validamos si las coordenadas fueron cargadas
            if(LONGITUDE != NO_COORDINATES && LATITUDE != NO_COORDINATES){
                // Validamos si hay algun empleado cargado en la base local
                EmpleadoDB eDB = new EmpleadoDB(getApplicationContext());
                // Separamos el administrador de la aplicacion
                Manager loggedManager = ((Session) getApplication()).loggedManager;
                // Recolectamos los empleados almacenados
                List<Empleado> lstEmployees = eDB.GetAll(loggedManager.get_legajoId());
                if(lstEmployees != null && lstEmployees.size() > 0){
                    // Configuramos el nuevo evento de listener
                    btnFichar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FicharWithCoordinatesAcquired();
                        }
                    });
                    // Cambiamos el color de fondo del boton
                    btnFichar.setBackgroundColor(getResources().getColor(R.color.azul));
                } else { // Si no hay ningun empleado cargado
                    btnFichar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MissingEmployeesMessage();
                        }
                    });
                }
                // Si las coordenadas no estan cargadas
            } else {
                // Seteamos el evento de click cuando las coordenadas no estan cargadas
                btnFichar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MissingCordinatesMessage();
                    }
                });
                // Cambiamos el color de fondo del boton
                btnFichar.setBackgroundColor(getResources().getColor(R.color.gray_background));
            }
        } else {
            // Seteamos el evento de click cuando las coordenadas no estan cargadas
            btnFichar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MissingFingerPrintDeviceMesage();
                }
            });
            // Cambiamos el color de fondo del boton
            btnFichar.setBackgroundColor(getResources().getColor(R.color.gray_background));
        }
    }

    public void MissingCordinatesMessage(){
        Toast.makeText(getApplicationContext(), R.string.getting_coordinates, Toast.LENGTH_LONG).show();
    }

    public void MissingEmployeesMessage(){
        Toast.makeText(getApplicationContext(), R.string.error_no_employees, Toast.LENGTH_SHORT).show();
    }

    public void MissingFingerPrintDeviceMesage(){
        Toast.makeText(getApplicationContext(), R.string.missing_fingerprint_devices, Toast.LENGTH_LONG).show();
    }

    public void FicharWithCoordinatesAcquired(){
        // Iniciamos la toma de huellas
        InitTakeFingerActivity();
    }

    private boolean isFingerPrintReady(){
        fpdev.SetInstance(this);
        fpdev.SetUpImage(true);
        if (isopening) {
            fpdev.CloseDevice();
            TimerStop();
            isopening = false;
        }
        switch (fpdev.OpenDevice()) {
            case 0:
                return true;
            case -1:

                return false;
            case -2:
            case -3:
                Log.e("FPDevice", "No esta listo, contiene errores a verificar.");
                return false;
            default:
                return false;
        }
    }

    private void InitTakeFingerActivity(){
        if(isFingerPrintReady()){
            // Pedimos al usuario que marque huella
            Toast.makeText(getApplicationContext(), R.string.txt_fp1, Toast.LENGTH_LONG).show();
            // Tarea que escucha lectura de huellas en dispositivo
            initHandler();
            // Informamos  que se esta haciendo la tarea de tomar huellas
            isopening = true;
            // Vaciamos el tipo de trabajo actual
            workType=WORKTYPE_NULL;
            // Detenemos el tiempo
            TimerStop();
            // Esperamos unos 200 ms
            SystemClock.sleep(200);
            // Iniciamos la toma de huella
            TimerStart();
            // Establecemos como actividad el matcheo de huellas
            workType=WORKTYPE_MATCH;
        }
    }

    // Registramos los datos del empleado en la base de datos
    private void registerMatchEmployee(Empleado emp){
        // Generamos un nuevo objeto signup
        SignUp newSignIn = new SignUp();
        // Establecemos el legajo del usuario fichado
        newSignIn.set_empleado(emp);
        // Levantamos las coordenadas
        Coordinate currentCoordinates = new Coordinate();
        currentCoordinates.set_longitude(LONGITUDE);
        currentCoordinates.set_latitude(LATITUDE);
        // Establecemos coordenadas
        newSignIn.set_coordinates(currentCoordinates);
        // Establecemos los comentarios
        newSignIn.set_details(SOURCE_ENROLL);
        // Establecemos las direcciones
        newSignIn.set_address(GetAddress(currentCoordinates));
        // Configuramos el horario de la fichada
        newSignIn.set_timestamp(Database.GetCurrentFormattedDatabaseDate());
        // Levantamos la instancia de base de datos local para almacenar los resultados de la fichada
        SignUpDB suDB = new SignUpDB(getApplicationContext());
        // Guardamos el signup en la base de datos
        suDB.Add(newSignIn);
        // Registramos la fichada en el momento
        checkAndRegisterEnrollsOnServer();
    }

    // Método que carga los enrolamientos en el servidor de la API
    private void checkAndRegisterEnrollsOnServer(){
        // Durante el proceso se realiza una peticion por JSON, por defecto android da una excepcion si se pide por network un recurso en el thread principal
        Thread checkSignupsThread = new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    // Validamos si hay conexión a la red
                    if(isNetworkReady()){
                        // Levantamos las fichadas que no esten registradas en el servidor
                        SignUpDB sDb = new SignUpDB(getApplicationContext());
                        // Levantamos el administrador de la cuenta
                        Session sessionInfo = (Session) getApplication();
                        // Elegimos el administrador de la cuenta
                        Manager loggedManager = sessionInfo.loggedManager;
                        // Recolectamos las fichadas sin registrar en el servidor
                        ArrayList<SignUp> lstSignups = sDb.GetUnregisteredSignups(loggedManager);
                        // Enviamos los fichados al servidor de W4U
                        APIRequests.Enroll(lstSignups, loggedManager, getApplicationContext());
                    }

                } catch(Resources.NotFoundException nfe){ }
                catch(Exception e){
                    Log.e("GettingSignUps", e.getMessage());
                }
            }
        });
        // Iniciamos el Thread
        checkSignupsThread.start();
    }

    private void checkTodaySignups(){
        try{
            // Levantamos una instancia de la base de datos
            SignUpDB sDB = new SignUpDB(getApplicationContext());
            // Recolectamos el administrador de la session
            Manager loggedManager = ((Session) getApplication()).loggedManager;
            // Recolectamos las ultimas fichadas correspondientes del día de hoy
            List<SignUp> todaySignups = sDB.GetTodaySignups(loggedManager);
            // Levantamos los dos layout
            ConstraintLayout noSignupsLayout = findViewById(R.id.WithoutLastSignupLayout);
            ConstraintLayout withSignupsLayout = findViewById(R.id.WithLastSignupLayout);
            // Validamos si el listado se encuentra nulo
            if(todaySignups != null && todaySignups.size() > 0) {
                // Obtenemos la ultima fichada
                SignUp lastSignup = todaySignups.get(todaySignups.size() - 1);
                // Ocultamos el layout que muestra que no hay fichadas
                noSignupsLayout.setVisibility(View.GONE);
                // Configuramos el panel que muestre los datos de la fichada
                TextView lblLegajo = findViewById(R.id.lblLegajoDataLastSignup);
                TextView lblFullname = findViewById(R.id.lblFullnameDataLastSignup);
                TextView lblTime = findViewById(R.id.lblTimeDataLastSignup);
                // Establecemos los valores de la ultima fichada
                lblLegajo.setText(lastSignup.get_empleado().get_legajo());
                lblFullname.setText(lastSignup.get_empleado().get_fullname());
                // Convertimos el formato de base de datos a formato solamente hora
                String strLastSignupTime = Database.GetHourOnlyFromDatabaseTime(lastSignup.get_timestamp());
                lblTime.setText(strLastSignupTime);
                // Mostramos el layout de ultima fichada
                withSignupsLayout.setVisibility(View.VISIBLE);
            } else{
                // Mostramos en la interfaz que no hay fichadas el día de hoy
                noSignupsLayout.setVisibility(View.VISIBLE);
                // Ocultamos llegado el caso de que sea necesario el layout de ultima fichada
                withSignupsLayout.setVisibility(View.GONE);
            }

        } catch (Exception e){
            Log.e("GetSignups", e.getMessage());
        }
    }

    // Chequeamos si hay conectividad en la red
    private boolean isNetworkReady(){
        try{
            // Llevantamos un administración de connexión
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            // Validamos si hay conexión a la red
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED){
                return true;
            }
            return false;
        } catch(Exception e){
            return false;
        }
    }

    // Obtenemos direcciones basadas en coordenadas geográficas
    private String GetAddress(Coordinate fetchingCoordinates){
        try{
            // Levantamos los métodos de geo
            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
            // Levantamos la lista de direcciones
            List<Address> addresses = geo.getFromLocation(fetchingCoordinates.get_latitude(), fetchingCoordinates.get_longitude(),1);
            // Validamos si existen resultados en la consulta
            if(addresses.isEmpty()){
                return ADDRESS_NOT_FOUND;
            } else if(addresses.size() > 0){
                // Si hay resultados, los mostramos en pantalla
                Address reqAddress = addresses.get(0);
                // Descomponemos el string en varios elementos para poder mostrarlos
                String strStreet = reqAddress.getThoroughfare();
                String strStreetNumber = reqAddress.getSubThoroughfare();
                String strProvince = reqAddress.getAdminArea();
                String strCountry = reqAddress.getCountryName();
                return strStreet + " " + strStreetNumber + ", " + strProvince + ", " + strCountry + ".";
            }
        } catch(Exception e){
            // Es posible que no devuelva resultados
        }
        return ADDRESS_NOT_FOUND;
    }


    public void TimerStart() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        }
        if (mTimer != null && mTimerTask != null)
            mTimer.schedule(mTimerTask, 50, 50);
    }

    public void TimerStop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            fpdev.CloseDevice();
            tvStatus.setText("Close");
            isopening = false;
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        // Levantamos el item de menu relacionado con los nuevos empleados
        MenuItem mnuNewEmployee = menu.findItem(R.id.mnu_new_employee);
        // Para poder cambiar el color debemos establecer un nuevo string
        SpannableString s = new SpannableString(getResources().getString(R.string.menu_new_employee));
        // Establecemos la variable color
        ForegroundColorSpan fcs;
        // Si el lector no esta listo se inhabilita esta funcion
        if(!isFingerPrintReady()){
            // Cambiamos el color del menu item
            fcs = new ForegroundColorSpan(getResources().getColor(R.color.abm_input_colors_text_buttons_disabled));
        } else{
            // Cambiamos el color del menu item
            fcs = new ForegroundColorSpan(getResources().getColor(R.color.verde));
        }
        s.setSpan(fcs, 0, s.length(), 0);
        // Establecemos el color
        mnuNewEmployee.setTitle(s);
        // Por defecto devolvemos verdadero
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_new_employee:
                if(isFingerPrintReady()){
                    Intent intentEnrol = new Intent(this, ActivityEmployeeABM.class);
                    this.startActivity(intentEnrol);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.cannot_enroll_employee_without_fpdevice, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.mnu_show_employees:
                // another startActivity, this is for item with id "menu_item2"
                Intent intentVerEmpleados = new Intent(this, ActivityShowEmployees.class);
                this.startActivity(intentVerEmpleados);
                break;
            case R.id.mnu_show_today_signups:
                Intent intentShowSignups = new Intent(this, ActivityShowSignups.class);
                this.startActivity(intentShowSignups);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == LocationChecker.PERMISSION_FINE_LOCATION_CODE || requestCode == LocationChecker.PERMISSION_COARSE_LOCATION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                // Mostramos el mensaje de error y cerramos la aplicacion
                GPSRequiredFragment requireGPS = new GPSRequiredFragment();
                // Deshabilitamos la cancelación tocando al costado del dialogo
                requireGPS.setCancelable(false);
                // Mostramos el dialog
                requireGPS.show(getFragmentManager(), "RequiredGPS");
            } else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                locationChecker.StartRequestLocation();
            }
        }
    }

    //write txt file
    public void writeTxtToFile(String content, String filename) {
        try {

            EmpleadoDB emp = new EmpleadoDB(getApplicationContext());
            emp.Save(new Empleado("Mega", "Jose", content, "61"));
            FileWriter fw = new FileWriter(sDirectory + "/" + filename + ".txt");//SD卡中的路径
            fw.flush();
            fw.write(content);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}
