package com.example.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class BlueToothOperation {
    private BluetoothAdapter mBluetoothAdapter;
    private Map<String,BluetoothDevice> searchedDevices;
    private Set<BluetoothDevice> bondedDevices;


    public BlueToothOperation(Context context1){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bondedDevices = mBluetoothAdapter.getBondedDevices();
        searchedDevices=new HashMap<>();
    }

    public boolean isEnable(){
        return mBluetoothAdapter.isEnabled();
    }

    public Map searchDevice(){
        mBluetoothAdapter.startDiscovery();
        System.out.println(new Date()+"开始扫描");
        //三秒后结束扫描
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.cancelDiscovery();
                System.out.println(new Date()+"停止扫描");
            }
        }, 5000);
        return searchedDevices;
    }

    public void connect(String deviceName){
        BluetoothDevice bluetoothDevice=searchedDevices.get(deviceName);

    }

    public Set<BluetoothDevice> getBondedDevices() {
        return bondedDevices;
    }

    public void addSearchedDevice(String name,BluetoothDevice device){
        searchedDevices.put(name,device);
    }
}
