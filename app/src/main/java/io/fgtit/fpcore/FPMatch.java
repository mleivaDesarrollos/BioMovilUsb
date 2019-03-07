package com.fgtit.fpcore;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.util.Base64;
import android.widget.Toast;

import io.apps4u.fpmobile.EmpleadoDB;
import io.apps4u.fpmobile.EmpleadosDbHelper;
import io.apps4u.fpmobile.MainActivity;

public class FPMatch {

	private static FPMatch mMatch=null;
	
	public static FPMatch getInstance(){
		if(mMatch==null){
			mMatch=new FPMatch();
		}
		return mMatch;
	}

	public native int InitMatch( int inittype, String initcode);
	public native int MatchTemplate( byte[] piFeatureA, byte[] piFeatureB);
	
	public boolean MatchTemplateOne( byte[] piEnl, byte[] piMat,int score){
		int n=piEnl.length/256;
		byte[] tmp=new byte[256];
		for(int i=0;i<n;i++){
			System.arraycopy(piEnl,i*256, tmp, 0, 256);
			if(MatchTemplate(tmp,piMat)>=score){
				return true;
			}
		}
		return false;
	}
	public boolean MatchTemplateW4u( byte[] piEnl, byte[] piMat,int score, Context ctx){



	    EmpleadosDbHelper em = new EmpleadosDbHelper(ctx);
								Cursor c = em.getTodosEmpleados();
								while (c.moveToNext()){
									String huelladb = c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Huella));
								    byte hu[] =  Base64.decode(huelladb,1);//c.getString(c.getColumnIndex(EmpleadoDB.EmpleadoRow.Huella)).getBytes();
                                    int n=hu.length/256;
                                    byte[] tmp=new byte[256];
                                    for(int i=0;i<n;i++){
                                        System.arraycopy(hu,i*256, tmp, 0, 256);
                                        int cal = MatchTemplate(tmp,piMat);
                                        if(cal>=score){
                                            return true;
                                        }
                                    }
                                }
		                        return false;
	}

	
	static {
		System.loadLibrary("fgtitalg");
		System.loadLibrary("fpcore");
	}


}
