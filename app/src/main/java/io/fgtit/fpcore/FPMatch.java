package com.fgtit.fpcore;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.util.Base64;
import android.util.Log;

import java.util.ArrayList;

import io.apps4u.fpdatabase.Empleado;
import io.apps4u.fpdatabase.EmpleadoDB;
import io.apps4u.fpmobile.Session;

public class FPMatch {

    private static FPMatch mMatch = null;

    public static FPMatch getInstance() {
        if (mMatch == null) {
            mMatch = new FPMatch();
        }
        return mMatch;
    }

    public native int InitMatch(int inittype, String initcode);

    public native int MatchTemplate(byte[] piFeatureA, byte[] piFeatureB);

    public boolean MatchTemplateOne(byte[] piEnl, byte[] piMat, int score) {
        int n = piEnl.length / 256;
        byte[] tmp = new byte[256];
        for (int i = 0; i < n; i++) {
            System.arraycopy(piEnl, i * 256, tmp, 0, 256);
            if (MatchTemplate(tmp, piMat) >= score) {
                return true;
            }
        }
        return false;
    }

    public Empleado MatchTemplateW4u(byte[] piEnl, byte[] piMat, int score, Context ctx, Application apc) {
        try{
            EmpleadoDB em = new EmpleadoDB(ctx);
            // Levantamos los datos de session
            Session sessionInfo = (Session) apc;
            // Levantamos todos los empleados
            ArrayList<Empleado> lstEmpleados = em.GetAll(sessionInfo.loggedManager.get_legajoId());
            for (Empleado employee : lstEmpleados) {
                String huelladb = employee.get_fingerprint();
                byte hu[] = Base64.decode(huelladb, 0);
                int n = hu.length / 256;
                byte[] tmp = new byte[256];
                for (int i = 0; i < n; i++) {
                    System.arraycopy(hu, i * 256, tmp, 0, 256);
                    int cal = MatchTemplate(tmp, piMat);
                    if (cal >= score) {
                        return employee;
                    }
                }
            }
            return null;
        }catch(Exception e){
            Log.e("ErrorMatch", e.getMessage());
            return null;
        }
    }


    static {
        System.loadLibrary("fgtitalg");
        System.loadLibrary("fpcore");
    }


}
