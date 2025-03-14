package com.example.smartwatchcompanionappv2;

import static org.ligi.tracedroid.sending.TraceDroidEmailSenderKt.sendTraceDroidStackTracesIfExist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.ligi.tracedroid.TraceDroid;

/* Foreground service responsible for communication to the BLE device, we keep this in the foreground
to allow the android device to always be communicating with the smartwatch when it appears. It could probably
function as a background service but that is untested at this point in time.  */
public class BLEService extends Service {


    private static String TAG = "BLEService";
    private BLEGATT blegatt;
    private static BLEService reference;
    public static final String CHANNEL_ID = "com.companionApp.UPDATE_SERVICE";

    public static Boolean isRunning = false;

    public void onCreate() {
        super.onCreate();
        reference = this;
        Log.i(TAG, "onCreate: Called");

        //init Tracedroid for problem reporting
        TraceDroid.INSTANCE.init(this); // passing Application Context
//        sendTraceDroidStackTracesIfExist("", this);

        //create the notification
        createNotificationChannel();

        Intent notificationIntent = new Intent(this.getApplicationContext(), BLEService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this.getApplicationContext(), 300, notificationIntent,
                        PendingIntent.FLAG_MUTABLE);

        Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID)
                .setContentTitle("ESP32 Smartwatch")
                .setContentText("BLE Gatt Server Is Running...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Started BLE Handler Service with ID:" + startId);
        isRunning = true;

        MainActivity.updateStatusText();

        try {
            blegatt = new BLEGATT(this.getApplicationContext());
            blegatt.connect(MainActivity.currentDevice);
        }catch (RuntimeException e){
            Log.i(TAG, "Failed to create BLEGatt: " + e);
            this.onDestroy();
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        BLEScanner.startScan(this.getApplicationContext());

        isRunning = false;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}