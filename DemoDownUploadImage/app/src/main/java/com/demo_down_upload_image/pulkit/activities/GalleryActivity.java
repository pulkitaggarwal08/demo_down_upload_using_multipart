package com.demo_down_upload_image.pulkit.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.demo_down_upload_image.pulkit.BuildConfig;
import com.demo_down_upload_image.pulkit.R;
import com.demo_down_upload_image.pulkit.adapters.ShowImageAdapter;
import com.demo_down_upload_image.pulkit.models.Images;
import com.demo_down_upload_image.pulkit.util.AppConstant;
import com.demo_down_upload_image.pulkit.util.CommonUtil;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GalleryActivity extends AppCompatActivity {

    private String imageUri, dateFormat, getData;

    private TextView tv_fa_upload, tv_fa_add;
    private Typeface fontAwesomeFont;

    private File filepath, dir;
    private Uri fileUri;
    private String mCurrentPhotoPath;

    private List<Images> arrayList = new ArrayList<>();
    private RecyclerView rv_images;
    private ShowImageAdapter showImageAdapter;

    private List<Images> imagesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        findIds();
        setFontAwesomeFont();
        init();
    }

    private void findIds() {

        tv_fa_upload = (TextView) findViewById(R.id.tv_fa_upload);
        tv_fa_add = (TextView) findViewById(R.id.tv_fa_add);
        rv_images = (RecyclerView) findViewById(R.id.rv_images);

    }

    public void setFontAwesomeFont() {

        fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
        tv_fa_upload.setTypeface(fontAwesomeFont);
        tv_fa_add.setTypeface(fontAwesomeFont);
    }

    private void init() {

        rv_images.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

        imageUri = getIntent().getStringExtra("image");
        imagesArrayList = (ArrayList<Images>) getIntent().getSerializableExtra("imagesArrayList");
        getData = getIntent().getStringExtra("single_list");

        if (imageUri != null) {

            addImagesInList(imageUri);
        } else if (imagesArrayList != null) {

            listeners();
        } else if (imageUri == null && imagesArrayList == null && getData == null) {
            Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show();
            return;
        } else if (getData != null) {

            addImagesInList(getData);
        }

    }

    private void listeners() {

        if (imagesArrayList != null) {

            if (arrayList != null) {

                imagesArrayList.addAll(arrayList);
                arrayList.clear();

                showImageAdapter = new ShowImageAdapter(getApplicationContext(), imagesArrayList, new ShowImageAdapter.onClickListener() {
                    @Override
                    public void onClickButton(int position, int view, Images images) {

                        if (view == R.id.tv_fa_cancel) {

                            String imagePath = imagesArrayList.get(position).getImagePath().toString();
                            String removeItem = imagesArrayList.remove(position).toString();
                            showImageAdapter.notifyDataSetChanged();

                            listeners();
                        }
                    }
                });
                rv_images.setAdapter(showImageAdapter);
            }
        } else {

            showImageAdapter = new ShowImageAdapter(getApplicationContext(), arrayList, new ShowImageAdapter.onClickListener() {
                @Override
                public void onClickButton(int position, int view, Images images) {

                    if (view == R.id.tv_fa_cancel) {

                        String imagePath = arrayList.get(position).getImagePath().toString();
                        String removeItem = arrayList.remove(position).toString();
                        showImageAdapter.notifyDataSetChanged();

                        listeners();
                    }
                }
            });
            rv_images.setAdapter(showImageAdapter);
        }

        tv_fa_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                builder.setTitle("Upload!")
                        .setCancelable(false)
                        .setMessage("Are you sure you want to upload the images?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                uploadMultipleImages();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.show();

            }
        });

        tv_fa_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissions();
            }
        });
    }

    private void addImagesInList(String imageUri) {

        arrayList.add(new Images(imageUri));

        if (arrayList.size() > 4) {
            tv_fa_add.setVisibility(View.GONE);
        } else {
            tv_fa_add.setVisibility(View.VISIBLE);
        }
        listeners();
    }

    private void uploadMultipleImages() {

        uploadMultipart();
    }

    private void uploadMultipart() {

        if (arrayList.size() != 0) {

            for (Images images : arrayList) {

                String path = images.getImagePath().toString();

                //Uploading code
                try {
                    String uploadId = UUID.randomUUID().toString();

                    //Creating a multi part request
                    new MultipartUploadRequest(this, uploadId, AppConstant.UPLOAD_URL)
                            .addFileToUpload(path, "image")
//                            .addParameter("name", name)
                            .setNotificationConfig(new UploadNotificationConfig())
                            .setMaxRetries(2)
                            .startUpload();

                } catch (Exception exc) {
                    Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (imagesArrayList.size() != 0) {

            //Uploading code
            for (Images images : imagesArrayList) {

                String path = images.getImagePath().toString();

                //Uploading code
                try {
                    String uploadId = UUID.randomUUID().toString();

                    //Creating a multi part request
                    new MultipartUploadRequest(this, uploadId, AppConstant.UPLOAD_URL)
                            .addFileToUpload(path, "image")
//                    .addParameter("name", name)
                            .setNotificationConfig(new UploadNotificationConfig())
                            .setMaxRetries(2)
                            .startUpload();

                } catch (Exception exc) {
                    Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkPermissions() {

        if (CommonUtil.checkAndRequestPermission(GalleryActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                AppConstant.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)) {

            if (CommonUtil.checkAndRequestPermission(GalleryActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    AppConstant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)) {

                if (CommonUtil.checkAndRequestPermission(GalleryActivity.this,
                        Manifest.permission.CAMERA,
                        AppConstant.MY_PERMISSIONS_REQUEST_CAMERA)) {

                    getDate();

                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                        cameraIntent();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        nougatCamera();
                    } else {
                        cameraIntent();
                    }
                }
            }
        }
    }

    public void nougatCamera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = null;
        Uri photoUri = null;

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = null;
                try {
                    photoURI = FileProvider.getUriForFile(GalleryActivity.this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            createImageFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, AppConstant.REQUEST_NOUGAT_CAMERA_CODE);
            }

        }
    }

    private void getDate() {

        Date date = new Date();
        CharSequence sequence = DateFormat.format("yyMMdd_HHmmss", date.getTime());
        dateFormat = sequence.toString();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = dateFormat + ".png";
        File file = new File(getExternalCacheDir(), imageFileName);

        mCurrentPhotoPath = String.valueOf(file);
        return file;
    }


    public void cameraIntent() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(getExternalCacheDir(), String.valueOf(dateFormat) + ".png");
        fileUri = Uri.fromFile(file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, AppConstant.REQUEST_CAMERA_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case AppConstant.REQUEST_NOUGAT_CAMERA_CODE:

                Uri imageUr = Uri.parse(mCurrentPhotoPath);
                File file = new File(imageUr.getPath());
                try {
                    InputStream ims = new FileInputStream(file);

                } catch (FileNotFoundException e) {
                    return;
                }

                if (imageUri != null) {

                    String realPath = String.valueOf(imageUri);

                    addImagesInList(realPath);
                } else {
                    finish();
                }
                break;

            case AppConstant.REQUEST_CAMERA_CODE:

                String image = getImageContentUri(getApplicationContext(),
                        new File((fileUri + "").substring(7, (fileUri + "").length()))) + "";

                if (image.equals("null") || image.equals(null)) {
                    finish();
                } else {
                    Uri uriPath = Uri.parse(image);
                    String realPath = getRealPath(uriPath);

                    addImagesInList(realPath);
                }

                break;
        }
    }

    private static Uri getImageContentUri(Context context, File imageFile) {

        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ", new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public String getRealPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case AppConstant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkPermissions();
                } else {
                    CommonUtil.checkAndRequestPermission(GalleryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            AppConstant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                break;

            case AppConstant.MY_PERMISSIONS_REQUEST_CAMERA:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getDate();

                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                        cameraIntent();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        nougatCamera();
                    } else {
                        cameraIntent();
                    }
                } else {
                    CommonUtil.checkAndRequestPermission(GalleryActivity.this, Manifest.permission.CAMERA,
                            AppConstant.MY_PERMISSIONS_REQUEST_CAMERA);
                }
                break;
        }
    }


}
