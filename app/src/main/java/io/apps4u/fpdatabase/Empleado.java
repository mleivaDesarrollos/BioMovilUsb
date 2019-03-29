package io.apps4u.fpdatabase;

import android.content.ContentValues;

public class Empleado {
    private String nombre;
    private String legajo;
    private String huella;
    private String empresa;

    public  Empleado(String empresa, String nombre,String legajo,String huella){
        this.empresa = empresa;
        this.nombre = nombre;
        this.legajo = legajo;
        this.huella = huella;
    }

    public java.lang.String getHuella() {
        return huella;
    }

    public java.lang.String getLegajo() {
        return legajo;
    }

    public java.lang.String getNombre() {
        return nombre;
    }

    public String getEmpresa(){ return empresa;}
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(EmpleadoDB.EmpleadoRow.Nombre, nombre);
        values.put(EmpleadoDB.EmpleadoRow.Legajo, legajo);
        values.put(EmpleadoDB.EmpleadoRow.Huella, huella);
        values.put(EmpleadoDB.EmpleadoRow.Empresa, empresa);

        return values;
    }
}
