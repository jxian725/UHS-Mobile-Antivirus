package com.uhs.mobileantivirus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScanDevice extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    private ListView mListView;
    ArrayList<String> listItems = new ArrayList<String>();
    List<PackageInfo> packList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        packList = getPackageManager().getInstalledPackages(0);

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);
        for (int i=0; i < packList.size(); i++) {
            PackageInfo packInfo = packList.get(i);
            String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                Log.e("Installed App № " + Integer.toString(i), appName);
                listItems.add(appName);
            } else {
                Log.e("System App № " + Integer.toString(i), appName);
                listItems.add(appName);
            }
        }
        adapter.notifyDataSetChanged();

    }

    protected ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) findViewById(R.id.apps_list);
        }
        return mListView;
    }

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    protected ListAdapter getListAdapter() {
        ListAdapter adapter = getListView().getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter)adapter).getWrappedAdapter();
        } else {
            return adapter;
        }
    }

    public void systemApps(View v)
    {
        listItems = new ArrayList<String>();
        setListAdapter(null);
        getSupportActionBar().setTitle("  System Applications");
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);
        for (int i=0; i < packList.size(); i++) {
            PackageInfo packInfo = packList.get(i);
            String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            } else {
                Log.e("System App № " + Integer.toString(i), appName);
                listItems.add(appName);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void installedApps(View v)
    {
        listItems = new ArrayList<String>();
        setListAdapter(null);
        getSupportActionBar().setTitle("  User Installed Applications");
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);
        for (int i=0; i < packList.size(); i++) {
            PackageInfo packInfo = packList.get(i);
            String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                Log.e("Installed App № " + Integer.toString(i), appName);
                listItems.add(appName);
            }
        }
        adapter.notifyDataSetChanged();
    }


}