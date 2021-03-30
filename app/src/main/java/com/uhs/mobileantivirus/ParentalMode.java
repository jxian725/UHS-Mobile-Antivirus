package com.uhs.mobileantivirus;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParentalMode extends Activity {

    ComponentName cn1 = new ComponentName("com.uhs.mobileantivirus", "com.uhs.mobileantivirus.LauncherAlias1");
    ComponentName cn2 = new ComponentName("com.uhs.mobileantivirus", "com.uhs.mobileantivirus.LauncherAlias2");
    private PackageManager packageManager;
    private ArrayList<String> packageNames;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private int count = 0;
    private long startMillis=0;
    private String m_Text = "";
    DatabaseLogin mDatabaseHelper;
    DatabaseParental pDatabaseHelper;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbPP;
    String data1, sParental_Password, datap, excluded_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseHelper = new DatabaseLogin(this);
        Cursor cursor = mDatabaseHelper.getData();
        if(cursor.getCount ()!=0){
            while(cursor.moveToNext()) {
                data1 = cursor.getString(1);
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
        dbPP = database.getReference(data1).child("Parental_Password");
        dbPP.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sParental_Password = dataSnapshot.getValue(String.class);
                if (sParental_Password == null) {
                    sParental_Password = "admin";
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("FIREBASE: ", "Failed to read value.", error.toException());
            }
        });


        // Setup UI elements
        listView = new ListView(this);
        listView.setVerticalScrollBarEnabled(false);
        listView.setId(android.R.id.list);
        listView.setDivider(null);
        setContentView(listView);
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();
        p.setMargins(100, 0, 0, 0);

        // Get a list of all the apps installed
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<String>(
                this, R.layout.list_app_text, new ArrayList<String>());
        packageNames = new ArrayList<>();

        // Tap on an item in the list to launch the app
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    startActivity(packageManager.getLaunchIntentForPackage(packageNames.get(position)));
                } catch (Exception e) {
                    fetchAppList();
                }
            }
        });

        // Long press on an item in the list to open the app settings
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // Attempt to launch the app with the package name
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + packageNames.get(position)));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    fetchAppList();
                }
                return false;
            }
        });
        fetchAppList();
    }


    private void fetchAppList() {
        // Start from a clean adapter when refreshing the list
        adapter.clear();
        packageNames.clear();

        // Query the package manager for all apps
        List<ResolveInfo> activities = packageManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);

        // Sort the applications by alphabetical order and add them to the list
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));
        String[] eApps = excluded_list.split(";");
        for (ResolveInfo resolver : activities) {

            // Exclude the settings app and this launcher from the list of apps shown
            String appName = (String) resolver.loadLabel(packageManager);
            int i=0;
            String tokenize = "";
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
                packageNames.add(resolver.activityInfo.packageName);
            }
        }
        listView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        // Prevent the back button from closing the activity.
        long time= System.currentTimeMillis();
        if (startMillis==0 || (time-startMillis> 3000) ) {
            startMillis=time;
            count=1;
        } else {
            count++;
        }

        if (count==5) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Parental Password");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text = input.getText().toString();
                    if (m_Text.equals(String.valueOf(sParental_Password))){
                        PackageManager pm = getPackageManager();
                        getPackageManager().clearPackagePreferredActivities(getPackageName());
                        int dis = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                        if(pm.getComponentEnabledSetting(cn1) == dis) dis = 3 - dis;
                        pm.setComponentEnabledSetting(cn1, dis, PackageManager.DONT_KILL_APP);
                        pm.setComponentEnabledSetting(cn2, 3 - dis, PackageManager.DONT_KILL_APP);
                        finish();
                    } else {
                        Toast.makeText(ParentalMode.this,"Invalid Password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
            fetchAppList();
        }
    }


}