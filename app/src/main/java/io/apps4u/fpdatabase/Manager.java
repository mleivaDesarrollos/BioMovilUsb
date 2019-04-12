package io.apps4u.fpdatabase;

import android.content.ContentValues;
import android.util.Base64;

public class Manager {
    // Información de debuggeo
    private static final String DEBUG_USERNAME = "hsilveyra@megatech.la";
    private static final String DEBUG_PASSWORD = "Hernan2019";

    private String _legajoId;
    private String _firstname;
    private String _lastname;
    private String _companyId;
    private String _companyName;
    private String _username;
    private String _password;

    public String get_firstname() {
        return _firstname;
    }

    public void set_firstname(String _firstname) {
        this._firstname = _firstname;
    }

    public String get_lastname() {
        return _lastname;
    }

    public void set_lastname(String _lastname) {
        this._lastname = _lastname;
    }

    public String get_companyId() {
        return _companyId;
    }

    public void set_companyId(String _companyId) {
        this._companyId = _companyId;
    }

    public String get_companyName() {
        return _companyName;
    }

    public void set_companyName(String _companyName) {
        this._companyName = _companyName;
    }

    public String get_legajoId() {
        return _legajoId;
    }

    public void set_legajoId(String _legajoId) {
        this._legajoId = _legajoId;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public ContentValues ToContentValues(){
        // Preparamos el ContentValue para devolver en el proceso
        ContentValues content = new ContentValues();
        // Almacenamos los valores
        content.put(ManagerDB.TableDefinition.LEGAJO, get_legajoId());
        content.put(ManagerDB.TableDefinition.FIRSTNAME, get_firstname());
        content.put(ManagerDB.TableDefinition.LASTNAME, get_lastname());
        content.put(ManagerDB.TableDefinition.COMPANY_ID, get_companyId());
        content.put(ManagerDB.TableDefinition.COMPANY_NAME, get_companyName());
        content.put(ManagerDB.TableDefinition.USERNAME, get_username());
        content.put(ManagerDB.TableDefinition.PASSWORD, get_password());
        // Devolvemos el valor procesado
        return content;
    }

    public String get_authorization(){
        // Recolectamos los datos y le aplicamos formato de HASH
        String loginDataToEncode = get_username() + ":" + get_password();
        // Obtenemos el String codificado en Base 64
        String encodedString = Base64.encodeToString(loginDataToEncode.getBytes(), 0);
        // Devolvemos el valor procesado y codificado en base64
        return "Basic " + encodedString;
    }

    // Metodo que dispone un administrador para pruebas
    public static Manager DebuggingManager(){
        // Generamos un nuevo administrador
        Manager debugManager = new Manager();
        // Establecemos los parametros del usuario
        debugManager.set_username(DEBUG_USERNAME);
        debugManager.set_password(DEBUG_PASSWORD);
        // Devolvemos el administrador generado
        return debugManager;
    }
}
