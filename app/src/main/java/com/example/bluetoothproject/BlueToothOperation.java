package com.example.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class BlueToothOperation {
    private BluetoothAdapter mBluetoothAdapter;
    private Map<String,BluetoothDevice> searchedDevices;
    private Set<BluetoothDevice> bondedDevices;


    public BlueToothOperation(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        searchedDevices=new HashMap<>();
    }

    public boolean isEnable(){
        return mBluetoothAdapter.isEnabled();
    }

    public void searchDevice(){
        searchedDevices.clear();
        mBluetoothAdapter.startDiscovery();
        System.out.println(new Date()+"开始扫描");
        //三秒后结束扫描
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.cancelDiscovery();
                System.out.println(new Date()+"停止扫描");
            }
        }, 10000);
    }

    public void bound(String deviceName){
        BluetoothDevice device=searchedDevices.get(deviceName);
        if(device==null){
            System.out.println("设备为空！！！！！");
        }
        boundDetail(device);
        //device.createRfcommSocketToServiceRecord();
    }

    public void boundDetail(BluetoothDevice device){
        //配对之前把扫描关闭
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        //判断设备是否配对，没有配对再配，配对了就不需要配了
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            try {
                Method createBondMethod = device.getClass().getMethod("createBond");
                Boolean returnValue = (Boolean) createBondMethod.invoke(device);
                returnValue.booleanValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void disable(){
        mBluetoothAdapter.disable();
    }

    public Set<BluetoothDevice> getBondedDevices() {
        bondedDevices = mBluetoothAdapter.getBondedDevices();
        return bondedDevices;
    }

    public Map<String, BluetoothDevice> getSearchedDevices() {
        return searchedDevices;
    }

    public void addSearchedDevice(String name, BluetoothDevice device){
        System.out.println("加入新设备："+name);
        searchedDevices.put(name,device);
    }
}
