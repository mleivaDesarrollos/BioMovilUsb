package io.apps4u.fpmobile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import fgtit.fpengine.fpdevice;

public class Enroll extends Activity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll); ivImage= (ImageView)findViewById(R.id.fpImage);
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
    public void guardarenDB(String content){
        try {
            TextView nombre = findViewById(R.id.txtEmployeFullname);
            TextView legajo = findViewById(R.id.txtEmployeeNumber);
            EmpleadosDbHelper emp = new EmpleadosDbHelper(getApplicationContext());
            emp.saveEmpleado(new Empleado(nombre.getText().toString(),legajo.getText().toString(), content));

        } catch (Exception e) {
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
                                            guardarenDB(Base64.encodeToString(refdata,0));
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
                                    if(com.fgtit.fpcore.FPMatch.getInstance().MatchTemplateW4u(refdata, matdata,60,getApplicationContext())){
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
