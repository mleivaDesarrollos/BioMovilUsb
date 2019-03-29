package io.apps4u.fpdatabase;

import android.content.ContentValues;

public class Company {

    private String _id;
    private String _name;

    public String get_id() {
        return _id;
    }

    public String get_name() {
        return _name;
    }

    // Constructor de instancia
    public Company(String id, String name){
        _id = id;
        _name = name;
    }

    public ContentValues ToContentValues(){
        // Preparamos el objeto content para devolver
        ContentValues content =  new ContentValues();
        // Guardamos el valor
        content.put(CompanyDB.Entry.ID, _id);
        content.put(CompanyDB.Entry.NAME, _name);
        // Devolvemos el objeto procesado
        return content;
    }

}
