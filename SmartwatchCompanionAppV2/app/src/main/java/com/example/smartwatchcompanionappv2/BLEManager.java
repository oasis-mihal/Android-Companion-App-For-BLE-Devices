package com.example.smartwatchcompanionappv2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class BLEManager {
    private static String TAG = "BLEManager";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt gyatt;
    private Context m_Context;


    BLEManager(Context context){
        m_Context = context;
        // TODO: Consolidate permission requests
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Doesn't have bluetooth permission");

            ActivityCompat.requestPermissions(MainActivity.reference, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Doesn't have bluetooth scan permission");

            ActivityCompat.requestPermissions(MainActivity.reference, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
            return;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

    }

    void startScanning(){
        Log.i(TAG, "Starting scan");
        if (ActivityCompat.checkSelfPermission(m_Context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Doesn't have bluetooth scan permission");

            ActivityCompat.requestPermissions(MainActivity.reference, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
            return;
        }

        bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                try {
                    if (result.getDevice() != null) {

                        Log.i(TAG, "Remote device name: " + result.getDevice().getName());
                        if (result.getDevice().getName() != null &&
                                result.getDevice().getName().equals("ESP32 Smartwatch")) {
                            Log.i(TAG, "Selected device: " + result.getDevice());
                            bluetoothDevice = result.getDevice();
                            // bluetoothAdapter.getBluetoothLeScanner().stopScan();
                            connect();
                        }
                    }
                } catch (Exception e) {
                    Log.i(TAG, "Exception getting scan result: " + e);
                }
            }
        });
    }

    private void connect(){
        if (ActivityCompat.checkSelfPermission(m_Context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Doesn't have bluetooth scan permission");

            ActivityCompat.requestPermissions(MainActivity.reference, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
            return;
        }

        gyatt = bluetoothDevice.connectGatt(m_Context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    //TODO: handle error
                    Log.i(TAG, "Failed to connect: " + status);
                    return;
                }
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    //TODO: handle the fact that we've just connected
                    Log.i(TAG, "Connected successfully");
                }
            }
        });
    }
}
