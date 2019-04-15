package io.apps4u.fpmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.apps4u.fpdatabase.DatabaseHelper;
import io.apps4u.fpdatabase.Manager;
import io.apps4u.fpdatabase.ManagerDB;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CheckDatabase();
        Manager recentlyLogged = getManagerRecentlyLogged();
        if(recentlyLogged != null){
            // Si el logueo es distinto a null, significa que el método devolvió una variable, por lo que guardamos esa variable en los datos de Session
            Session CurrentSession = (Session) getApplication();
            // Establecemos el administrador de la actual sesion
            CurrentSession.loggedManager = recentlyLogged;
            // Levantamos el intent
            Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
            // Iniciamos la actividad
            startActivity(intent);
        } else {
            setupActionBar();
            // Set up the login form.
            mEmailView = (AutoCompleteTextView) findViewById(R.id.txtUsername);
            populateAutoComplete();

            mPasswordView = (EditText) findViewById(R.id.txtPassword);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            Button mEmailSignInButton = (Button) findViewById(R.id.btnLogin);
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);
            // Validamos si estamos modo debuggeo

            CheckDebugMode();
        }
    }

    private void CheckDatabase(){
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
    }

    private Manager getManagerRecentlyLogged(){
        // Levantamos una nueva instancia de base de datos
        ManagerDB mDB = new ManagerDB(getApplicationContext());
        // Obtenemos el administrador
        Manager mng = mDB.GetLastLoggedManager();
        // Validamos si hay algún usuario logueado
        if(mng != null){
            // Obtenemos la fecha actual
            Date nowTime = new Date();
            // Obtenemos la diferencia de tiempo en milisegundos
            long diffMs = Math.abs(nowTime.getTime() - mng.get_last_login().getTime());
            if(!Session.DEBUG){
                // Calculamos la diferencia en dias
                long diff = TimeUnit.DAYS.convert(diffMs, TimeUnit.MILLISECONDS);
                // Validamos si la diferencia en dias es menor o igual al máximo
                if(diff <= ManagerDB.DAYS_TO_KEEP_LOGGED){
                    return mng;
                }
            } else {
                // En modo debug, validamos si en 10 segundos se cierra la sesión
                long diff = TimeUnit.SECONDS.convert(diffMs, TimeUnit.MILLISECONDS);
                // Calculamos que la diferencia sea menor que 10
                if(diff <= 120){
                    return mng;
                }
            }
        }
        return null;
    }

    private void CheckDebugMode(){
        if(Session.DEBUG){
            // Obtenemos el usuario Manger de debug
            Manager debugManager = Manager.DebuggingManager();
            // Establecemos las credenciales en los campos de logueo
            mEmailView.setText(debugManager.get_username());
            mPasswordView.setText(debugManager.get_password());
        }
    }

    private void populateAutoComplete() {
        // Levantmaos una instancia de gestor de base de datos
        ManagerDB mDB = new ManagerDB(getApplicationContext());
        // Recolectamos el administrador de la base de datos
        Manager lastLoggedManager = mDB.GetLastLoggedManager();
        if(lastLoggedManager != null){
            // Establecemos el mail del usuario en el campo de ingreso
            mEmailView.setText(lastLoggedManager.get_username());
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            // TODO: alert the user with a Snackbar/AlertDialog giving them the permission rationale
            // To use the Snackbar from the design support library, ensure that the activity extends
            // AppCompatActivity and uses the Theme.AppCompat theme.
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                // Consultamos por API
                Manager loggedManager = APIRequests.Login(mEmail, mPassword);
                // Validamos si viene cargado con un valor el administrador
                if(loggedManager == null) {
                    // TODO Validación adicional sobre base de datos si la validación no es correcta se devuelve falso
                    return false;
                }
                // Cargamos el ayudante de base de datos
                ManagerDB mDb = new ManagerDB(getApplicationContext());
                // Si llegamos a esta instancia, significa que hay que registrar una nueva fecha
                loggedManager.set_last_login(new Date());
                // Validamos si el usuario ya se encuentra cargado en la base de datos
                if(mDb.IsAlreadySaved(loggedManager.get_username())){
                    // Ejecutamos la actualización de la base de datos
                    mDb.Update(loggedManager);
                } else{ // Si no está cargado en la base de datos se ingresa el usuario
                    mDb.Add(loggedManager);
                }
                // Si el logueo es distinto a null, significa que el método devolvió una variable, por lo que guardamos esa variable en los datos de Session
                Session CurrentSession = (Session) getApplication();
                // Establecemos el administrador de la actual sesion
                CurrentSession.loggedManager = loggedManager;
            } catch(Exception e) {
                Log.e("UserLoginTask", e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
                // Iniciamos la actividad
                startActivity(intent);
                // Finalizamos la presente actividad
                //finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

