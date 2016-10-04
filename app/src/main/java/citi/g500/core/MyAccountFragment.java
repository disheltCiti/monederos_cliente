package citi.g500.core;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import citi.g500.R;
import citi.g500.core.CustomRangeNotifier;
import citi.g500.core.MenuActivity;
import citi.g500.core.QRActivity;
import citi.g500.messaging.SessionHandler;
import citi.g500.messaging.SyncUsageClient;
import citi.g500.messaging.UsageClient;
import citi.g500.wrappers.Beacon;

public class MyAccountFragment extends Fragment implements BeaconConsumer {


    protected static final String TAG = "HomeActivity";

    private BeaconManager beaconManager;
    private Button qrButton;

    public MyAccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beaconManager = BeaconManager.getInstanceForApplication(this.getActivity());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        beaconManager.bind(this);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        qrButton = (Button) view.findViewById(R.id.button3);
        //Getting User data
        UsageClient usageClient = new UsageClient(this.getActivity(), MenuActivity.class);
        usageClient.getUserData(SessionHandler.USER_ID, R.id.button1, R.id.button2);
        //Init Listeners
        this.initListeners();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int mode) {
        return getActivity().bindService(intent, serviceConnection, mode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        NotificationManager notificationManager = ((NotificationManager) this.getActivity().getSystemService(Context.NOTIFICATION_SERVICE));
        notificationManager.cancelAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onBeaconServiceConnect() {
        CustomRangeNotifier rangeNotifier = new CustomRangeNotifier();
        rangeNotifier.setContext(this.getActivity());
        beaconManager.setRangeNotifier(rangeNotifier);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("citiRegion", null, null, null));
        } catch (RemoteException e) {
        }
    }

    private void initListeners() {
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QRActivity.class);
                startActivity(intent);
                Log.d(TAG, "Click on QR");
            }
        });
    }

    public void showCustomAlertDialog(final String message, final String title, final String okMessage) {
        Log.d(TAG, "Custom Called from activity!");
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setMessage(message).setTitle(title);
                builder.setPositiveButton(okMessage, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

}
