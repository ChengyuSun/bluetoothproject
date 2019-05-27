package com.example.bluetoothproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BoundedDeviceActivity extends AppCompatActivity {

    private TextView textView_top;
    private EditText editText_sendMessage;
    private Button button_sendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boundeddevices);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String topMessage = "正在与 "+intent.getStringExtra(MainActivity.object_name)+" 进行通讯...";

        //实例化各个组件
        textView_top=(TextView)findViewById(R.id.textView_top);
        editText_sendMessage=(EditText)findViewById(R.id.editText_sendMessage);
        button_sendMessage=(Button)findViewById(R.id.button_sendMessage);

        textView_top.setText(topMessage);
    }

    public void sendMessage(View view){
        String message=editText_sendMessage.getText().toString();
        MainActivity.blueToothOperation.sendMessage(editText_sendMessage.getText().toString());
    }

}
