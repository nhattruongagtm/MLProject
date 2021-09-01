package com.example.mlproject;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;

public class MainActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_CODE = 1;
    private static final int IMAGE_CAPTURE_CODE = 2;
    private static final int RC_DRAW = 3;
    private static final int REQUEST_IMG = 4;
    private TextView txt, txtCNN, txtSVM;
    private Button btnResult, btnSelectImg, btnCamera, btnGallery, btnPredict, btnDraw;
    private EditText n1, n2;
    private ImageView imageView;
    private final int REQUEST_CODE_CAMERA = 1;
    private Uri urlImage;
    private Python py;
    private PyObject pyObject, pyObjectCNN, pyObjectImage;
    private ProgressBar progressBar_svm, progressBar_cnn;
    private LinearLayout resultLayout;
    private boolean toogle = false;
    private BitmapDrawable drawable;
    private Bitmap bitmap;
    private String imageString = "";
    private static final int SELECT_PICTURE = 1;
    DrawFragment drawFragment;
    private String selectedImagePath;

    // current path image from camera
    private String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        maping();

        drawFragment = new DrawFragment();

        // khai báo python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // tạo instance
        py = Python.getInstance();
        // tạo obj gọi đến tên file
        pyObject = py.getModule("test_svm");
        pyObjectCNN = py.getModule("test_cnn");
        pyObjectImage = py.getModule("processImage");

        // mở camera
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source.getInstance().isHaveImage =false;
                Intent intent = new Intent(MainActivity.this,AndroidCameraApi.class);
                startActivityForResult(intent,REQUEST_IMG);
//                if (checkSelfPermission(Manifest.permission.CAMERA)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{Manifest.permission.CAMERA},
//                            MY_CAMERA_REQUEST_CODE);
//                }else{
//                    openCameraProvider();
//                }
            }
        });

//        Intent intent = getIntent();
//        if(intent != null){
//
//
//            Bundle bundle = intent.getBundleExtra("img");
//            byte[] byteArray = bundle.getByteArray("img");
//
//            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//            imageView.setImageBitmap(bmp);
//    }


        // mở thư viện ảnh
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                requestPermissions();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });

        // vẽ
        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogle = !toogle;

                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                if (toogle) {
                    resultLayout.setVisibility(View.GONE);
                    ft.add(R.id.framelayout, drawFragment);
                } else {
                    ft.remove(drawFragment);
                    resultLayout.setVisibility(View.VISIBLE);
                }
                ft.commit();

            }
        });
        // dự đoán
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(drawFragment);
                ft.commit();
                resultLayout.setVisibility(View.VISIBLE);
                toogle = false;

                try {
                    drawable = (BitmapDrawable) imageView.getDrawable();
                    bitmap = drawable.getBitmap();
                    imageString = getStringImage(bitmap);

                    // hiển thị hình ảnh
                    displayImageByContours(imageString);
                    // dự đoán svm
                    new SVMAsynTask().execute(imageString);

                    // dự đoán cnn
                    new CNNAsynTask().execute(imageString);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Vui lòng chọn hình ảnh!", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    private void openCameraProvider(){
//        String fileName = "photo";
//        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        try {
//            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
//            currentPath = imageFile.getAbsolutePath();
//            Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.mlproject.fileprovider", imageFile);
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//            startActivityForResult(intent, REQUEST_IMG);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //----------------
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMG);
    }

    private void requestPermissions() {
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

    private void openGallery() {
        TedBottomPicker.OnImageSelectedListener listener = new TedBottomPicker.OnImageSelectedListener() {
            @Override
            public void onImageSelected(Uri uri) {
                try {
                    urlImage = uri;
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageView.setImageBitmap(bmp);

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
//        if(requestCode == 2 && resultCode == RESULT_OK && data != null){
//
//            imageView.setImageURI(urlImage);
//            Toast.makeText(this, urlImage.toString(), Toast.LENGTH_SHORT).show();
//
//        }

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                urlImage = selectedImageUri;
                imageView.setImageURI(urlImage);
            }
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMG) {
            if(Source.getInstance().isHaveImage){
                byte bytes[] = android.util.Base64.decode(Source.getInstance().data, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                Bitmap bmpCrop = Bitmap.createBitmap(bmp,165,485,850,245);
                imageView.setImageBitmap(bmpCrop);
            }


//            Bitmap bitmap = BitmapFactory.decodeFile(this.currentPath);
//            imageView.setImageBitmap(bitmap);

//            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void maping() {
        imageView = findViewById(R.id.img);
        btnCamera = findViewById(R.id.camera);
        btnGallery = findViewById(R.id.gallery);
        btnPredict = findViewById(R.id.btnPredict);
        txtCNN = findViewById(R.id.txtCNN);
        txtSVM = findViewById(R.id.txtSVM);
        progressBar_svm = findViewById(R.id.progress_circular);
        progressBar_cnn = findViewById(R.id.progress_circular1);
        btnDraw = findViewById(R.id.draw);
        resultLayout = findViewById(R.id.result_predict);

    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "a");
        values.put(MediaStore.Images.Media.DESCRIPTION, "b");
        urlImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, urlImage);
        startActivityForResult(intent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Không có quyền truy cập camera!", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_CAMERA_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCameraProvider();
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class SVMAsynTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String imgString = strings[0];
            String predict = "";

            PyObject obj = pyObject.callAttr("main", imgString);

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

    private class CNNAsynTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String imgString = strings[0];
            String predict = "";

            PyObject obj_cnn = pyObjectCNN.callAttr("main", imgString);

            predict = obj_cnn.toString();


//            byte data[] = android.util.Base64.decode(str,Base64.DEFAULT);
//            Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length);

//            imageView.setImageBitmap(bmp);

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

    private String getStringImage(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        byte[] imageBytes = baos.toByteArray();

        String encodeImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodeImage;

    }

    private void displayImageByContours(String image) {
        PyObject obj_img = pyObjectImage.callAttr("main", image);
        String code = obj_img.toString();

        byte data[] = android.util.Base64.decode(code, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

        imageView.setImageBitmap(bmp);
    }
}