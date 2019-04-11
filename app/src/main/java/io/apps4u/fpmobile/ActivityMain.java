package io.apps4u.fpmobile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.SystemClock;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.apps4u.fpdatabase.Coordinate;
import io.apps4u.fpdatabase.Empleado;
import io.apps4u.fpdatabase.EmpleadoDB;
import com.fgtit.fpcore.FPMatch;
import fgtit.fpengine.fpdevice;
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

    private static final DateFormat FORMATDATE_W4U = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
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

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		/*FPMatch.getInstance().InitMatch(0, "https://www.hfteco.com");

		btnOpen=(Button)findViewById(R.id.button1);
		btnClose=(Button)findViewById(R.id.button2);
		btnEnrol=(Button)findViewById(R.id.button10);
		btnMatch=(Button)findViewById(R.id.button11);
		btnMenuEnrol=(Button)findViewById(R.id.action_enrol);

		tvStatus=(TextView)findViewById(R.id.textView1);
		ivImage=(ImageView)findViewById(R.id.imageView1);
		mEditText = (EditText) findViewById(R.id.editText1);
		mEditText.setVisibility(View.GONE);
		checkBoxImage=(CheckBox)findViewById(R.id.checkBox2);


	//	initHandler();
		
		fpdev.SetInstance(this);
		fpdev.SetUpImage(true); 
						
		btnOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isopening){
					fpdev.CloseDevice();
					isopening=false;
				}
				
				switch(fpdev.OpenDevice()){
				case 0:	
					isopening=true;
					tvStatus.setText("Open Device OK");
					break;
				case -1:
					tvStatus.setText("Link Device Fail");
					break;
				case -2:
					tvStatus.setText("Evaluation version expires");
					break;
				case -3:
					tvStatus.setText("Open Device Fail");
					break;
				}
			}
		});
		
		*//*btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//requestPermission();
				if(isopening){
					fpdev.CloseDevice();
					tvStatus.setText("Close");
					isopening=false;
				}
			}
		});*//*
         *//*


		btnEnrol.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isopening){
					workType=WORKTYPE_NULL;
					TimerStop();
					SystemClock.sleep(200);
					TimerStart();
					tvStatus.setText(R.string.txt_fp1);
					workType=WORKTYPE_ENROL;
					enrolCount=0;
					totalCount=0;
				}
			}
		});
 
		btnMatch.setOnClickListener(new View.OnClickListener() {
	    	@Override
	    	public void onClick(View v) {
	    		if(isopening){
	    			workType=WORKTYPE_NULL;
	    			TimerStop();
					SystemClock.sleep(200);
	    			TimerStart();
	    			tvStatus.setText(R.string.txt_fp1);
	    			workType=WORKTYPE_MATCH;
	    		}
	    	}
	    });
		
		btnOpen.callOnClick();*/
        locationChecker = new LocationChecker(this);
        CheckCoordinateStatus();
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
                                                // agregar el post a la fichada
                                                Toast.makeText(getApplicationContext(), R.string.txt_fichaok, Toast.LENGTH_SHORT).show();
                                            } catch(SecurityException e) {
                                                // En caso de que exista error de permisos
                                                Toast.makeText(getApplicationContext(), R.string.error_gps_not_available, Toast.LENGTH_SHORT).show();
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

    public void CheckCoordinateStatus(){
        // Levantamos el boton
        Button btnFichar = findViewById(R.id.btnFichar);
        // Validamos si las coordenadas fueron cargadas
        if(LONGITUDE != NO_COORDINATES && LATITUDE != NO_COORDINATES){
            // Configuramos el nuevo evento de listener
            btnFichar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FicharWithCoordinatesAcquired();
                }
            });
            // Cambiamos el color de fondo del boton
            btnFichar.setBackgroundColor(getResources().getColor(R.color.azul));
            // Si las coordenadas no estan cargadas
        } else {
            // Seteamos el evento de click cuando las coordenadas no estan cargadas
            btnFichar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), R.string.error_gps_not_available, Toast.LENGTH_LONG).show();
                }
            });
            // Cambiamos el color de fondo del boton
            btnFichar.setBackgroundColor(getResources().getColor(R.color.gray_background));

        }
    }

    public void FicharWithoutCoordinates(){

    }


    public void FicharWithCoordinatesAcquired(){
        fpdev.SetInstance(this);
        fpdev.SetUpImage(true);
        if (isopening) {
            fpdev.CloseDevice();
            isopening = false;
        }
        initHandler();
        switch (fpdev.OpenDevice()) {
            case 0:
                isopening = true;
                Toast.makeText(getApplicationContext(), "Coloque el dedo en el lector", Toast.LENGTH_SHORT).show();
                workType=WORKTYPE_NULL;
                TimerStop();
                SystemClock.sleep(200);
                TimerStart();
                workType=WORKTYPE_MATCH;
                break;
            case -1:
                Toast.makeText(getApplicationContext(), "El lector no se encuentra listo", Toast.LENGTH_SHORT).show();
                break;
            case -2:
                Toast.makeText(getApplicationContext(), "Contacte con el administrador", Toast.LENGTH_SHORT).show();
                break;
            case -3:
                Toast.makeText(getApplicationContext(), "Error al abrir dispositivo", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Registramos los datos del empleado en la base de datos
    private void registerMatchEmployee(Empleado emp){
        // Generamos un nuevo objeto signup
        SignUp newSignIn = new SignUp();
        // Establecemos el legajo del usuario fichado
        newSignIn.set_legajo(emp.get_legajo());
        // Levantamos las coordenadas
        // Coordinate currentLocation = GetCurrentCoordinates();
        // Establecemos coordenadas
        //newSignIn.set_coordinates(currentLocation);
        // Establecemos los comentarios
        newSignIn.set_details(SOURCE_ENROLL);
        // Establecemos las direcciones
        //newSignIn.set_address(GetAddress(currentLocation));
        // Configuramos el horario de la fichada
        newSignIn.set_timestamp(FORMATDATE_W4U.format((new Date())));
        // TODO AQUI IRIA LA VALIDACION ONLINE
        // Levantamos la instancia de base de datos local para almacenar los resultados de la fichada
        SignUpDB suDB = new SignUpDB(getApplicationContext());
        // Guardamos el signup en la base de datos
        suDB.Add(newSignIn);
    }

   /* // Obtenemos las coordenadas del dispositivo
    private Coordinate GetCurrentCoordinates(){
        try{
            // Generamos un nuevo objeto
            Coordinate coordinate = new Coordinate();
            // Levantamos el LocationManager
            LocationManager lmCurrent = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            // Levantamos la ubicacion
            // Location location = lmCurrent.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location location;



            // Validamos si la location es nula (si es nula es que no tiene acceso al satelite en este momento
            if(location != null ){
                coordinate.set_latitude(location.getLatitude());
                coordinate.set_longitude(location.getLongitude());
            } else {
                // Seleccionamos el proveedor
                String provider = lmCurrent.getBestProvider(new Criteria(), true);
                // Levantamos la ubicación
                location = lmCurrent.getLastKnownLocation(provider);
                if(location != null){
                    coordinate.set_latitude(location.getLatitude());
                    coordinate.set_longitude(location.getLongitude());
                }
            }
            // Devolvemos las coordenadas
            return coordinate;
        } catch(SecurityException e){
            throw e;
        }
    }*/

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

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        //btnOpen.callOnClick();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onStop() {
        //btnClose.callOnClick();
        super.onStop();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_new_employee:
                Intent intentEnrol = new Intent(this, ActivityEmployeeABM.class);
                this.startActivity(intentEnrol);
                break;
            case R.id.mnu_show_employees:
                // another startActivity, this is for item with id "menu_item2"
                Intent intentVerEmpleados = new Intent(this, ActivityShowEmployees.class);
                this.startActivity(intentVerEmpleados);
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
