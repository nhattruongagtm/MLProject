package com.example.mlproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private static final int IMAGE_CAPTURE_CODE = 2;
    private TextView txt, txtCNN, txtSVM;
    private Button btnResult,btnSelectImg,btnCamera,btnGallery,btnPredict;
    private EditText n1, n2;
    private ImageView imageView;
    private final int REQUEST_CODE_CAMERA = 1;
    private Uri urlImage;
    private String path = null;
    private Python py;
    private PyObject pyObject,pyObjectCNN;
    private ProgressBar progressBar_svm,progressBar_cnn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        maping();

        // khai báo python
        if(!Python.isStarted()){
                Python.start(new AndroidPlatform(this));
            }

        // tạo instance
        py = Python.getInstance();
        // tạo obj gọi đến tên file
        pyObject = py.getModule("test_svm");
        pyObjectCNN = py.getModule("test_cnn");


        // mở thư viện ảnh
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions();
            }
        });

        // mở camera
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED){
                        String[] permission = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission,PERMISSION_CODE) ;
                    }
                    else{
                        openCamera();
                    }
                }
                else{

                }
            }
        });
        // dự đoán
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(urlImage == null){
//                    Toast.makeText(MainActivity.this, "Vui lòng chụp ảnh hoặc chọn từ thư viện!", Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    Python py = Python.getInstance();
//                    PyObject pyObject = py.getModule("test_svm");
//                    PyObject obj = pyObject.callAttr("main","example.png");
////                    PyObject obj_cnn = pyObject_CNN.callAttr("main","example3.png");
//
//                    txtSVM.setText(obj.toString());
////                    txtCNN.setText(obj_cnn.toString());
//                }

                // dự đoán cnn
                new CNNAsynTask().execute();


                // dự đoán svm
                new SVMAsynTask().execute();



            }
        });

    }
    private void requestPermissions(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                openGallery();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private void openGallery(){
        TedBottomPicker.OnImageSelectedListener listener = new TedBottomPicker.OnImageSelectedListener() {
            @Override
            public void onImageSelected(Uri uri) {
                try {
                    urlImage = uri;
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                    imageView.setImageBitmap(bmp);
//                    Toast.makeText(MainActivity.this, urlImage.toString(), Toast.LENGTH_SHORT).show();



                    File storage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File imgFile = File.createTempFile("example2",".png",storage);
                    path = imgFile.getAbsolutePath();

                    Log.d("path--------------",path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(MainActivity.this)
                .setOnImageSelectedListener(listener).create();
        tedBottomPicker.show(getSupportFragmentManager());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 2 && resultCode == RESULT_OK && data != null){

            imageView.setImageURI(urlImage);
            Toast.makeText(this, urlImage.toString(), Toast.LENGTH_SHORT).show();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void maping(){
        imageView = findViewById(R.id.img);
        btnCamera = findViewById(R.id.camera);
        btnGallery = findViewById(R.id.gallery);
        btnPredict = findViewById(R.id.btnPredict);
        txtCNN = findViewById(R.id.txtCNN);
        txtSVM = findViewById(R.id.txtSVM);
        progressBar_svm = findViewById(R.id.progress_circular);
        progressBar_cnn = findViewById(R.id.progress_circular1);
    }
    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"a");
        values.put(MediaStore.Images.Media.DESCRIPTION,"b");
        urlImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,urlImage);
        startActivityForResult(intent,IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }
                else {
                    Toast.makeText(this, "Không có quyền truy cập camera!", Toast.LENGTH_SHORT).show();
                }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private class SVMAsynTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            String predict = "";

            PyObject obj = pyObject.callAttr("main","example.png");

            predict = obj.toString();

            return predict;
        }

        @Override
        protected void onPreExecute() {
            txtSVM.setVisibility(View.GONE);
            progressBar_svm.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            txtSVM.setVisibility(View.VISIBLE);
            progressBar_svm.setVisibility(View.GONE);
            txtSVM.setText(s);
        }
    }
    private class CNNAsynTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            String predict = "";

            PyObject obj_cnn = pyObjectCNN.callAttr("main","example.png");

            predict = obj_cnn.toString();

            return predict;
        }

        @Override
        protected void onPreExecute() {
            txtCNN.setVisibility(View.GONE);
            progressBar_cnn.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            txtCNN.setVisibility(View.VISIBLE);
            progressBar_cnn.setVisibility(View.GONE);
            txtCNN.setText(s);
        }
    }
}