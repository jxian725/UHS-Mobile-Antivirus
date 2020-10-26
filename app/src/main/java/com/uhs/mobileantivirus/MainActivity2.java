package com.uhs.mobileantivirus;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "MainActivity2";

    DatabaseHelper mDatabaseHelper;
    private Button scanButton;
    private TextView lastScanValueText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mDatabaseHelper = new DatabaseHelper(this);

        //getHistoryTextView
        lastScanValueText = (TextView)findViewById(R.id.lastScanValueText);

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
                //goto Scan

                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });

        ToggleButton toggle = (ToggleButton) findViewById(R.id.protectionButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(new Intent(getApplicationContext(),service.class));

                } else {
                    stopService(new Intent(getApplicationContext(),service.class));
                }
            }
        });
    }

    public void AddData(String newEntry){
        boolean insertData = mDatabaseHelper.addData(newEntry);
        if (insertData) {
            Toast.makeText(getBaseContext(), "Success Insert Data", Toast.LENGTH_SHORT ).show();
        } else {
            Toast.makeText(getBaseContext(), "Fail Insert Data", Toast.LENGTH_SHORT ).show();
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
           case R.id.antiThief:
                Toast.makeText(this,"Anti Thief", Toast.LENGTH_SHORT).show();
                return true;
           case R.id.cloudBackup:
                Toast.makeText(this,"Cloud Backup", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.antiPhishing:
                Toast.makeText(this,"Anti Phishing", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.webProtection:
                Toast.makeText(this,"Web Protection", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.parentalControl:
                Toast.makeText(this,"Parental Control", Toast.LENGTH_SHORT).show();
                return true;
           default:
               return super.onOptionsItemSelected(item);
        }
    }

}