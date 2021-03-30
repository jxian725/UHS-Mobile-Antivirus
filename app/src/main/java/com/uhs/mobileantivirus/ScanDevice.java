package com.uhs.mobileantivirus;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ScanDevice extends AppCompatActivity {
    private static final String TAG = "ScanDevice";
    private static final int REQUEST_CODE = 1;
    Intent mServiceIntent;
    private service mYourService;
    DatabaseReference dbEA;
    DatabaseParental pDatabaseHelper;
    DatabaseLogin hDatabaseHelper;
    DatabaseScan mDatabaseHelper;
    private Button scanButton;
    private TextView lastScanValueText;
    String LOGIN_STATUS,data1,data2,excluded_list;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    Spinner dropdown;
    String[] method_type = new String[]{"Signature Based", "Dex Code Based", "Permission Based"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        ToggleButton toggle = (ToggleButton) findViewById(R.id.protectionButton);
        mDatabaseHelper = new DatabaseScan(this);
        mYourService = new service();
        mServiceIntent = new Intent(this, mYourService.getClass());
        if (!isMyServiceRunning(mYourService.getClass())) {
           toggle.setChecked(false);
        } else {
            toggle.setChecked(true);
        }

        hDatabaseHelper = new DatabaseLogin(this);
        Cursor cursor2 = hDatabaseHelper.getData();
        if(cursor2.getCount ()==0){
            LOGIN_STATUS = "N";
        } else {
            while(cursor2.moveToNext()) {
                data1 = cursor2.getString(1);
                if (data1.length() >= 3){
                    LOGIN_STATUS = "Y";
                } else {
                    LOGIN_STATUS = "N";
                }
            }
        }
        if (LOGIN_STATUS.equals("Y")){
            dbEA = database.getReference(data1).child("Parental_Exclude");
            dbEA.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    excluded_list = dataSnapshot.getValue(String.class);
                    if (excluded_list!=null){
                        pDatabaseHelper = new DatabaseParental(ScanDevice.this);
                        pDatabaseHelper.addData(excluded_list);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.i("FIREBASE: ", "Failed to read value.", error.toException());
                }
            });
        }

        //getHistoryTextView
        lastScanValueText = (TextView)findViewById(R.id.lastScanValueText);

        final Spinner dropdown = findViewById(R.id.dropdown);
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(this, R.layout.list_dropdown_text, method_type);
        dropdown.setAdapter(dropdownAdapter);

        //get current dateTime
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDateTime = dateFormat.format(currentTime);
        String currentYear = currentDateTime.substring(0,4);
        String currentMonth = currentDateTime.substring(5,7);
        String currentDay = currentDateTime.substring(8,10);
        String currentHour = currentDateTime.substring(11,13);
        String currentMinute = currentDateTime.substring(14,16);
        String currentSecond = currentDateTime.substring(17,19);

        //RetrieveLastScanDateTime
        Cursor cursor = mDatabaseHelper.getData();
        if(cursor.getCount ()==0){
            Toast.makeText(getApplicationContext(), "NO DATA", Toast.LENGTH_SHORT ).show();
        } else {
            while(cursor.moveToNext()) {
                //differentiateDateTime
                String historyDateTime = cursor.getString(1);
                String historyYear = historyDateTime.substring(0,4);
                String historyMonth = historyDateTime.substring(5,7);
                String historyDay = historyDateTime.substring(8,10);
                String historyHour = historyDateTime.substring(11,13);
                String historyMinute = historyDateTime.substring(14,16);
                String historySecond = historyDateTime.substring(17,19);
                //calculateDateTime
                if (Integer.parseInt(currentYear) == Integer.parseInt(historyYear) ){
                    if (Integer.parseInt(currentMonth) == Integer.parseInt(historyMonth)){
                        if(Integer.parseInt(currentDay) == Integer.parseInt(historyDay)){
                            if (Integer.parseInt(currentHour) == Integer.parseInt(historyHour)){
                                if(Integer.parseInt(currentMinute) == Integer.parseInt(historyMinute)){
                                    if(Integer.parseInt(currentSecond) == Integer.parseInt(historySecond)){
                                        lastScanValueText.setText("Just now");
                                    }
                                    else {
                                        int cS = Integer.parseInt(currentSecond);
                                        int hS = Integer.parseInt(historySecond);
                                        int getSecond = cS - hS;
                                        String StringGetSecond = Integer.toString(getSecond);
                                        if (getSecond > 1 ) {
                                            lastScanValueText.setText(StringGetSecond + " seconds ago");
                                        }
                                        else {
                                            lastScanValueText.setText(StringGetSecond + " second ago");
                                        }
                                    }
                                }
                                else {
                                    int cMin = Integer.parseInt(currentMinute);
                                    int hMin = Integer.parseInt(historyMinute);
                                    int getMinute = cMin - hMin;
                                    String StringGetMinute = Integer.toString(getMinute);
                                    if (getMinute > 1 ) {
                                        lastScanValueText.setText(StringGetMinute + " minutes ago");
                                    }
                                    else {
                                        lastScanValueText.setText(StringGetMinute + " minute ago");
                                    }
                                }
                            }
                            else {
                                int cH = Integer.parseInt(currentHour);
                                int hH = Integer.parseInt(historyHour);
                                int getHour = cH - hH;
                                String StringGetHour = Integer.toString(getHour);
                                if (getHour > 1 ) {
                                    lastScanValueText.setText(StringGetHour + " hours ago");
                                }
                                else {
                                    lastScanValueText.setText(StringGetHour + " hour ago");
                                }
                            }
                        }
                        else {
                            int cD = Integer.parseInt(currentDay);
                            int hD = Integer.parseInt(historyDay);
                            int getDay = cD - hD;
                            String StringGetDay = Integer.toString(getDay);
                            if (getDay > 1) {
                                lastScanValueText.setText(StringGetDay + " days ago");
                            }
                            else {
                                lastScanValueText.setText(StringGetDay + " day ago");
                            }
                        }
                    }
                    else {
                        int cM = Integer.parseInt(currentMonth);
                        int hM = Integer.parseInt(historyMonth);
                        int getMonth = cM - hM;
                        String StringGetMonth = Integer.toString(getMonth);
                        if (getMonth > 1) {
                            lastScanValueText.setText(StringGetMonth + " months ago");
                        }
                        else {
                            lastScanValueText.setText(StringGetMonth + " month ago");
                        }
                    }
                }
                else {
                    int cY = Integer.parseInt(currentYear);
                    int hY = Integer.parseInt(historyYear);
                    int getYear = cY - hY;
                    String StringGetYear = Integer.toString(getYear);
                    if (getYear > 1) {
                        lastScanValueText.setText(StringGetYear + "years ago");
                    }
                    else {
                        lastScanValueText.setText(StringGetYear + "year ago");
                    }
                }
            }
        }
        final String strDateTimeAdd = currentDateTime;

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)  {
                AddData(strDateTimeAdd);
                //get dropdown value
                String selected = dropdown.getSelectedItem().toString();
                if (selected.equals("Signature Based")){
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    startActivity(new Intent(ScanDevice.this, SignatureBased.class));
                } else if (selected.equals("Dex Code Based")){
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    startActivity(new Intent(ScanDevice.this, ScanFiles.class));
                } else if (selected.equals("Permission Based")){
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    startActivity(new Intent(ScanDevice.this, PermissionBased.class));
                }
            }
        });

        final Intent intent = new Intent(ScanDevice.this, service.class);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getBaseContext(), "Protection ON", Toast.LENGTH_SHORT).show();
                    startService(intent);
                } else {
                    //stopService(mServiceIntent);
                    Toast.makeText(getBaseContext(), "Protection OFF", Toast.LENGTH_SHORT).show();
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.cancel(100);
                    stopService(intent);
                }
            }
        });

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.v("PERMISSION CHECK","Permission is granted");
            //File write logic here
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

    }

    public void AddData(String newEntry){
        boolean insertData = mDatabaseHelper.addData(newEntry);
        if (!insertData) {
            Toast.makeText(getBaseContext(), "Unexpected Error", Toast.LENGTH_SHORT ).show();
        }
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    @Override
    protected void onDestroy() {
        ToggleButton toggle = (ToggleButton) findViewById(R.id.protectionButton);
        if (toggle.isChecked() == true) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, Restarter.class);
            this.sendBroadcast(broadcastIntent);
            super.onDestroy();
        } else {
            super.onDestroy();
        }
    }
}