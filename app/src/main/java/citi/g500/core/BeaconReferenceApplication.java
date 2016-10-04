package citi.g500.core;

/**
 * Created by ftorres on 15/09/2016.
 */

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import citi.g500.R;
import citi.g500.messaging.SessionHandler;

/**
 * Created by dyoung on 12/13/13.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier {

    private static final String TAG = "BeaconReferenceApp";
    private static final String iBeaconLayout = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private boolean haveDetectedBeaconsSinceBoot = false;
    private LoginActivity monitoringActivity = null;


    public void onCreate() {
        super.onCreate();
        this.initBeaconManager();
        this.initBatterySaver("citiRegion");
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "Did enter region.");
        Log.d(TAG, "Auto launching MainActivity:" + this.checkActiveActivity());
        if (!haveDetectedBeaconsSinceBoot && !SessionHandler.FLAG) {
            //Launch the main activity when I found a Beacon
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
            haveDetectedBeaconsSinceBoot = true;
            SessionHandler.FLAG = true;
        } else {
            if (monitoringActivity != null) {
                /* If the Monitoring Activity is visible, we log info about the beacons we have
                 seen on its display */
                //monitoringActivity.showMessage("I see a beacon again");
            } else {
                // If we have already seen beacons before, but the monitoring activity is not in
                // the foreground, we send a notification to the user on subsequent detections.
                Log.d(TAG, "Sending notification.");
                sendNotification();
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if (monitoringActivity != null) {
            Log.d(TAG, "I no longer see a beacon.");
            //monitoringActivity.showMessage("I no longer see a beacon.");
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        if (monitoringActivity != null) {
            Log.d(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            //monitoringActivity.showMessage("I have just switched from seeing/not seeing beacons: " + state);
        }
    }

    private void sendNotification() {
        //Set notification options
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("G500")
                        .setContentText("Estación G500 cercana!")
                        .setSmallIcon(R.drawable.icon_reload)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        //Turn on screen
        PowerManager.WakeLock wl = ((PowerManager) getSystemService(POWER_SERVICE)).
                newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "example");
        wl.acquire(3000);
        wl.release();
        //Set TaskBuilder to set the target activity
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (SessionHandler.USER_ID.equals("")) {
            stackBuilder.addNextIntent(new Intent(this, LoginActivity.class));
        } else {
            stackBuilder.addNextIntent(new Intent(this, MenuActivity.class));
        }
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        //Build notification
        notificationManager.notify(1, builder.build());
    }

    private void initBeaconManager() {
        Log.d(TAG, "Setting up Beacon Manager");
        //Init BeaconManager
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(iBeaconLayout));
        //Set the duration of the scan to be 1.1 seconds
        beaconManager.setBackgroundScanPeriod(1100l);
        //Set the time between each scan to be 1 hour (3600 seconds)
        beaconManager.setBackgroundBetweenScanPeriod(2000l);
    }

    private void initBatterySaver(String regionName) {
        Log.d(TAG, "Setting up background monitoring for beacons and power saving");
        //Wake up the app when a beacon is seen
        Region region = new Region(regionName, null, null, null);
        RegionBootstrap regionBootstrap = new RegionBootstrap(this, region);
        /*Simply constructing this class and holding a reference to it in your custom Application
         class will automatically cause the BeaconLibrary to save battery whenever the application
         is not visible.  This reduces bluetooth power usage by about 60%*/
        //BackgroundPowerSaver backgroundPowerSaver = backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    public void setMonitoringActivity(LoginActivity activity) {
        this.monitoringActivity = activity;
    }

    private boolean checkActiveActivity() {
        SharedPreferences settings = getSharedPreferences(SessionHandler.PREFERENCES_TAG, 0);
        return settings.getBoolean("active", false);
    }

}