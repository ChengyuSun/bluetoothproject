package com.example.bluetoothproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    public static final String object_name="";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    //实例化蓝牙操作类
    public static final BlueToothOperation blueToothOperation=new BlueToothOperation();
    //搜索到的设备列表
    private List<String> allDevices=new ArrayList<>();
    //绑定的设备列表
    private List<String> boundedDeviceList=new ArrayList<>();
    //界面组件
    private TextView textView;
    private ListView listView_searched;
    private Switch enableSwitch;
    private ListView listView_bounded;

    //动态权限检测
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
        }
    }

    //下面是搜索到新设备的监听器
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //将新设备添加进map
                if(device.getName()!=null){//设备名不为空时刷新列表
                    blueToothOperation.addSearchedDevice(device.getName(),device);
                    flushList();
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Toast.makeText(context,"扫描完毕",Toast.LENGTH_SHORT).show();
            }
        }
    };

    //增加过滤器
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
        //实例化需要的组件
        textView=(TextView)findViewById(R.id.textView_deviceCount);
        listView_searched = (ListView) findViewById(R.id.listView_searchedDevices);
        enableSwitch=(Switch)findViewById(R.id.switch_enable);
        listView_bounded=(ListView)findViewById(R.id.listView_boundedDevices);
        blueToothOperation.initServerSocket();
        //注册监听器
        registerReceiver(receiver,getIntentFilter());
        //列表响应
        listView_searched.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bound(position);
            }
        });

        listView_bounded.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connect(position);
            }
        });

        //开关响应
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                   enableBlueTooth();
                }else {
                    disableBluetooth();
                }
            }
        });

        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限.它在用户选择"不再询问"的情况下返回false
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
            }
        }



        //初次启动获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 开启时提醒请求获取位置权限
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }



    //开启蓝牙
    public void enableBlueTooth(){
        if(blueToothOperation.isEnable()){
            Toast.makeText(getApplicationContext(), "已打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
        Toast.makeText(getApplicationContext(), "已打开蓝牙", Toast.LENGTH_SHORT).show();
    }

    //关闭蓝牙
    public void disableBluetooth(){
        if(blueToothOperation.isEnable()){
            blueToothOperation.disable();
        }
        Toast.makeText(getApplicationContext(), "已关闭蓝牙", Toast.LENGTH_SHORT).show();
    }


    //开始搜索
    public void searchDevice(View view){
        if(!blueToothOperation.isEnable()){
            Toast.makeText(getApplicationContext(), "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        blueToothOperation.searchDevice();
    }


    //刷新搜索列表
    public void flushList(){
        allDevices.clear();
        for(Object deviceName:blueToothOperation.getSearchedDevices().keySet()){
            allDevices.add(String.valueOf(deviceName));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MainActivity.this, android.R.layout.simple_list_item_1, allDevices
        );
        if(adapter.getCount()!=0){
            listView_searched.setAdapter(adapter);
        }
    }

    //查看已绑定设备
    public void boundedDevices(View view){
        if(!blueToothOperation.isEnable()){
            Toast.makeText(getApplicationContext(), "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        boundedDeviceList=new ArrayList<>();
        for(BluetoothDevice bluetoothDevice:blueToothOperation.getBondedDevices()){
           boundedDeviceList.add(bluetoothDevice.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MainActivity.this, android.R.layout.simple_list_item_1, boundedDeviceList);
        ListView boundedDevices=(ListView)findViewById(R.id.listView_boundedDevices);
        boundedDevices.setAdapter(adapter);
    }

    //绑定设备
    public void bound(int position){
        System.out.println("列表项:"+position+"->"+allDevices.get(position));
        blueToothOperation.bound(allDevices.get(position));
    }

    //连接设备-->进行界面跳转
    public void connect(int position){
        String objectName=boundedDeviceList.get(position);
        if(blueToothOperation.connect(objectName)){
            Intent intent = new Intent(this, BoundedDeviceActivity.class);
            intent.putExtra(object_name,objectName);
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "建立连接失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void getmessage(View view){

    }

}
