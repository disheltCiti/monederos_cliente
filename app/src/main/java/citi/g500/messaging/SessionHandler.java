package citi.g500.messaging;

import citi.g500.wrappers.Beacon;
import citi.g500.wrappers.BeaconStructure;

/**
 * Created by ftorres on 22/09/2016.
 */
public class SessionHandler {


    public static final String AUTH_SERVER_URL = "https://172.31.99.119:9444/oauth2/token/";
    public static final String CLIENT_KEY = "fsgbTWNTmQ7ev64W71Ctn0vjLmUa";
    public static final String PRIVATE_KEY = "Ykqxtrx6bAvw1Qo5a5nE2cvmMKca";
    public static final String BASE_URL = "http://172.31.99.119:8282/internal/";
    public static String ACCESS_TOKEN = "";
    public static String TOKEN_TYPE = "";
    public static final String LOGIN_RESOURCE = "login/";
    public static final String DATA_RESOURCE = "getBalanceAndPoints/";
    public static final String PUMP_BYBEACON_RESOURCE = "getGasPumpIdByBeacon/";
    public static final String CONTENT_TYPE = "application/json";
    public static final String PREFERENCES_TAG = "G500_TAG";
    public static final int QR_HEIGHT = 300;
    public static final int QR_WIDTH = 300;
    public static String USER_ID = "";
    public static String USER_BALANCE = "";
    public static String USER_POINTS = "";
    public static String USER_NAME = "";
    public static String USER_GAS_PUMP_ID = "";
    public static String USER_GAS_STATION_ID = "";
    public static String USER_GAS_STATION_NAME = "";
    public static BeaconStructure beaconStructure = new BeaconStructure(3);
    public static String CLOSEST_GAS_PUMP = "";
    public static String CLOSEST_GAS_STATION = "";
    public static Beacon CLOSEST_BEACON;
    public static boolean FLAG = false;
}
