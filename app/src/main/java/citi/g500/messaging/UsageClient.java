package citi.g500.messaging;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import citi.g500.core.LoginActivity;
import citi.g500.core.MenuActivity;
import citi.g500.core.QRActivity;
import citi.g500.wrappers.Beacon;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by ftorres on 21/09/2016.
 */
public class UsageClient {

    private static final String TAG = "HttpClient";
    private boolean flag;
    private final Activity activity;
    private final Class clas;

    public UsageClient(Activity activity, Class clas) {
        this.activity = activity;
        this.clas = clas;
    }

    public void login(final String user, final String password) {
        //Getting json body
        StringEntity jsonBody = null;
        try {
            jsonBody = new StringEntity(JsonHandler.loginMessage(user, password));
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Input Json wrong!");
        }
        //Flagging
        flag = true;
        //Make http request
        HttpClientHandler.post(
                activity.getApplicationContext(),
                SessionHandler.LOGIN_RESOURCE,
                jsonBody,
                SessionHandler.CONTENT_TYPE,
                new AsyncHttpResponseHandler() {
                    //Status flag
                    int status = -1;

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        Log.d(TAG, "Login Http Request Success!: " + statusCode);
                        if (statusCode == 200) {
                            status = statusCode;
                            try {
                                JSONObject document = new JSONObject(new String(response));
                                try {
                                    //Getting id from response message
                                    String id = ((JSONObject) ((JSONObject) document.get("Entries")).get("Entry")).getString("id");
                                    SessionHandler.USER_ID = id;
                                    //Setting shared preferences
                                    LoginActivity login = (LoginActivity) activity;
                                    login.setPersistentLogin(user, password);
                                    //Logging
                                    Log.d(TAG, "Login User: " + id);
                                } catch (ClassCastException e) {
                                    flag = false;
                                    Log.d(TAG, "Login User Null");
                                }
                            } catch (JSONException e) {
                                Log.d(TAG, "Json Malformed!");
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(activity.getBaseContext(), "Login failed:\nWrong Token!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Log.d(TAG, "Login Http Request failed: " + statusCode);
                        status = statusCode;
                        Toast.makeText(activity.getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "Login Http Request Finished!");
                        if (flag && status == 200) {
                            Intent intent = new Intent(activity.getApplicationContext(), MenuActivity.class);
                            activity.startActivityForResult(intent, 0);
                            activity.finish();
                        } else {
                            Toast.makeText(activity.getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getUserData(String id, final int balanceView, final int pointsView) {
        //Getting json body
        StringEntity jsonBody = null;
        try {
            jsonBody = new StringEntity(JsonHandler.getDataMessage(id));
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Input Json wrong!");
        }
        HttpClientHandler.post(
                activity.getApplicationContext(),
                SessionHandler.DATA_RESOURCE,
                jsonBody,
                SessionHandler.CONTENT_TYPE,
                new AsyncHttpResponseHandler() {
                    int status = -1;
                    String balance = "0";
                    String points = "0";
                    String name = "";

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        Log.d(TAG, "Login Http Request Success!");
                        if (statusCode == 200) {
                            status = statusCode;
                            try {
                                JSONObject document = new JSONObject(new String(response));
                                try {
                                    JSONObject entry = (JSONObject) ((JSONObject) document.get("Entries")).get("Entry");
                                    balance = entry.getString("balance");
                                    points = entry.getString("points");
                                    name = entry.getString("name");
                                    Log.d(TAG, "User Data:" + balance + "\t" + points + "\t" + name);
                                } catch (ClassCastException e) {
                                    Log.d(TAG, "No data!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Log.d(TAG, "User data http request failed!!");
                        Toast.makeText(activity.getBaseContext(), "User data failed!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFinish() {
                        if (status == 200) {
                            //Setting edit texts
                            SessionHandler.USER_BALANCE = balance;
                            SessionHandler.USER_POINTS = points;
                            SessionHandler.USER_NAME = name;
                            //Setting Buttons
                            ((Button) activity.findViewById(balanceView)).setText("Saldo\n" + balance);
                            ((Button) activity.findViewById(pointsView)).setText("Puntos\n" + points);
                            ((MenuActivity) activity).updateUserName(name);
                            Log.d(TAG, "User data request finished!");
                        }
                    }

                });
    }

    public void getGasPumpByBeacon(Beacon beacon) {
        //Getting json body
        StringEntity jsonBody = null;
        try {
            jsonBody = new StringEntity(JsonHandler.getBeaconMessage(beacon));
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Input Json wrong!");
        }
        HttpClientHandler.post(
                activity.getApplicationContext(),
                SessionHandler.PUMP_BYBEACON_RESOURCE,
                jsonBody,
                SessionHandler.CONTENT_TYPE,
                new AsyncHttpResponseHandler() {
                    int status = -1;
                    String gasPumpId = "";
                    String gasStationId = "";
                    String gasStationName = "";

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        Log.d(TAG, "Get beacon Http Request Success!");
                        if (statusCode == 200) {
                            status = statusCode;
                            try {
                                JSONObject document = new JSONObject(new String(response));
                                try {
                                    //Retrieving response
                                    JSONObject entry = (JSONObject) ((JSONObject) document.get("Entries")).get("Entry");
                                    gasPumpId = entry.getString("gaspumps_id");
                                    gasStationId = entry.getString("id");
                                    gasStationName = entry.getString("gasStationName");
                                    //Setting user data
                                    SessionHandler.USER_GAS_PUMP_ID = gasPumpId;
                                    SessionHandler.USER_GAS_STATION_ID = gasStationId;
                                    SessionHandler.USER_GAS_STATION_NAME = gasStationName;
                                    SessionHandler.CLOSEST_GAS_PUMP = gasPumpId;
                                    SessionHandler.CLOSEST_GAS_STATION = gasStationId;
                                    Log.d(TAG, "Gas Info:" + gasPumpId + "\t" + gasStationId);
                                } catch (ClassCastException e) {
                                    Log.d(TAG, "getGasPumpByBeacon: No data!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Log.d(TAG, "Get beacon Http Request Failed!");
                    }

                    @Override
                    public void onFinish() {
                        if (status == 200) {
                            //Geofencing callback
                            QRActivity parsedActivity = (QRActivity) activity;
                            parsedActivity.doGeofence();
                            Log.d(TAG, "Get beacon Http Request Finished!");
                        }
                    }
                });
    }


}
