package io.apps4u.fpdatabase;

public class SignUp {
    private int _id;

    private Empleado _empleado;

    private Coordinate _coordinates;

    private String _details;

    private String _address;

    private String _timestamp;

    private boolean _registered_on_server;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Empleado get_empleado() {
        return _empleado;
    }

    public void set_empleado(Empleado _empleado) {
        this._empleado = _empleado;
    }

    public Coordinate get_coordinates() {
        return _coordinates;
    }

    public void set_coordinates(Coordinate _coordinates) {
        this._coordinates = _coordinates;
    }

    public String get_details() {
        return _details;
    }

    public void set_details(String _details) {
        this._details = _details;
    }

    public String get_address() {
        return _address;
    }

    public void set_address(String _address) {
        this._address = _address;
    }

    public String get_timestamp() {
        return _timestamp;
    }

    public void set_timestamp(String _timestamp) {
        this._timestamp = _timestamp;
    }

    public boolean is_registered_on_server() {
        return _registered_on_server;
    }

    public void set_registered_on_server(boolean _registered_on_server) {
        this._registered_on_server = _registered_on_server;
    }
}
