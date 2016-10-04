package citi.g500.messaging;

import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import citi.g500.wrappers.Beacon;

/**
 * Created by ftorres on 04/10/2016.
 */
public class SyncUsageClient {

    private static final String TAG = "SyncTag";

    private final OkHttpClient client;

    public SyncUsageClient() {
        this.client = new OkHttpClient();
    }

    public void getGasInfoByBeacon(Beacon beacon) {
        Log.d(TAG, "START");
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JsonHandler.getBeaconMessage(beacon));
        Request request = new Request.Builder()
                .header("Accept", SessionHandler.CONTENT_TYPE)
                .header("Content-Type", SessionHandler.CONTENT_TYPE)
                .header("Authorization", SessionHandler.TOKEN_TYPE + " " + SessionHandler.ACCESS_TOKEN)
                .url(SessionHandler.BASE_URL + SessionHandler.PUMP_BYBEACON_RESOURCE)
                .post(body)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject document = new JSONObject(new String(response.body().string()));
            JSONObject entry = (JSONObject) ((JSONObject) document.get("Entries")).get("Entry");
            SessionHandler.USER_GAS_STATION_NAME = entry.getString("gasStationName");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
