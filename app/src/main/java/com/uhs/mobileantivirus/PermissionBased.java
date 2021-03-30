package com.uhs.mobileantivirus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PermissionBased extends AppCompatActivity {

    private PackageManager packageManager;
    private ArrayList<String> packageNames, excludedApps, permissionFlag;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_permission_based);

        listView = (ListView) findViewById(R.id.listView1);
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, R.layout.list_app_control, new ArrayList<String>());
        packageNames = new ArrayList<>();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));

        BufferedReader reader = null;
        excludedApps = new ArrayList<String>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("PermissionExclusionApps.txt"), "UTF-8"));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                excludedApps.add(mLine);
            }
        } catch (IOException e) {
            Log.i("Read URL Error: ", String.valueOf(e));
        }

        BufferedReader reader2 = null;
        permissionFlag = new ArrayList<String>();
        try {
            reader2 = new BufferedReader(
                    new InputStreamReader(getAssets().open("PermissionFlag.txt"), "UTF-8"));
            String mLine2;
            while ((mLine2 = reader2.readLine()) != null) {
                permissionFlag.add(mLine2);
            }
        } catch (IOException e) {
            Log.i("Read URL Error: ", String.valueOf(e));
        }

        Log.i("Excluded:", String.valueOf(excludedApps));

        label1: for (ResolveInfo resolver : activities) {
            String appName = (String) resolver.loadLabel(packageManager);
                label2: for (int o = 0; o < excludedApps.size(); o++) {
                String temp2 = excludedApps.get(o);
                    if(appName.equals(temp2)){
                        continue label1;
                    }
                }
                //Log.i("Curr:", appName + " VS " + temp2);
                ArrayList<String> temp;
                temp = getPermissionsByPackageName(resolver.activityInfo.packageName);
                label3: for (int i = 0; i < temp.size(); i++) {
                    token = "0";
                    label4: for (int z = 0; z < permissionFlag.size(); z++){
                        if(temp.get(i).equals(permissionFlag.get(z))){
                            token = "1";
                        }
                    }

                    if (token.equals("1")){
                        adapter.add(appName);
                        packageNames.add(resolver.activityInfo.packageName);
                        continue label1;
                    }
                }
            }
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        ArrayList<String> temp;
                        temp = getPermissionsByPackageName(packageNames.get(position));
                        final String[] array = temp.toArray(new String[0]);
                        LayoutInflater inflater= LayoutInflater.from(PermissionBased.this);
                        View view2 = inflater.inflate(R.layout.permission_list, null);
                        ListView listview = (ListView)view2.findViewById(R.id.listviewitem);
                        ArrayAdapter<String> permission_adapter = new ArrayAdapter<String>(PermissionBased.this,R.layout.permission_listview, array){
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = (TextView) view.findViewById(android.R.id.text1);
                                for (int z = 0; z < permissionFlag.size(); z++){
                                    if (array[position].equals(permissionFlag.get(z))) {
                                        text.setTextColor(Color.RED);
                                    }
                                }
                                return view;
                            }
                        };
                        listview.setAdapter(permission_adapter);
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PermissionBased.this);
                        alertDialog.setTitle("Permission List");
                        alertDialog.setView(view2);
                        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog alert = alertDialog.create();
                        alert.show();
                    } catch (Exception e) {
                    }
                }
            });

        }

    protected ArrayList<String> getPermissionsByPackageName(String packageName){
        // Initialize a new string builder instance
        ArrayList<String> builder = new ArrayList<>();

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);

            for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
                if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    String permission =packageInfo.requestedPermissions[i];
                    builder.add(permission);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return builder.toString();
        return builder;
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