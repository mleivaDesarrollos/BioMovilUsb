package io.apps4u.fpdatabase;

public class Coordinate {
    private double _latitude;

    private double _longitude;

    public double get_latitude() {
        return _latitude;
    }

    public void set_latitude(double _latitude) {
        this._latitude = _latitude;
    }

    public double get_longitude() {
        return _longitude;
    }

    public void set_longitude(double _longitude) {
        this._longitude = _longitude;
    }

    public Coordinate() { }

    public Coordinate(double paramLatitude, double paramLongitude) {
        _latitude = paramLatitude;
        _longitude = paramLongitude;
    }

}
