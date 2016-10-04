package citi.g500.core;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import citi.g500.messaging.SessionHandler;
import citi.g500.messaging.SyncUsageClient;
import citi.g500.messaging.UsageClient;
import citi.g500.wrappers.BeaconStructure;

/**
 * Created by ftorres on 19/09/2016.
 */
public class CustomRangeNotifier implements RangeNotifier {

    protected static final String TAG = "RangeNotifier";

    private Activity activity;
    private boolean isFirstNotified = false;

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.d(TAG, "Beacon found!: " + beacons.size());
        BeaconStructure beaconStructure = SessionHandler.beaconStructure;
        for (Beacon beacon : beacons) {
            beaconStructure.addBeacon(new citi.g500.wrappers.Beacon(
                    beacon.getId1() + "",
                    beacon.getId3() + "",
                    beacon.getId2() + "",
                    beacon.getDistance()));
            Log.d(TAG, beaconStructure.toString());
        }
        if (!isFirstNotified && beaconStructure.getSize() > 0) {
            this.sendInitialConfirmation(SessionHandler.beaconStructure.getClosestBeaconWCentrality());
            isFirstNotified = true;
        }
    }

    private void sendInitialConfirmation(citi.g500.wrappers.Beacon beacon) {
        SyncUsageClient client = new SyncUsageClient();
        client.getGasInfoByBeacon(beacon);
        MenuActivity menuActivity = (MenuActivity) activity;
        menuActivity.showCustomAlertDialog(SessionHandler.USER_GAS_STATION_NAME);
    }

    public void setContext(Activity activity) {
        this.activity = activity;
    }
}
