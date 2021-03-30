package com.uhs.mobileantivirus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ScanFiles extends AppCompatActivity {

    private PackageManager packageManager;
    ArrayAdapter<String> adapter;
    private ListView listView;
    ArrayList<String> appNames;
    List<PackageInfo> packList;
    String serverHash, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_files);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        packList = getPackageManager().getInstalledPackages(0);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ArrayList<String> exclusion = new ArrayList<String>();
        exclusion.add("Youtube");
        exclusion.add("Google");
        exclusion.add("Drive");
        exclusion.add("com.android.providers.media");
        exclusion.add("com.android.systemui.plugin.globalactions.wallet");
        exclusion.add("com.android.carrierconfig");
        exclusion.add("com.android.ons");
        exclusion.add("com.android.backupconfirm");
        exclusion.add("com.android.emulator.radio.config");
        exclusion.add("com.android.sharedstoragebackup");
        exclusion.add("com.android.cellbroadcastreceiver");
        exclusion.add("com.android.service.ims.RcsServiceApp");
        exclusion.add("Google Play services");
        exclusion.add("com.google.android.overlay.permissioncontroller");
        exclusion.add("com.google.android.overlay.emulatorconfig");
        exclusion.add("com.android.localtransport");
        exclusion.add("Photos");
        exclusion.add("Calendar");
        exclusion.add("com.google.android.sdksetup");
        exclusion.add("com.android.server.NetworkPermissionConfig");
        exclusion.add("com.android.wallpaperbackup");
        exclusion.add("Gboard");

        final ProgressBar progressSpin = (ProgressBar) findViewById(R.id.progressBar);
        progressSpin.setVisibility(View.GONE);
        packageManager = getPackageManager();
        listView = findViewById(R.id.apps_list);
        final List<String> sourceDir = new ArrayList<>();
        appNames = new ArrayList<>();
        List<ApplicationInfo> packages =  packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        for (ApplicationInfo packageInfo : packages) {
            String appName = (String) packageInfo.loadLabel(packageManager);
            token="0";
            for (int y = 0; y<exclusion.size(); y++){
                if(appName.equalsIgnoreCase(exclusion.get(y))){
                    token = "1";
                }
            }
            if (!token.equals("1")){
                adapter.add(appName);
                appNames.add(appName);
                sourceDir.add(packageInfo.sourceDir);
            }
        }
        listView.setAdapter(adapter);

        //CHECK DATABASE
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                progressSpin.setVisibility(View.VISIBLE);
                String appName = appNames.get(position);
                if(appName.contains(".")){
                    appName.replace(".","-");
                }
                DatabaseReference dbDH = database.getReference("DexHashes").child(appName);
                String s = sourceDir.get(position);
                byte[] b = getFile(s);
                byte[] dex = unzipDex(b, s);
                String byteArray = Arrays.toString(dex);
                final String computedHash = sha1Hash(byteArray);
                Log.d(appName, sha1Hash(byteArray));
                dbDH.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            serverHash = dataSnapshot.getValue(String.class);
                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressSpin.setVisibility(View.GONE);
                                    AlertDialog.Builder builder;
                                    try {
                                    if (serverHash.equals(computedHash)) {
                                        builder = new AlertDialog.Builder(ScanFiles.this, R.style.PassDialogStyle);
                                        builder.setTitle("Hash Matches");
                                        builder.setMessage("This package is safe to keep.");
                                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                    } else {
                                        builder = new AlertDialog.Builder(ScanFiles.this, R.style.AlertDialogStyle);
                                        builder.setTitle("Hash Unmatched");
                                        builder.setMessage("Malicious Package!");
                                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                    }
                                    builder.show();
                                    } catch(Exception exception) {
                                        Log.e(serverHash, String.valueOf(exception));
                                        builder = new AlertDialog.Builder(ScanFiles.this, R.style.AlertDialogStyle);
                                        builder.setTitle("No Data");
                                        builder.setMessage("No data found. Take precautions.");
                                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        builder.show();
                                    }
                                }
                            }, 3000);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(ScanFiles.this, R.style.AlertDialogStyle);
                        builder.setTitle("No Data");
                        builder.setMessage("No data found. Take precautions.");
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });
            }
        });

        //ADD DATABASE
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String appName = appNames.get(position);
                if(appName.contains(".")){
                    appName.replace(".","-");
                }
                DatabaseReference dbDH = database.getReference("DexHashes").child(appName);
                String s = sourceDir.get(position);
                byte[] b = getFile(s);
                byte[] dex = unzipDex(b, s);
                String byteArray = Arrays.toString(dex);
                final String computedHash = sha1Hash(byteArray);
                Log.d(appName, sha1Hash(byteArray));
                dbDH.setValue(computedHash);
            }
        });*/
    }

    String sha1Hash(String toHash){
        String hash = null;
        try{
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();
            hash = bytesToHex(bytes);
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return hash;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes){
        char[] hexChars = new char[bytes.length*2];
        for(int j=0;j<bytes.length;j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2+1] = hexArray[v&0x0F];
        }
        return new String(hexChars);
    }


    byte[] getFile(String filename){
        try {
            RandomAccessFile f = new RandomAccessFile(filename, "r");
            byte[] b = new byte[(int)f.length()];
            f.readFully(b);
            return b;
        } catch(IOException exception){
            exception.printStackTrace();
        }
        return null;
    }

    public byte[] unzipDex(byte[] bytes, String filename){
        try{
            ZipFile zipFile = new ZipFile(filename);
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
            ZipEntry ze = zis.getNextEntry();
            while(ze!=null){
                String entryName = ze.getName();
                if(!entryName.equals("classes.dex")){
                    ze = zis.getNextEntry();
                    continue;
                }
                InputStream is = zipFile.getInputStream(ze);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                //16384
                while ((nRead = is.read(data, 0, data.length)) != -1){
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return buffer.toByteArray();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }
}