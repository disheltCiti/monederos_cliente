package citi.g500.messaging;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import citi.g500.wrappers.Beacon;

/**
 * Created by ftorres on 28/09/2016.
 */
public class JsonHandler {

    public static String loginMessage(String user, String password) {
        JSONObject entry = new JSONObject();
        JSONObject content = new JSONObject();
        try {
            content.put("user", user);
            content.put("password", password);
            entry.put("Entry", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entry.toString();
    }

    public static String getDataMessage (String id){
        JSONObject entry = new JSONObject();
        JSONObject content = new JSONObject();
        try {
            content.put("idUser", id);
            entry.put("Entry", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entry.toString();
    }

    public static String getBeaconMessage (Beacon beacon){
        JSONObject entry = new JSONObject();
        JSONObject content = new JSONObject();
        try {
            content.put("uui", beacon.getUui());
            content.put("major", beacon.getMajor());
            content.put("minor", beacon.getMinor());
            entry.put("Entry", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entry.toString();
    }
}
