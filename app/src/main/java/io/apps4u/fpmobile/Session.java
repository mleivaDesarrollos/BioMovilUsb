package io.apps4u.fpmobile;

import android.app.Application;

import io.apps4u.fpdatabase.Manager;

public class Session extends Application {

    // Si estamos en modo debugeo, lo verificamos desde esta variable
    public static final boolean DEBUG = true;

    public Manager loggedManager;

}
