package com.uhs.mobileantivirus;


import java.io.File;
import java.io.InputStream;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CloudBackup extends AppCompatActivity {

    private TextView title;
    private TextView editFile;
    private TextView selectedFile;
    private TextView percentage;
    private TextView used;
    private ProgressBar storageBar;
    private Button selectFileBtn;
    private Button uploadBtn;
    DatabaseCloud mDatabaseHelper;


    private InputStream getResources(String s) {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_backup);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mDatabaseHelper = new DatabaseCloud(this);
        title = (TextView) findViewById(R.id.title);
        editFile = (TextView)findViewById(R.id.editFile);
        selectedFile = (TextView)findViewById(R.id.selectedFile);
        selectFileBtn = (Button) findViewById(R.id.selectFileBtn);
        percentage = (TextView)findViewById(R.id.percentage);
        storageBar = (ProgressBar)findViewById(R.id.storageBar);
        storageBar.setMax(100);
        used = (TextView)findViewById(R.id.used);


        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();

        Cursor cursor = mDatabaseHelper.getStorage();
        if(cursor.getCount ()==0){
            used.setText("0.00 Used");
            percentage.setText("0%");
            storageBar.setProgress(0);
        } else {
            while(cursor.moveToNext()) {
                String datas = cursor.getString(1);
                used.setText(datas + "KB Used");
                double i = Double.parseDouble(datas);
                double ii = (i/5242880) * 100;
                int iii = (int)ii;
                percentage.setText((String.format("%.2f", ii))+"%");
                storageBar.setProgress(iii);
            }
        }




        uploadBtn = (Button) findViewById(R.id.uploadBtn);
        uploadBtn.setVisibility(View.GONE);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)  {
                if(selectedFile.getText().length() <=1 ){
                    Toast.makeText(CloudBackup.this,"No File Selected", Toast.LENGTH_SHORT).show();
                } else {
                    String path = String.valueOf(editFile.getText());
                    String index = path.substring(path.lastIndexOf("/") + 1);
                    Uri file = Uri.fromFile(new File(path));
                    StorageReference uploadFiles = storageRef.child(file.getLastPathSegment());
                    UploadTask uploadTask = uploadFiles.putFile(file);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(CloudBackup.this,"Upload Failed", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            Toast.makeText(CloudBackup.this,"Upload Success", Toast.LENGTH_SHORT).show();
                            //set Size
                            String path = String.valueOf(editFile.getText());
                            File file = new File(path);
                            int file_size = Integer.parseInt(String.valueOf(file.length()/1024));
                            Cursor cursor2 = mDatabaseHelper.getStorage();
                            if(cursor2.getCount ()==0){
                                used.setText(String.valueOf(file_size) + "KB Used");
                                double ii = (file_size/5242880) * 100;
                                percentage.setText(ii + "%");
                                int iii = (int)ii;
                                storageBar.setProgress(iii);
                                mDatabaseHelper.updateStorage(String.valueOf(file_size));
                            } else {
                                double i = 0;
                                while(cursor2.moveToNext()) {
                                    String datas = cursor2.getString(1);
                                    i = Double.parseDouble(datas) + file_size;
                                    used.setText(i + " KB Used");
                                    double ii = (i/5242880) * 100;
                                    int iii = (int)ii;
                                    percentage.setText((String.format("%.2f", ii))+"%");
                                    storageBar.setProgress(iii);
                                }
                                mDatabaseHelper.updateStorage(String.valueOf(i));
                            }
                        }
                    });
                }
            }
        });



        selectFileBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)  {
                if (selectedFile.getText().length()>=1){
                    selectFileBtn.setText("Select File to Backup");
                    selectedFile.setText("");
                    editFile.setText("");
                    uploadBtn.setVisibility(View.GONE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, 7);
                }
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri PathHolder = data.getData();
        String FilePath = getPath(this,PathHolder);
        //String FilePath = String.valueOf(PathHolder);
        selectedFile.setText("Selected File:");
        editFile.setText(FilePath);
        uploadBtn.setVisibility(View.VISIBLE);
        selectFileBtn.setText("Cancel");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.scanDevice:
                Intent intent1 = new Intent(this, ScanDevice.class);
                startActivity(intent1);
                return true;
            case R.id.antiThief:
                Intent intent2 = new Intent(this, AntiTheft.class);
                startActivity(intent2);
                return true;
            case R.id.cloudBackup:
                Intent intent3 = new Intent(this, CloudBackup.class);
                startActivity(intent3);
                return true;
            case R.id.antiPhishing:
                Intent intent4 = new Intent(this, AntiPhishing.class);
                startActivity(intent4);
                return true;
            case R.id.parentalControl:
                Intent intent5 = new Intent(this, ParentalControl.class);
                startActivity(intent5);
                return true;
            case R.id.userProfile:
                Intent intent6 = new Intent(this, UserProfile.class);
                startActivity(intent6);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}


