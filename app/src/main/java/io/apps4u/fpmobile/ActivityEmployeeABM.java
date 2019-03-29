package io.apps4u.fpmobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import fgtit.fpengine.fpdevice;
import io.apps4u.fpdatabase.Empleado;
import io.apps4u.fpdatabase.EmpleadosDbHelper;

public class ActivityEmployeeABM extends Activity {
    private static final String NO_FINGER_DATA = "NONE";
    public static final int WORKTYPE_NULL=0;
    public static final int WORKTYPE_ENROL=1;
    public static final int WORKTYPE_MATCH=2;
    public static final int ENROL_NUM=3;
    private static fpdevice fpdev=new fpdevice();
    private Timer mTimer=null;
    private TimerTask mTimerTask=null;
    private int workType=0;
    private int enrolCount=0;
    private int totalCount=0;
    private byte bmpdata[]=new byte[74806];
    private int bmpsize[]=new int[1];
    public byte rawdata[]=new byte[73728];
    public int rawsize[]=new int[1];
    private static boolean isopening=false;
    private byte tpdata[]=new byte[512];
    private int tpsize[]=new int[1];
    private byte refdata[]=new byte[ENROL_NUM*256];
    private byte matdata[]=new byte[256];
    private Handler handler=null;
    public static final String ACTION_USB_PERMISSION = "io.apps4u.fpmobile.USB";
    private Button btnOpen,btnClose,btnEnrol;
    private ImageView ivImage=null;
    private boolean modifyEmployee = false;
    private String FingerData = NO_FINGER_DATA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        if(!isModifyRequest()){
            ivImage= (ImageView)findViewById(R.id.fpImage);
            Button btnScanear = findViewById(R.id.btnScanear);
            fpdev.SetInstance(this);
            fpdev.SetUpImage(true);
            switch(fpdev.OpenDevice()){
                case 0:
                    initHandler();
                    isopening=true;
                    Toast.makeText(getApplicationContext(),"Dispositivo OK",Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    //  tvStatus.setText("Link Device Fail");
                    break;
                case -2:
                    // tvStatus.setText("Evaluation version expires");
                    break;
                case -3:
                    // tvStatus.setText("Open Device Fail");
                    break;
            }

            btnScanear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    workType=WORKTYPE_NULL;
                    TimerStop();
                    SystemClock.sleep(200);
                    TimerStart();
                    Toast.makeText(getApplicationContext(),R.string.txt_fp1,Toast.LENGTH_SHORT).show();
                    workType=WORKTYPE_ENROL;
                    enrolCount=0;
                    totalCount=0;
                }
            });
        }
    }

    // Validamos si el intent viene con legajo, de ser asi traemos los datos de la base y completamos los campos
    private boolean isModifyRequest(){
        // Recolectamos el intent
        Intent intent = getIntent();
        // Validamos si el intent viene con legajo
        if(intent.hasExtra("legajo")){
            // Generamos una nueva instancia del contexto
            EmpleadosDbHelper dbInstance = new EmpleadosDbHelper(getApplicationContext());
            // Separamos el legajo y lo almacenamos en un string
            String strLegajo = intent.getStringExtra("legajo");
            // Obtenemos el empleado desde la base de datos
            Empleado employee = dbInstance.GetEmpleado(strLegajo);
            // Mostramos los datos del empleado en cada uno de los campos
            EditText txtCompanyID = (EditText) findViewById(R.id.txtCompanyID);
            EditText txtEmployeeFullname = (EditText) findViewById(R.id.txtEmployeFullname);
            EditText txtEmployeeNumber = (EditText) findViewById(R.id.txtEmployeeNumber);
            ImageView ivFingerPrint = (ImageView) findViewById(R.id.fpImage);
            Button btnScanear = (Button) findViewById(R.id.btnScanear);
            Button btnGuardar = (Button) findViewById(R.id.btnGuardar);
            // Deshabilitamos la modificación del numero de empleado
            txtEmployeeNumber.setEnabled(false);
            // Desencriptamos a bytes la imagen de la huella
            byte[] decodedHuella = Base64.decode(employee.getHuella().getBytes(), 0);
            // Generamos un bitmap a partir de los bytes
            Bitmap bmpHuella = BitmapFactory.decodeByteArray(decodedHuella, 0, decodedHuella.length);
            // Guardamos los datos en cada uno de los elementos
            txtCompanyID.setText(employee.getEmpresa());
            txtEmployeeFullname.setText(employee.getNombre());
            txtEmployeeNumber.setText(employee.getLegajo());
            ivFingerPrint.setImageBitmap(bmpHuella);
            // Ocultamos el boton de lectura de huella, al modificar un empleado no es lógico modificar la huella
            btnScanear.setVisibility(View.INVISIBLE);
            ivFingerPrint.setVisibility(View.INVISIBLE);
            // Establecemos el modo de guardado en modificacion
            // Cambiamos el titulo de la activity
            setTitle(R.string.title_activity_modify);
            modifyEmployee = true;
            // Habilitamos el botón de guardar
            btnGuardar.setEnabled(true);
            return true;
        }
        return false;
    }

    public void btnGuardarOnClickHandler(View view){
        if(modifyEmployee){
            ActualizarEmpleado();
        } else {
            GuardarEmpleado();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            fpdev.CloseDevice();
            Toast.makeText(getApplicationContext(),"Disp Cerrado",Toast.LENGTH_SHORT).show();
            isopening=false;
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void TimerStop(){
        if (mTimer!=null){
            mTimer.cancel();
            mTimer = null;
            mTimerTask.cancel();
            mTimerTask=null;
        }
    }
    public void TimerStart(){
        if(mTimer==null){
            mTimer = new Timer();
        }
        if(mTimerTask == null){
            mTimerTask = new TimerTask(){
                @Override
                public void run(){
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        }
        if(mTimer!=null && mTimerTask!=null)
            mTimer.schedule(mTimerTask,50,50);
    }
    private void GuardarEmpleado(){
        try {
            if(FingerData == NO_FINGER_DATA) throw new Exception("No es posible guardar el empleado sin tomar la huella");
            // Recolectamos todos los elementos de vista
            TextView tvNombre = findViewById(R.id.txtEmployeFullname);
            TextView tvLegajo = findViewById(R.id.txtEmployeeNumber);
            TextView tvEmpresa = findViewById(R.id.txtCompanyID);
            // Instanciamos una nueva instancia de ayudante de base de datos
            EmpleadosDbHelper emp = new EmpleadosDbHelper(getApplicationContext());
            // Separamos los strings para su posterior grabado
            String empNombre = tvNombre.getText().toString();
            String empEmpresa = tvEmpresa.getText().toString();
            String empLegajo = tvLegajo.getText().toString();
            // Generamos unn nuevo empleado con todos los datos necesarios
            Empleado newEmployee = new Empleado(empEmpresa, empNombre, empLegajo, FingerData);
            // Guardamos el empleado en la base de datos
            emp.saveEmpleado(newEmployee);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo dispuesto para actualizar el empleado en la base de datos
    private void ActualizarEmpleado(){
        try{
            // Recolectamos los items que serán actualizados
            TextView tvNombre = (TextView) findViewById(R.id.txtEmployeFullname);
            TextView tvLegajo = (TextView) findViewById(R.id.txtEmployeeNumber);
            TextView tvEmpresa = (TextView) findViewById(R.id.txtCompanyID);
            // Guardamos los datos de las etiquetas en strings
            String strNombre = tvNombre.getText().toString();
            String strLegajo = tvLegajo.getText().toString();
            String strEmpresa = tvEmpresa.getText().toString();
            // Generamos un nuevo empleado
            Empleado updateEmployee = new Empleado(strEmpresa, strNombre, strLegajo, null );
            // Generamos un nuevo objeto de ayudante de base de datos
            EmpleadosDbHelper empDB = new EmpleadosDbHelper(getApplicationContext());
            // Actualizamos la base de datos
            empDB.UpdateEmpleado(updateEmployee);
            // Preparamos la nueva actividad donde muestre nuevamente el listado de empleados
            Intent intentVerEmpleados = new Intent(this, ActivityShowEmployees.class);
            // Iniciamos la actividad basada en este intent
            startActivity(intentVerEmpleados);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    private void initHandler(){
        handler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1: {
                        switch(workType){
                            case WORKTYPE_ENROL:{
                                TimerStop();
                                int k=totalCount % 2;
                                int ret=fpdev.FPGetImage(0xffffffff);
                                if(ret!=0){
                                    if(k!=0){
                                        totalCount++;
                                        Toast.makeText(getApplicationContext(),R.string.txt_fp1,Toast.LENGTH_SHORT).show();
                                    }
                                    TimerStart();
                                }else{
                                    if(k==0){
                                        if(true){
                                            if(fpdev.FPUpImage(0xffffffff,rawdata,rawsize)==0){
                                                fpdev.FPImageToBitmap(rawdata,bmpdata);
                                                Bitmap bm1= BitmapFactory.decodeByteArray(bmpdata, 0, 74806);
                                                ivImage.setImageBitmap(bm1);
                                            }
                                        }
                                        if(fpdev.FPGenChar(0xffffffff,0x01)!=0){

                                            Toast.makeText(getApplicationContext(),"Falla en leer la huella",Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if(fpdev.FPUpChar(0xffffffff,0x01,tpdata,tpsize)!=0){
                                            Toast.makeText(getApplicationContext(),"Falla en leer la huella",Toast.LENGTH_SHORT).show();
                                            return ;
                                        }
                                        System.arraycopy(tpdata,0,refdata,enrolCount*256,256);
                                        enrolCount++;
                                        totalCount++;



                                        if(enrolCount>=(ENROL_NUM)){
                                            // Guardamos el dato de la huella en una variable
                                            FingerData = Base64.encodeToString(refdata,0);
                                            // Obtenemos el boton guardar
                                            Button btnGuardar = (Button) findViewById(R.id.btnGuardar);
                                            Button btnScanear = (Button) findViewById(R.id.btnScanear);
                                            // Habilitamos el botón guardar y deshabilitamos el botón scanear
                                            btnGuardar.setEnabled(true);
                                            btnScanear.setEnabled(false);
                                            // guardarenDB(Base64.encodeToString(refdata,0));
                                            Toast.makeText(getApplicationContext(),"Se ha tomado la Huella con exito",Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getApplicationContext(),"Quite el Dedo",Toast.LENGTH_SHORT).show();
                                            TimerStart();
                                        }
                                    }else{
                                        TimerStart();
                                    }
                                }
                            }
                            break;
                            case WORKTYPE_MATCH:{
                                TimerStop();
                                int ret=fpdev.FPGetImage(0xffffffff);
                                if(ret!=0){
                                    TimerStart();
                                }else{
                                    if(true){
                                        if(fpdev.FPUpImage(0xffffffff,rawdata,rawsize)==0){
                                            fpdev.FPImageToBitmap(rawdata,bmpdata);
                                            Bitmap bm1=BitmapFactory.decodeByteArray(bmpdata, 0, 74806);
                                            ivImage.setImageBitmap(bm1);
                                        }
                                    }
                                    if(fpdev.FPGenChar(0xffffffff,0x01)!=0){
                                        Toast.makeText(getApplicationContext(),"Falla en leer la huella",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if(fpdev.FPUpChar(0xffffffff,0x01,tpdata,tpsize)!=0){
                                        Toast.makeText(getApplicationContext(),"Falla en leer la huella",Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    System.arraycopy(tpdata,0,matdata,0,256);
                                    Toast.makeText(getApplicationContext(),"Se ha tomado la Huella con exito",Toast.LENGTH_SHORT).show();
                                    //if(FPMatch.getInstance().MatchTemplateOne(refdata, matdata,60)){
                                    if(io.fgtit.fpcore.FPMatch.getInstance().MatchTemplateW4u(refdata, matdata,60,getApplicationContext())){
                                        // agregar el post a la fichada
                                        Toast.makeText(getApplicationContext(),R.string.txt_fichaok,Toast.LENGTH_SHORT).show();

                                    }else{
                                        Toast.makeText(getApplicationContext(),R.string.txt_fichafail,Toast.LENGTH_SHORT).show();

                                    }




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
}
