package com.uhs.mobileantivirus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignatureBased extends AppCompatActivity {

    private PackageManager packageManager;
    private ArrayList<String> packageNames, SignatureDB;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private String token,signatureBase256,listSignature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_signature_based);

        listView = (ListView) findViewById(R.id.listView1);
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, R.layout.list_app_control, new ArrayList<String>());
        packageNames = new ArrayList<>();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));

        BufferedReader reader = null;
        SignatureDB = new ArrayList<String>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("SignatureDatabase.txt"), "UTF-8"));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                SignatureDB.add(mLine);
            }
        } catch (IOException e) {
            Log.i("Read URL Error: ", String.valueOf(e));
        }

        label1: for (ResolveInfo resolver : activities) {
            String appName = (String) resolver.loadLabel(packageManager);
            String tempPN = resolver.activityInfo.packageName;
            try {
                final PackageInfo packageInfo = packageManager.getPackageInfo(tempPN, PackageManager.GET_SIGNING_CERTIFICATES);
                final Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                final MessageDigest md = MessageDigest.getInstance("SHA-256");
                for (Signature signature : signatures) {
                    md.update(signature.toByteArray());
                    signatureBase256 = new String(Base64.encode(md.digest(), Base64.DEFAULT));
                }
                token = "0";
                label2: for (int q = 0; q < SignatureDB.size(); q++){
                    String tempSDB = SignatureDB.get(q);
                    String temp256 = signatureBase256.substring(0,44);
                    if(tempSDB.equals(temp256)){
                        token = "1";
                        continue label2;
                    }
                }
                if (token.equals("0")){
                    adapter.add(appName);
                    packageNames.add(resolver.activityInfo.packageName);
                    continue label1;
                }
            } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        }
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String signature256 = packageNames.get(position);
                final PackageInfo packageInfo;
                try {
                    packageInfo = packageManager.getPackageInfo(signature256, PackageManager.GET_SIGNING_CERTIFICATES);
                    final Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                    final MessageDigest md = MessageDigest.getInstance("SHA-256");
                    for (Signature signature : signatures) {
                        md.update(signature.toByteArray());
                        listSignature = new String(Base64.encode(md.digest(), Base64.DEFAULT));
                    }
                    Log.e("SHA-256",listSignature);
                } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(SignatureBased.this, R.style.AlertDialogStyle);
                builder.setTitle("Unrecognized Signature");
                builder.setMessage("SHA-256:\n"+listSignature);
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
}