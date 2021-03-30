package com.uhs.mobileantivirus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class AntiTheft extends AppCompatActivity {

    private static final int REQUEST_CODE = 2;
    DatabaseLogin mDatabaseHelper;
    private LocationManager locationManager=null;
    private LocationListener locationListener=null;
    private MapView mapView;
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private EditText editLocation = null;
    private ProgressBar pb;
    private static final String TAG = "Debug";
    private Boolean flag = false;
    private DatabaseReference dbLong;
    private DatabaseReference dbLat;
    String LOGIN_STATUS;
    String data1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(AntiTheft.this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_anti_theft);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setRequestedOrientation(ActivityInfo
                .SCREEN_ORIENTATION_PORTRAIT);
        editLocation = (EditText) findViewById(R.id.editTextLocation);
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        mapView = (MapView) findViewById(R.id.mapView);
        pb.setVisibility(View.INVISIBLE);

        mDatabaseHelper = new DatabaseLogin(this);
        Cursor cursor = mDatabaseHelper.getData();
        if(cursor.getCount ()==0){
            LOGIN_STATUS = "N";
        } else {
            while(cursor.moveToNext()) {
                data1 = cursor.getString(1);
                if (data1.length() >= 3){
                    LOGIN_STATUS = "Y";
                } else {
                    LOGIN_STATUS = "N";
                }
            }
        }

        if (!LOGIN_STATUS.equals("Y")){
            Toast.makeText(AntiTheft.this,"LOGIN TO CONTINUE", Toast.LENGTH_LONG).show();
            startActivity(new Intent(AntiTheft.this, MainActivity.class));
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v("PERMISSION CHECK","Permission is granted");
            //File write logic here
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        dbLong = FirebaseDatabase.getInstance().getReference(data1).child("Longitude");
        dbLat = FirebaseDatabase.getInstance().getReference(data1).child("Latitude");

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        flag = displayGpsStatus();
        if (flag) {
            editLocation.setGravity(Gravity.CENTER);
            editLocation.setText("Locating Device...");
            pb.setVisibility(View.VISIBLE);
            locationListener = new MyLocationListener();

            locationManager.requestLocationUpdates(LocationManager
                    .GPS_PROVIDER, 5000, 10,locationListener);
        } else {
            alertbox("Gps Status!!", "Your GPS is: OFF");
        }
    }

    /*--------------Check GPS ON/OFF ---------- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    /*---------- GPS LOCATION OFF ------------- */
    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Device's GPS is Disable")
                .setCancelable(false)
                .setTitle("** Gps Status **")
                .setPositiveButton("Gps On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                Intent myIntent = new Intent(
                                        Settings.ACTION_SECURITY_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(final Location loc) {
            editLocation.setText("");
            pb.setVisibility(View.INVISIBLE);
            editLocation.setGravity(Gravity.LEFT);
            editLocation.setTextColor(getResources().getColor(R.color.white));
            final String longitude = "Longitude: " +loc.getLongitude();
            final String latitude  = "Latitude : " +loc.getLatitude();
            final double longtt = loc.getLongitude();
            final double latt = loc.getLatitude();
            dbLong.setValue(longtt);
            dbLat.setValue(latt);
            /*----------to get City-Name from coordinates ------------- */
            String cityName=null;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address>  addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                cityName=addresses.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String s = longitude+"\n"+latitude +
                    "\n\nCurrent Location: "+cityName;
            editLocation.setText(s);
            final CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(latt, longtt))
                    .zoom(12)
                    .tilt(20)
                    .build();
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                    List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
                    symbolLayerIconFeatureList.add(Feature.fromGeometry(
                            Point.fromLngLat(longtt, latt)));
                    mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                                    .withImage(ICON_ID, BitmapFactory.decodeResource(
                                            AntiTheft.this.getResources(), R.drawable.mapbox_marker_icon_default))
                                    .withSource(new GeoJsonSource(SOURCE_ID,
                                            FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
                                    .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                                    .withProperties(
                                                            iconImage(ICON_ID),
                                                            iconAllowOverlap(true),
                                                            iconIgnorePlacement(true)
                                                    )
                                    ), new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);
                        }
                    });
                }
            });
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
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
}