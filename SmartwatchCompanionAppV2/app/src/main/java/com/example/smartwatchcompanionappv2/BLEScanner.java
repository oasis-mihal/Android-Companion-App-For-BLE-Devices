package com.example.smartwatchcompanionappv2;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;


/* Initiates a pendingIntent scan in the background, allowing the android device to locate a BLE device without
the screen even being on, uses the nordic semiconductor library Android Scanner Compat

 For the most part this is just the example code provided by the library documentation on github
 */
public class BLEScanner {

    private static String TAG = "BLE";
    private static int code = 4000;

    public static void startScan(Context con) {
        Log.i(TAG, "----------------- Starting BLE Scan ---------------------------");

        if (ActivityCompat.checkSelfPermission(con, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Doesn't have bluetooth permission");

            ActivityCompat.requestPermissions(MainActivity.reference, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            return;
        }
        if (ActivityCompat.checkSelfPermission(con, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Doesn't have bluetooth scan permission");

            ActivityCompat.requestPermissions(MainActivity.reference, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
            return;
        }

        //if (ActivityCompat.checkSelfPermission(con, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        //    Log.i(TAG, "Doesn't have post notifications permission");

        //    ActivityCompat.requestPermissions(MainActivity.reference, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
        //    return;
        //}

        Intent intent = new Intent(con, BLEScanReceiver.class); // explicite intent
        intent.setAction(BLEScanReceiver.ACTION_SCANNER_FOUND_DEVICE);
//        intent.putExtra("some.extra", value); // optional
        PendingIntent pendingIntent = PendingIntent.getBroadcast(con, code, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        BluetoothManager bluetoothManager = (BluetoothManager) con.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(100)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(MainActivity.SERVICE_UUID))
                .build());
        scanner.startScan(filters, settings, pendingIntent);
    }


    public static void stopScan(Context con) {
        // To stop scanning use the same or an equal PendingIntent (check PendingIntent documentation)
        BluetoothManager bluetoothManager = (BluetoothManager) con.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

        Intent intent = new Intent(con, BLEScanReceiver.class);
        intent.setAction("com.smartwatchCompanion.bleReciever.ACTION_SCANNER_FOUND_DEVICE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(con, code, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);

        scanner.stopScan(pendingIntent);
    }
}
