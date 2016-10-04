package citi.g500.core;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import citi.g500.messaging.SessionHandler;
import citi.g500.messaging.UsageClient;
import citi.g500.security.CustomAuthenticator;
import citi.g500.R;

/**
 * Created by ftorres on 14/09/2016.
 */
public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Setting layout and inflating
        super.onCreate(savedInstanceState);
        setContentView(R.layout.g500_main_activity);
        //Initiating progress dialog
        this.progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme);
        String[] credentials = this.checkPersistentLogin();
        //If I was logged, retrieve new token and enter menu, else enter login
        if (credentials[0] != "" && credentials[1] != "") {
            this.validateAndGo(0, credentials[0], credentials[1]);
        } else {
            this.setListenersForLoginButton("G500", "Autenticando");
        }
        Log.d(TAG, "On create");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        notificationManager.cancelAll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(this);
    }

    private void validateAndGo(int timeDelayed, final String user, final String password) {
        final Activity activity = this;
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        //Setting token
                        new CustomAuthenticator(activity).authenticate(user, password);
                    }
                }, timeDelayed);
    }

    public void showMessage(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setPersistentLogin(String user, String password) {
        SharedPreferences settings = getSharedPreferences(SessionHandler.PREFERENCES_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("user", user);
        editor.putString("password", password);
        editor.commit();
    }

    private String[] checkPersistentLogin() {
        SharedPreferences settings = getSharedPreferences(SessionHandler.PREFERENCES_TAG, 0);
        String[] credentials = new String[2];
        credentials[0] = settings.getString("user", "");
        credentials[1] = settings.getString("password", "");
        return credentials;
    }

    public void doLogin(String user, String password) {
        UsageClient httpClient = new UsageClient(this, MenuActivity.class);
        httpClient.login(user, password);
    }

    private void initProgressBar(String title, String message) {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        progressDialog.setTitle(title);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
    }

    private void setListenersForLoginButton(final String progressBarTitle, final String progressBarMessage) {
        ((Button) findViewById(R.id.btn_login)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LoginActivity.this.initProgressBar(progressBarTitle, progressBarMessage);
                LoginActivity.this.validateAndGo(
                        2000,
                        ((EditText) findViewById(R.id.input_email)).getText().toString(),
                        ((EditText) findViewById(R.id.input_password)).getText().toString());
            }
        });
    }
}
