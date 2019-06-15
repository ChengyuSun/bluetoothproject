package com.example.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class BlueToothOperation {
    private static final int STATUS_CONNECT = 0x11;

    private BluetoothAdapter mBluetoothAdapter;
    private Map<String, BluetoothDevice> searchedDevices;
    private Set<BluetoothDevice> bondedDevices;

    private BluetoothServerSocket mServerSocket = null;
    private BluetoothSocket mSocket = null;

    private UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private OutputStream outputStream;
    private InputStream inputStream;

    private List<String> infos = new ArrayList<>();


    public BlueToothOperation() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        searchedDevices = new HashMap<>();
    }

    public boolean isEnable() {
        return mBluetoothAdapter.isEnabled();
    }

    public void searchDevice() {
        searchedDevices.clear();
        mBluetoothAdapter.startDiscovery();
        System.out.println(new Date() + "开始扫描");
        //三秒后结束扫描
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.cancelDiscovery();
                System.out.println(new Date() + "停止扫描");
            }
        }, 10000);
    }

    public void bound(String deviceName) {
        BluetoothDevice device = searchedDevices.get(deviceName);
        boundDetail(device);
        //device.createRfcommSocketToServiceRecord();
    }

    public void boundDetail(BluetoothDevice device) {
        //配对之前把扫描关闭
        if (mBluetoothAdapter.isDiscovering()) {
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

    //进入APP调用，初始化服务端
    public void initServerSocket() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("server_socket", mUUID);

                    mSocket = mServerSocket.accept();

                    while (mSocket != null) {
                        char[] buff = new char[1024];
                        inputStream = mSocket.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        int len = inputStreamReader.read(buff);
                        String string = new String(buff, 0, len);
                        infos.add(string);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //客户端调用，去找到相同mUUID的服务端
    public boolean connect(String deviceName) {
        final BluetoothDevice device = searchedDevices.get(deviceName);

        if (device != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        mSocket = device.createRfcommSocketToServiceRecord(mUUID);
                        //连接一个可以连接的服务器
                        mSocket.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            return true;
        }
        else
            return false;
    }

    //发送信息
    public void sendMessage(String massage) {
        //TODO sendMessage
        try {
            outputStream = mSocket.getOutputStream();
            outputStream.write(massage.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //接收信息
    public List<String> getMessage() {
        List<String> temp_infos=new ArrayList<>(infos);
        infos.clear();
        return temp_infos;
    }

    public void sendFile(File file) {
        //TODO
    }


    public void disable() {
        mBluetoothAdapter.disable();
    }

    public Set<BluetoothDevice> getBondedDevices() {
        bondedDevices = mBluetoothAdapter.getBondedDevices();
        return bondedDevices;
    }

    public Map<String, BluetoothDevice> getSearchedDevices() {
        return searchedDevices;
    }

    public void addSearchedDevice(String name, BluetoothDevice device) {
        System.out.println("加入新设备：" + name);
        searchedDevices.put(name, device);
    }
}
