package io.apps4u.fpdatabase;

import android.content.ContentValues;

public class Empleado {
    private String nombre;
    private String legajo;
    private String huella;
    private String empresa;

    public Empleado(){

    }

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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public void setHuella(String huella) {
        this.huella = huella;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEmpresa(){ return empresa;}
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(EmpleadoDB.TableDefinition.Nombre, nombre);
        values.put(EmpleadoDB.TableDefinition.LEGAJO, legajo);
        values.put(EmpleadoDB.TableDefinition.FINGERPRINT, huella);
        values.put(EmpleadoDB.TableDefinition.Empresa, empresa);

        return values;
    }
}
