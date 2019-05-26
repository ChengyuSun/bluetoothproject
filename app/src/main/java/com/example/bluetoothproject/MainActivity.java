package com.example.bluetoothproject;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.bluetoothproject.MESSAGE";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BlueToothOperation blueToothOperation=new BlueToothOperation(this);
    private List<String> allDevices;
    //下面是搜索到新设备的监听器
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("搜索到了设备："+device.getName());
                //将新设备添加进map
                if(device.getName()!=null)
                    blueToothOperation.addSearchedDevice(device.getName(),device);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Toast.makeText(context,"扫描完毕",Toast.LENGTH_SHORT).show();
            }
        }
    };


    private IntentFilter getIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(receiver,getIntentFilter());
        ListView listView=(ListView)findViewById(R.id.devices);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connect(position);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 开启时提醒请求获取位置权限
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }

    public void enableBlueTooth(View view){
        if(blueToothOperation.isEnable()){
            Toast.makeText(getApplicationContext(), "已打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
        Toast.makeText(getApplicationContext(), "已打开蓝牙", Toast.LENGTH_SHORT).show();
    }

    public void searchDevice(View view){
        if(!blueToothOperation.isEnable()){
            Toast.makeText(getApplicationContext(), "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        allDevices=new ArrayList<>();
        for(Object deviceName:blueToothOperation.searchDevice().keySet()){
           allDevices.add(String.valueOf(deviceName));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MainActivity.this, android.R.layout.simple_list_item_1, allDevices
        );
        TextView textView=(TextView)findViewById(R.id.deviceCount);
        if(adapter.getCount()==0){
            textView.setText("未搜索到设备");
        }
        else{
            textView.setText("");
        }
        ListView listView = (ListView) findViewById(R.id.devices);
        listView.setAdapter(adapter);
    }

    public void boundedDevices(View view){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        String message ="";
        for(BluetoothDevice bluetoothDevice:blueToothOperation.getBondedDevices()){
            message+=(bluetoothDevice.getName()+"\n");
        }
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void connect(int position){
        blueToothOperation.connect(allDevices.get(position));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
        }
    }

}
