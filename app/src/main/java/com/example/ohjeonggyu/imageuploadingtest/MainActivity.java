package com.example.ohjeonggyu.imageuploadingtest;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.ohjeonggyu.imageuploadingtest.util.ImageHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import cz.msebera.android.httpclient.Header;
import kr.co.namee.permissiongen.PermissionGen;

public class MainActivity extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;

    Button button,uploadButton;
    Bitmap bitmap;
    String picturePath;
    File file;
    EditText title, content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionGen.with(MainActivity.this)
                .addRequestCode(100)
                .permissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET
                )
                .request();


        button = (Button)findViewById(R.id.imageFromGallery);

        uploadButton = (Button)findViewById(R.id.uploadButton);
        button.setOnClickListener(buttonListener);
        uploadButton.setOnClickListener(buttonListener);

        title = (EditText)findViewById(R.id.titleEditText);
        content = (EditText)findViewById(R.id.contentEditText);
    }


    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imageFromGallery:
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                    break;

                case R.id.uploadButton :
                    ImageHttpClient imageHttpClient = new ImageHttpClient();
                    file = SaveBitmapToFileCache(bitmap, Environment.getExternalStorageDirectory().getAbsolutePath()+"/temp.jpg");


                    RequestParams params = new RequestParams();
                    params.setForceMultipartEntityContentType(true);
                    params.put("title",title.getText());
                    params.put("content",content.getText());
                    try {
                        params.put("image",file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    imageHttpClient.post("post",params,handler);
                    break;
            }

        }
    };

    JsonHttpResponseHandler handler = new JsonHttpResponseHandler(){

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            Log.i("onSuccess", "hello");

            if (file.exists())
                file.delete();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
            Log.i("Fail", statusCode+"");
            Log.i("Fail", headers+"");
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

//            String[] projection = { MediaStore.Images.Media.DATA };
//
//            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(projection[0]);
//            picturePath = uri.toString();
//            picturePath = cursor.getString(columnIndex); // returns null
//            cursor.close();
//            Log.i("URI", picturePath);
        }
    }

    private class BitmapToFileAsyncTask extends AsyncTask<Bitmap, Void, File> {
        ProgressDialog dialog;
        @Override
        protected File doInBackground(Bitmap... params) {

            return null;
        }

        @Override
        protected void onPreExecute() {

            // 작업을 시작하기 전 할일
            dialog = new ProgressDialog(getApplicationContext());
            dialog.setTitle("이미지 변환중");
            dialog.setMessage("잠시만 기다리세요...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();

            super.onPreExecute();
        }
        @Override
        protected void onProgressUpdate(Void... voids) {
            // 파일 다운로드 퍼센티지 표시 작업
            // setProgressPercent(progress[0]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private File SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try
        {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return fileCacheItem;
    }
}
