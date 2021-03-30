package com.uhs.mobileantivirus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile extends AppCompatActivity {

    DatabaseLogin mDatabaseHelper;
    DatabaseCloud cDatabaseHelper;
    Button LOGOUT,CHANGE;
    Double dlongg, dlat;
    String LOGIN_STATUS,data1,data2,password,llogin,oldpw,newpw,newpw2;
    TextView userid,longitude,latitude,cloudbackup,lastlogin;
    EditText oldpwtv,newpwtv,newpwtv2;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbLL,dblong,dblat,dbPW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        LOGOUT = (Button)findViewById(R.id.LOGOUT);
        CHANGE = (Button)findViewById(R.id.change);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        mDatabaseHelper = new DatabaseLogin(this);
        Cursor cursor = mDatabaseHelper.getData();
        if(cursor.getCount ()==0){
            LOGIN_STATUS = "N";
        } else {
            while(cursor.moveToNext()) {
                data1 = cursor.getString(1);
                data2 = cursor.getString(2);
                if (data1.length() >= 3){
                    LOGIN_STATUS = "Y";
                } else {
                    LOGIN_STATUS = "N";
                }
            }
        }
        cDatabaseHelper = new DatabaseCloud(this);
        Cursor cursor2 = cDatabaseHelper.getStorage();
        if(cursor2.getCount ()==0){
            cloudbackup = findViewById(R.id.cloudbackup);
            cloudbackup.setText("0.00 KB");
        } else {
            while (cursor2.moveToNext()) {
                String cbackup = cursor2.getString(1);
                cloudbackup = findViewById(R.id.cloudbackup);
                cloudbackup.setText(String.valueOf(cbackup)+" KB");
            }
        }
        if(LOGIN_STATUS.equals("Y")){
            userid = findViewById(R.id.userid);
            userid.setText(String.valueOf(data1));
        } else {
            Toast.makeText(UserProfile.this,"LOGIN TO CONTINUE", Toast.LENGTH_LONG).show();
            startActivity(new Intent(UserProfile.this, MainActivity.class));
        }
        LOGOUT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LOGIN_STATUS = "N";
                AddData("N","N");
                DatabaseReference dbLS = database.getReference(data1).child("LOGIN_STATUS");
                dbLS.setValue("N");
                Toast.makeText(UserProfile.this,"LOGGED OUT", Toast.LENGTH_LONG).show();
                startActivity(new Intent(UserProfile.this, MainActivity.class));
            }
        });
        dbLL = database.getReference(data1).child("LAST_LOGIN");
        dbPW = database.getReference(data1).child("PASSWORD");
        dblong = database.getReference(data1).child("Longitude");
        dblat = database.getReference(data1).child("Latitude");
        dbLL.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                llogin = dataSnapshot.getValue(String.class);
                lastlogin = findViewById(R.id.lastlogin);
                String year = llogin.substring(0,4);
                String month = llogin.substring(4,6);
                String day = llogin.substring(6,8);
                String hour = llogin.substring(8,10);
                String min = llogin.substring(10,12);
                String ss = llogin.substring(12,14);
                lastlogin.setText(year+"/"+month+"/"+day+" "+hour+":"+min+":"+ss);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("FIREBASE: ", "Failed to read value.", error.toException());
            }
        });
        dblong.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dlongg = dataSnapshot.getValue(Double.class);
                longitude = findViewById(R.id.longitude);
                longitude.setText(String.valueOf(dlongg));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("FIREBASE: ", "Failed to read value.", error.toException());
            }
        });
        dblat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dlat = dataSnapshot.getValue(Double.class);
                latitude = findViewById(R.id.latitude);
                latitude.setText(String.valueOf(dlat));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("FIREBASE: ", "Failed to read value.", error.toException());
            }
        });
        dbPW.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                password = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("FIREBASE: ", "Failed to read value.", error.toException());
            }
        });
        CHANGE.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
                LayoutInflater inflater = UserProfile.this.getLayoutInflater();
                builder.setTitle("Change Password");
                builder.setView(R.layout.change_password);
                View container = inflater.inflate(R.layout.change_password, null);
                builder.setView(container);
                oldpwtv = container.findViewById(R.id.oldpw);
                newpwtv = container.findViewById(R.id.newpw);
                newpwtv2 = container.findViewById(R.id.newpw2);
                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        oldpw = oldpwtv.getText().toString();
                        newpw = newpwtv.getText().toString();
                        newpw2 = newpwtv2.getText().toString();
                        if (!oldpw.equalsIgnoreCase(password)){
                            Toast.makeText(UserProfile.this,"Invalid Old Password", Toast.LENGTH_LONG).show();
                        } else if (!newpw.equalsIgnoreCase(newpw2)){
                            Toast.makeText(UserProfile.this,"New Password not match", Toast.LENGTH_LONG).show();
                        } else {
                            dbPW.setValue(newpw);
                            Toast.makeText(UserProfile.this,"Password Changed!", Toast.LENGTH_LONG).show();
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
                builder.create();
            }
        });

    }

    public void AddData(String newEntry1, String newEntry2){
        boolean insertData = mDatabaseHelper.addData(newEntry1, newEntry2);
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