package com.demo_down_upload_image.pulkit;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.demo_down_upload_image.pulkit.activities.GalleryActivity;
import com.demo_down_upload_image.pulkit.models.Images;
import com.demo_down_upload_image.pulkit.util.AppConstant;
import com.demo_down_upload_image.pulkit.util.CommonUtil;
import com.demo_down_upload_image.pulkit.util.RealPathUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_image;
    private Button btn_upload_images, btn_download_single_image, btn_download_multiple_images;

    private String dateFormat, image_url, multipleImageUrls[], fileName;
    public String userChoosenTask;

    private Uri fileUri;
    private String mCurrentPhotoPath;

    private static double SPACE_KB = 1024;
    private static double SPACE_MB = 1024 * SPACE_KB;
    private static double SPACE_GB = 1024 * SPACE_MB;
    private static double SPACE_TB = 1024 * SPACE_GB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findIds();
        init();

    }

    private void init() {

        getDate();
        checkPermissions();
        clickOnButtons();
    }

    private void findIds() {

        iv_image = (ImageView) findViewById(R.id.iv_image);
        btn_upload_images = (Button) findViewById(R.id.btn_upload_images);
        btn_download_single_image = (Button) findViewById(R.id.btn_download_single_image);
        btn_download_multiple_images = (Button) findViewById(R.id.btn_download_multiple_images);
    }

    private void getDate() {

        Date date = new Date();
        CharSequence sequence = DateFormat.format("yyMMdd_HHmmss", date.getTime());
        dateFormat = sequence.toString();
    }

    private void clickOnButtons() {

        btn_upload_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImages();
            }
        });

        btn_download_single_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadSingleImage();
            }
        });

        btn_download_multiple_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadMultipleImages();
            }
        });

    }

    private void uploadImages() {

        selectImage();
    }

    private void downloadSingleImage() {

        File file = new File("/sdcard/image/" + fileName + ".png");

//        if (file.exists()) {
//
//            iv_image.setImageURI(Uri.fromFile(file));
//            Toast.makeText(this, "Image Already Downloaded", Toast.LENGTH_LONG).show();
//
//        } else {
        startDownloadImage();
//        }
    }

    private void downloadMultipleImages() {

        startDownloadMultipleImages();
    }

    private void selectImage() {

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";

                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                        cameraIntent();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        nougatCamera();
                    } else {
                        cameraIntent();
                    }

                } else if (items[item].equals("Choose from Library")) {

                    userChoosenTask = "Choose from Library";

                    galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (CommonUtil.checkAndRequestPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    AppConstant.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)) {

                getGalleryImage();
            }
        } else {
            getGalleryImage();
        }
    }

    private void getGalleryImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), AppConstant.REQUEST_MULTIPLE_PICK_IMAGE_CODE);
    }

    private void startDownloadImage() {
        image_url = "https://static.pexels.com/photos/33045/lion-wild-africa-african.jpg";
//        image_url = "http://i.imgur.com/OY2zNEb.jpg";
//        image_url = "https://i.redd.it/gwrw2x08bu8z.jpg";
//        image_url = "https://angusadventures.com/wp-content/uploads/2013/05/seaworthiness_image001.jpg";
//        image_url = "http://i.imgur.com/CQzlM.jpg";
//        image_url = "http://farm4.staticflickr.com/3810/9046947167_3a51fffa0b_s.jpg";

        new DownloadImageAsync().execute(image_url);
    }

    private void startDownloadMultipleImages() {

        multipleImageUrls = new String[]{
                "http://i.imgur.com/OY2zNEb.jpg", "https://i.redd.it/gwrw2x08bu8z.jpg",
                "https://angusadventures.com/wp-content/uploads/2013/05/seaworthiness_image001.jpg",
                "http://i.imgur.com/CQzlM.jpg", "http://farm4.staticflickr.com/3810/9046947167_3a51fffa0b_s.jpg",

        };

        new DownloadMultipleImagesAsync().execute(multipleImageUrls);
    }

    private void checkPermissions() {

        if (CommonUtil.checkAndRequestPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                AppConstant.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)) {

            if (CommonUtil.checkAndRequestPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    AppConstant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)) {

                if (CommonUtil.checkAndRequestPermission(MainActivity.this,
                        Manifest.permission.CAMERA,
                        AppConstant.MY_PERMISSIONS_REQUEST_CAMERA)) {

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
                    photoURI = FileProvider.getUriForFile(MainActivity.this,
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

                Uri imageUri = Uri.parse(mCurrentPhotoPath);

                if (imageUri != null) {

                    String realPath = String.valueOf(imageUri);

                    Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                    intent.putExtra("image", realPath);
                    startActivity(intent);
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

                    Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                    intent.putExtra("image", realPath);
                    startActivity(intent);
                }
                break;

            case AppConstant.REQUEST_MULTIPLE_PICK_IMAGE_CODE:

                if (data == null) {
                    System.out.println("");
                    return;
                }

                if (data.getData() != null) {

                    /*PDf*/
                    String pdfData = data.getData().toString();
                    String temp[] = pdfData.split("content://com.estrongs.files");

                    String fullPath = temp[temp.length - 1];

                    if (fullPath.equalsIgnoreCase(".pdf")) {
                        return;
                    } else {
                        /*Single Image*/
                        if (data != null) {
                            if (pdfData.contains("content://com.android.providers.media")) {
                                String path = RealPathUtil.getRealPathFromURI_API19(this, Uri.parse(pdfData));

                                Log.v("path", path + "");

                                Intent singleDataIntent = new Intent(getApplicationContext(), GalleryActivity.class);
                                singleDataIntent.putExtra("single_list", path);
                                startActivity(singleDataIntent);

                            } else {
                                String path = getRealPathFromURI(Uri.parse(pdfData));

                                Log.v("path", path + "");

                                Intent multipleIntent = new Intent(getApplicationContext(), GalleryActivity.class);
                                multipleIntent.putExtra("single_list", path);
                                startActivity(multipleIntent);

                            }
                        }
                    }
                } else {
                    /*Multiple Images*/
                    ArrayList<Images> imagesArrayList = new ArrayList<>();
                    String imagePath = null;

                    if (data != null) {
                        ClipData clipData = data.getClipData();

                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                ClipData.Item item = clipData.getItemAt(i);
                                Uri uri = item.getUri();
                                Log.v("uri", uri + "");

                                //In case you need image's absolute path
                                if (Build.VERSION.SDK_INT < 11)
                                    imagePath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, uri);

                                    // SDK >= 11 && SDK < 19
                                else if (Build.VERSION.SDK_INT < 19)
                                    imagePath = RealPathUtil.getRealPathFromURI_API11to18(this, uri);

                                    // SDK > 19 (Android 4.4)
                                else
                                    imagePath = RealPathUtil.getRealPathFromURI_API19(this, uri);


                                Log.v("path", imagePath + "");
                                imagesArrayList.add(new Images(imagePath));
                            }
                        }
                    }
                    Intent multipleIntent = new Intent(getApplicationContext(), GalleryActivity.class);
                    multipleIntent.putExtra("imagesArrayList", imagesArrayList);
                    startActivity(multipleIntent);
                }

                break;
        }

    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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

            case AppConstant.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkPermissions();
                } else {
                    CommonUtil.checkAndRequestPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE,
                            AppConstant.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                break;

            case AppConstant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkPermissions();
                } else {
                    CommonUtil.checkAndRequestPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            AppConstant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                break;

            case AppConstant.MY_PERMISSIONS_REQUEST_CAMERA:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    CommonUtil.checkAndRequestPermission(MainActivity.this, Manifest.permission.CAMERA,
                            AppConstant.MY_PERMISSIONS_REQUEST_CAMERA);
                }
                break;
        }
    }

    class DownloadImageAsync extends AsyncTask<String, Long, String> {

        ProgressDialog progressDialog;

        NotificationManager notifyManager;
        NotificationCompat.Builder builder;

        TextView total_left, total_mbs;
        ProgressBar status_progress;
        RemoteViews remoteViews;

        long startTime;
        long elapsedTime = 0L;

        @Override
        protected void onPreExecute() {
            /*super.onPreExecute();*/

            /*1). normal progress bar*/
            /*progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Downloading file...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.setCancelable(false);
            progressDialog.show();*/

            /*(2,3). with bytes and megabytes notificationBar*/
            remoteViews = new RemoteViews(getPackageName(), R.layout.customnotification);

            status_progress = (ProgressBar) findViewById(R.id.status_progress);
            total_mbs = (TextView) findViewById(R.id.total_mbs);
            total_left = (TextView) findViewById(R.id.total_left);

            /*(2,3,4). with bytes and megabytes notificationBar*/
            notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setContentTitle("File Downloading...")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContent(remoteViews);

            Toast.makeText(getApplicationContext(), "Downloading the file... The download progress is on notification bar."
                    , Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            int count;
            long lengthofFile = 0;
            OutputStream output;

            try {

                String path = params[0];

                URL url = new URL(path);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                lengthofFile = conexion.getContentLength();
                Log.d("Length", "Length of file: " + lengthofFile);

                InputStream input = new BufferedInputStream(url.openStream());

                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsolutePath() + "/image/");
                dir.mkdirs();

                String[] splitUrlName = image_url.split("/");
                String DotfileName = String.valueOf(splitUrlName[splitUrlName.length - 1]);

                if (DotfileName.contains(".jpg")) {
                    String[] splitDotfileName = DotfileName.split(".jpg");
                    fileName = splitDotfileName[splitDotfileName.length - 1];

                }

                output = new FileOutputStream("/sdcard/image/" + fileName + ".png");

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    long progress = (int) ((total * 100) / lengthofFile);

                    /*1). normal progress bar*/
                    /*publishProgress(progress);*/

                    /*(2,3). with bytes and megabytes*/
                    publishProgress(progress, total, lengthofFile);

                    /*4) normal notification bar*/
                    /*publishProgress(progress);*/
                    output.write(data, 0, count);

                }
                output.close();
                input.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Download Completed";
        }

        @Override
        protected void onProgressUpdate(final Long... values) {

        /*4). for normal notification bar*/
            /*protected void onProgressUpdate(final Integer... values) {*/

             /*1). simple progress bar*/
            /*progressDialog.setProgress(values[0]);*/

            /*2). progress bar for bytes and megabytes*/
            /*progressDialog.setProgress(Integer.parseInt(String.valueOf(values[0])));
            progressDialog.setProgressNumberFormat((bytes2String(values[1])) + "/" + (bytes2String(values[2])));*/

            /*3). custom notification bar*/
            final CharSequence total_mbs = bytes2String(values[2]);
            final CharSequence total_left = bytes2String(values[1]);

//            if (elapsedTime > 500) {
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        remoteViews.setProgressBar(R.id.status_progress, 100, Integer.parseInt(String.valueOf(values[0])), false);
//                        remoteViews.setTextViewText(R.id.total_mbs, total_mbs);
//                        remoteViews.setTextViewText(R.id.total_left, total_left);
//
//                        notifyManager.notify(0, builder.build());
//
//                        startTime = System.currentTimeMillis();
//                        elapsedTime = 0;
//                    }
//                });
//            }
//            else {
//                elapsedTime = new Date().getTime() - startTime;
//            }


            new Thread(new Runnable() {
                public void run() {

                    remoteViews.setProgressBar(R.id.status_progress, 100, Integer.parseInt(String.valueOf(values[0])), false);
                    remoteViews.setTextViewText(R.id.total_mbs, total_mbs);
                    remoteViews.setTextViewText(R.id.total_left, total_left);
                    notifyManager.notify(0, builder.build());
                }
            }).start();


            /*4). normal notification bar*/
           /* builder.setProgress(100, values[0], false);
            notifyManager.notify(0, builder.build());*/

        }

        @Override
        protected void onPostExecute(String result) {

            /*(1,2). progress bar*/
            /*progressDialog.hide();*/

            /*3). custom notification bar*/
            /*remoteViews.setProgressBar(0, 0, 0, false);
            notifyManager.notify(0, builder.build());*/

            /*4). normal notification bar */
            /*builder.setProgress(0, 0, false);
            notifyManager.notify(0, builder.build());*/

        }

    }

    public static String bytes2String(long sizeInBytes) {

        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);

        try {
            if (sizeInBytes < SPACE_KB) {
                return nf.format(sizeInBytes) + " Bytes";
            } else if (sizeInBytes < SPACE_MB) {
                return nf.format(sizeInBytes / SPACE_KB) + " KB";
            } else if (sizeInBytes < SPACE_GB) {
                return nf.format(sizeInBytes / SPACE_MB) + " MB";
            } else if (sizeInBytes < SPACE_TB) {
                return nf.format(sizeInBytes / SPACE_GB) + " GB";
            } else {
                return nf.format(sizeInBytes / SPACE_TB) + " TB";
            }
        } catch (Exception e) {
            return sizeInBytes + " Byte";
        }

    }

    class DownloadMultipleImagesAsync extends AsyncTask<String, Integer, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
//            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Downloading file...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            int count;
            int lengthofFile = 0;
            OutputStream output;
            String path;

            try {

                for (int i = 0; i < params.length; i++) {

                    path = params[i];

                    URL url = new URL(path);
                    URLConnection conexion = url.openConnection();
                    conexion.connect();

                    lengthofFile = conexion.getContentLength();
                    Log.d("Length", "Length of file: " + lengthofFile);

                    InputStream input = new BufferedInputStream(url.openStream());

                    File filepath = Environment.getExternalStorageDirectory();
                    File dir = new File(filepath.getAbsolutePath() + "/image/");
                    dir.mkdirs();

                    String[] splitUrlName = path.split("/");
                    String DotfileName = String.valueOf(splitUrlName[splitUrlName.length - 1]);

                    if (DotfileName.contains(".jpg")) {
                        String[] splitDotfileName = DotfileName.split(".jpg");
                        fileName = splitDotfileName[splitDotfileName.length - 1];

                    }

                    output = new FileOutputStream("/sdcard/image/" + fileName + ".png");

                    byte data[] = new byte[1024];
                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        int progress = (int) ((total * 100) / lengthofFile);
                        publishProgress(progress);
                        output.write(data, 0, count);
                    }

                    output.close();
                    input.close();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Download Completed";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            progressDialog.hide();

//            File file = new File("/sdcard/image/" + fileName + ".png");
//
//
//            if (file.exists()) {
//
//                iv_image.setImageURI(Uri.fromFile(file));
//            } else {
//                new DownloadImageAsync().execute(image_url);
//            }

        }

    }

}