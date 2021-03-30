package com.uhs.mobileantivirus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParentalControl extends AppCompatActivity {

    DatabaseLogin mDatabaseHelper;
    DatabaseParental pDatabaseHelper;
    Button startlock;
    String LOGIN_STATUS, data1, sParental_Password,excluded_list, datap, tokenize;
    EditText Parental_Password;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbPP, dbEA;
    private PackageManager packageManager;
    private ArrayList<String> appNames, excludedApps;
    private ArrayAdapter<String> adapter, adapter2;
    private ListView listView, listView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_parental_control);
        startlock = (Button) findViewById(R.id.startlock);
        Parental_Password = (EditText) findViewById(R.id.parental_password);

        mDatabaseHelper = new DatabaseLogin(this);
        Cursor cursor = mDatabaseHelper.getData();
        if (cursor.getCount() == 0) {
            LOGIN_STATUS = "N";
        } else {
            while (cursor.moveToNext()) {
                data1 = cursor.getString(1);
                if (data1.length() >= 3) {
                    LOGIN_STATUS = "Y";
                } else {
                    LOGIN_STATUS = "N";
                }
            }
        }
        pDatabaseHelper = new DatabaseParental(this);
        Cursor cursor2 = pDatabaseHelper.getData();
        if (!(cursor2.getCount() == 0)) {
            while (cursor2.moveToNext()) {
                datap = cursor2.getString(1);
                excluded_list = datap;
            }
        }

        if (!LOGIN_STATUS.equals("Y")) {
            Toast.makeText(ParentalControl.this, "LOGIN TO CONTINUE", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ParentalControl.this, MainActivity.class));
        } else {
            dbPP = database.getReference(data1).child("Parental_Password");
            dbEA = database.getReference(data1).child("Parental_Exclude");
            dbPP.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    sParental_Password = dataSnapshot.getValue(String.class);
                    if (sParental_Password != null) {
                        Parental_Password.setText(sParental_Password);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.i("FIREBASE: ", "Failed to read value.", error.toException());
                }
            });
        }


        listView = (ListView) findViewById(R.id.listView1);
        listView2 = (ListView) findViewById(R.id.listView2);
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(this, R.layout.list_app_control, new ArrayList<String>());
        adapter2 = new ArrayAdapter<String>(this, R.layout.list_app_control, new ArrayList<String>());
        appNames = new ArrayList<>();
        excludedApps = new ArrayList<>();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));

        String[] eApps = excluded_list.split(";");

        for (ResolveInfo resolver : activities) {
            // Exclude the settings app and this launcher from the list of apps shown
            String appName = (String) resolver.loadLabel(packageManager);
            int i=0;
            tokenize = "";
            String counter = "";
            while (i < eApps.length){
                if (String.valueOf(eApps[i]).equalsIgnoreCase(appName)) {
                    tokenize += "0";
                } else {
                    tokenize += "1";
                }
                counter += "1";
                i++;
            }

            if (tokenize.equals(counter)) {
                adapter.add(appName);
                appNames.add(appName);
            } else {
                adapter2.add(appName);
                excludedApps.add(appName);
            }
        }
        listView.setAdapter(adapter);
        listView2.setAdapter(adapter2);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String getApps = appNames.get(position);
                    if (excludedApps.contains(getApps)) {
                        Toast.makeText(ParentalControl.this, getApps + "is already excluded", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            adapter.remove(getApps);
                            appNames.remove(position);
                            adapter2.add(getApps);
                            excludedApps.add(getApps);
                        } catch (Exception e) {
                        }
                    }
                    listView.setAdapter(adapter);
                    listView2.setAdapter(adapter2);
                }
            });

            listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String getApps = excludedApps.get(position);
                    if (appNames.contains(getApps)) {
                        Toast.makeText(ParentalControl.this, getApps + "is already restore", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            adapter.add(getApps);
                            appNames.add(getApps);
                            adapter2.remove(getApps);
                            excludedApps.remove(position);
                        } catch (Exception e) {
                        }
                    }
                    listView.setAdapter(adapter);
                    listView2.setAdapter(adapter2);
                }
            });


            startlock.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText nParental_Password = findViewById(R.id.parental_password);
                    if (nParental_Password.length() < 1) {
                        Toast.makeText(ParentalControl.this, "Parental Password cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        dbPP = database.getReference(data1).child("Parental_Password");
                        dbPP.setValue(nParental_Password.getText().toString());
                        excluded_list = "";
                        for (int counter = 0; counter < excludedApps.size(); counter++) {
                            if (excluded_list.equals("")) {
                                excluded_list = excludedApps.get(counter);
                            } else {
                                excluded_list += ";" + excludedApps.get(counter);
                            }
                        }
                        dbEA = database.getReference(data1).child("Parental_Exclude");
                        dbEA.setValue(excluded_list);
                        pDatabaseHelper.addData(excluded_list);

                    Context context = ParentalControl.this;
                    PackageManager packageManager = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, FakeHome.class);
                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    context.startActivity(intent);

                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                    }
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
