package com.example.bluetoothproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class BoundedDeviceActivity extends AppCompatActivity {

    private TextView textView_top;
    private EditText editText_sendMessage;
    private Button button_sendMessage;
    private EditText editText_file;
    private Button button_sendFile;

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
        editText_file=(EditText)findViewById(R.id.editText_file);
        button_sendFile=(Button)findViewById(R.id.button_sendMessage);

        textView_top.setText(topMessage);
    }

    public void sendMessage(View view){
        String message=editText_sendMessage.getText().toString();
        MainActivity.blueToothOperation.sendMessage(editText_sendMessage.getText().toString());
        editText_sendMessage.setText("");
        Toast.makeText(getApplicationContext(),"消息发送成功",Toast.LENGTH_SHORT).show();
    }

    public void sendFile(View view){
        String path=editText_file.getText().toString();
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
               MainActivity.blueToothOperation.sendFile(file);
                Toast.makeText(getApplicationContext(),"文件发送成功",Toast.LENGTH_SHORT).show();
                editText_file.setText("");
                return;
            }
        }
        Toast.makeText(getApplicationContext(),"文件不存在",Toast.LENGTH_SHORT).show();
    }

    public void fileChooser(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = getPath(this, uri);
                    editText_file.setText(path);
                }
            }
        }
    }

    public String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) { final String docId = DocumentsContract.getDocumentId(uri);
//                Log.i(TAG,"isExternalStorageDocument***"+uri.toString());
//                Log.i(TAG,"docId***"+docId);
//                以下是打印示例：
//                isExternalStorageDocument***content://com.android.externalstorage.documents/document/primary%3ATset%2FROC2018421103253.wav
//                docId***primary:Test/ROC2018421103253.wav
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } // DownloadsProvider
            else if (isDownloadsDocument(uri)) { //                Log.i(TAG,"isDownloadsDocument***"+uri.toString());
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId( Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } // MediaProvider
            else if (isMediaDocument(uri)) { //                Log.i(TAG,"isMediaDocument***"+uri.toString());
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) { contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) { contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) { contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            } } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //            Log.i(TAG,"content***"+uri.toString());
            return getDataColumn(context, uri, null, null);
        } // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //            Log.i(TAG,"file***"+uri.toString());
            return uri.getPath();
        } return null;
    } /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) { Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try { cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                null);
            if (cursor != null && cursor.moveToFirst()) { final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            } } finally { if (cursor != null) cursor.close();
        } return null;
    }
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
