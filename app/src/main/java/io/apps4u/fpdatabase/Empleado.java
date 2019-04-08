package io.apps4u.fpdatabase;

import android.content.ContentValues;

public class Empleado {
    private String _legajo;

    private String _fullname;

    private String _fingerprint;

    private String _managerid;

    public String get_legajo() {
        return _legajo;
    }

    public void set_legajo(String _legajo) {
        this._legajo = _legajo;
    }

    public String get_fullname() {
        return _fullname;
    }

    public void set_fullname(String _fullname) {
        this._fullname = _fullname;
    }

    public String get_fingerprint() {
        return _fingerprint;
    }

    public void set_fingerprint(String _fingerprint) {
        this._fingerprint = _fingerprint;
    }

    public String get_managerid() {
        return _managerid;
    }

    public void set_managerid(String _managerid) {
        this._managerid = _managerid;
    }

    public Empleado() { }

    public Empleado(String paramLegajo, String paramFullname, String paramFingerprint, String paramManager){
        _legajo = paramLegajo;
        _fullname = paramFullname;
        _fingerprint = paramFingerprint;
        _managerid = paramManager;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(EmpleadoDB.TableDefinition.LEGAJO, get_legajo());
        values.put(EmpleadoDB.TableDefinition.FULLNAME, get_fullname());
        values.put(EmpleadoDB.TableDefinition.FINGERPRINT, get_fingerprint());
        values.put(EmpleadoDB.TableDefinition.MANAGER_ID, get_managerid());
        return values;
    }
}
