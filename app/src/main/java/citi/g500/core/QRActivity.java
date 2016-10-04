package citi.g500.core;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import citi.g500.messaging.SessionHandler;
import citi.g500.messaging.UsageClient;
import citi.g500.util.CustomUtility;
import citi.g500.wrappers.Beacon;
import citi.g500.R;

/**
 * Created by ftorres on 22/09/2016.
 */
public class QRActivity extends AppCompatActivity {

    protected static final String TAG = "QRActivity";

    private UsageClient client;
    private String balance;
    private String points;
    private String valid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.g500_qr_activity);
        //Setting toolbar
        this.settingToolbar((Toolbar) findViewById(R.id.my_toolbar), "Generar QR", Color.WHITE);
        //Setting boxes
        this.settingBoxes();
        //Geofence request
        client = new UsageClient(this, QRActivity.class);
        //Init listeners
        this.initListeners();
        //Active
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        notificationManager.cancelAll();
    }

    public void doGeofence() {
        //Showing QR Code
        showConfirmationForQR(balance, points, valid);
        //showQRCode(SessionHandler.USER_ID, SessionHandler.CLOSEST_GAS_STATION, SessionHandler.CLOSEST_GAS_PUMP, balance, points, valid);
    }

    private void settingToolbar(Toolbar toolbar, String title, int color) {
        toolbar.setTitleTextColor(color);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
    }

    private void settingBoxes() {
        ((TextView) findViewById(R.id.qr_balance)).setText(SessionHandler.USER_BALANCE);
        ((TextView) findViewById(R.id.qr_points)).setText(SessionHandler.USER_POINTS);
    }

    private void initListeners() {
        //Auto complete
        //Balance
        final AutoCompleteTextView freqOperations = (AutoCompleteTextView) findViewById(R.id.autoCompleteBalance);
        Double[] operations = new Double[]{100.00, 200.00, 500.00};
        ArrayAdapter<Double> adapter = new ArrayAdapter<Double>(this, android.R.layout.simple_spinner_dropdown_item, operations);
        freqOperations.setAdapter(adapter);
        freqOperations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOperations.showDropDown();
            }
        });
        //Points
        final AutoCompleteTextView freqPoints = (AutoCompleteTextView) findViewById(R.id.autoCompletePoints);
        Integer[] operationsPoints = new Integer[]{0, 10, 50, 100};
        ArrayAdapter<Integer> adapterPoints = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, operationsPoints);
        freqPoints.setAdapter(adapterPoints);
        freqPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqPoints.showDropDown();
            }
        });
        //Listener for qr action
        Button qrButton = (Button) findViewById(R.id.qr_createButton);
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click on Create QR");
                if (validatePaymentData()) {
                    Beacon beacon = SessionHandler.beaconStructure.getClosestBeaconWCentrality();
                    if (beacon != null) {
                        client.getGasPumpByBeacon(beacon);
                        SessionHandler.CLOSEST_BEACON = beacon;
                    } else {
                        showCustomAlertDialog("No hay beacons en rango!", "G500", "Ok");
                    }
                } else {
                    showCustomAlertDialog("Saldo o puntos insuficientes", "G500", "Ok");
                }
            }
        });
    }

    private boolean validatePaymentData() {
        this.balance = ((EditText) findViewById(R.id.autoCompleteBalance)).getText().toString();
        this.points = ((EditText) findViewById(R.id.autoCompletePoints)).getText().toString();
        this.valid = ((Spinner) findViewById(R.id.qr_spinnerValid)).getSelectedItem().toString();
        if (balance.equals("")) {
            balance = "0";
        }
        if (points.equals("")) {
            points = "0";
        }
        if (Double.parseDouble(balance) > Double.parseDouble(SessionHandler.USER_BALANCE) ||
                Double.parseDouble(points) > Double.parseDouble(SessionHandler.USER_POINTS)) {
            return false;
        } else {
            return true;
        }
    }

    private void showCustomAlertDialog(String message, String title, String okMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(title);
        builder.setPositiveButton(okMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showConfirmationForQR(final String balance, final String points, final String valid) {
        //Building alert dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Setting title and message
        builder.setMessage(Html.fromHtml(
                "Confirma la siguiente creación de código QR:" +
                        "<br><br><font color='#4286f4'><b>Valor: </b></font>$" + balance +
                        "<br><br><font color='#4286f4'><b>Válido: </b></font>" + valid + " días" +
                        "<br><br><font color='#4286f4'><b>Puntos de lealtad: </b></font>" + points + " puntos"))
                .setTitle("Confirmación");
        //Setting button ok
        builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showQRCode(SessionHandler.USER_ID, SessionHandler.CLOSEST_GAS_STATION, SessionHandler.CLOSEST_GAS_PUMP, balance, points, valid);
            }
        });
        builder.setNegativeButton("Volver", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "QR Volver Dialog");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showQRCode(String userId, String gasStation, String gasPump, String balance, String points, String valid) {
        //Building alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Inflating Layout with image
        View view = this.getLayoutInflater().inflate(R.layout.g500_alert_dialog, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.selectedImage);
        //Setting image with QR generated, giving info to coding.
        imageView.setImageBitmap(
                CustomUtility.codingAsQR(userId + ";"
                                + gasStation + ";"
                                + gasPump + ";"
                                + balance + ";"
                                + points + ";"
                                + valid,
                        SessionHandler.QR_HEIGHT,
                        SessionHandler.QR_WIDTH));
        //Setting QR code on alert dialog
        builder.setView(view);
        //Setting title and message
        builder.setMessage(Html.fromHtml(
                "<font color='#4286f4'><b>Usuario: </b></font>" + SessionHandler.USER_NAME +
                        "<br><font color='#4286f4'><b>Estación: </b></font>" + SessionHandler.USER_GAS_STATION_ID + " -- " + SessionHandler.USER_GAS_STATION_NAME +
                        "<br><font color='#4286f4'><b>Beacon: </b></font>" + SessionHandler.CLOSEST_BEACON.getMajor() + " : " + SessionHandler.CLOSEST_BEACON.getMinor() +
                        "<br><font color='#4286f4'><b>Monto: </b></font> $" + this.calculateFinalBalance(balance, points) +
                        "<br><font color='#4286f4'><b>Válidez: </b></font>" + CustomUtility.getCurrentTimeStamp() + " -- " + CustomUtility.getCurrentTimeStampPlusDate(Integer.parseInt(valid))))
                .setTitle("Código QR");
        //Setting button ok
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "QR Ok Dialog");
            }
        });
        //Create and show alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private double calculateFinalBalance(String balance, String points) {
        return Double.parseDouble(balance) + Double.parseDouble(points);
    }
}
