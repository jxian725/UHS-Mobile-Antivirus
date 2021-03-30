package com.uhs.mobileantivirus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    DatabaseLogin mDatabaseHelper;
    VideoView videoView;
    TextView title_av;
    ImageView title_img;
    EditText USERID;
    EditText PASSWORD;
    Button LOGIN;
    Button SKIP;
    String LOGIN_STATUS;
    String data1;
    String data2;
    String fbdata1;
    String fbdata2;
    int token = 0;
    SimpleDateFormat s = new SimpleDateFormat("yyyyMMddhhmmss");
    DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss").withZone(DateTimeZone.UTC);
    String timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        title_img = (ImageView)findViewById(R.id.title_img);
        title_av = (TextView)findViewById(R.id.title_av);
        title_img.setVisibility(View.GONE);
        title_av.setVisibility(View.GONE);
        videoView = (VideoView)findViewById(R.id.videoView);
        USERID = (EditText) findViewById(R.id.USERID);
        PASSWORD = (EditText)findViewById(R.id.PASSWORD);
        LOGIN = (Button)findViewById(R.id.LOGIN);
        SKIP = (Button)findViewById(R.id.SKIP);
        USERID.setVisibility(View.GONE);
        PASSWORD.setVisibility(View.GONE);
        LOGIN.setVisibility(View.GONE);
        SKIP.setVisibility(View.GONE);
        USERID.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);

        videoView.setVideoURI(video);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        timestamp = s.format(new Date());
        DateTime parsed = inputFormatter.parseDateTime(timestamp);
        //DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss").withZone(DateTimeZone.forID("Asia/Kuala_Lumpur"));
        DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        timestamp = outputFormatter.print(parsed);


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
                    DatabaseReference dbLL = database.getReference(data1).child("LAST_LOGIN");
                    DatabaseReference dbLS = database.getReference(data1).child("LOGIN_STATUS");
                        dbLL.setValue(timestamp);
                        dbLS.setValue("Y");
                } else {
                    LOGIN_STATUS = "N";
                }
            }
        }

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(isFinishing())
                    return;

                if (LOGIN_STATUS.equalsIgnoreCase("Y")){
                    startActivity(new Intent(MainActivity.this, ScanDevice.class));
                } else {
                    videoView.setVisibility(View.GONE);
                    LOGIN.setVisibility(View.VISIBLE);
                    SKIP.setVisibility(View.VISIBLE);
                    USERID.setVisibility(View.VISIBLE);
                    PASSWORD.setVisibility(View.VISIBLE);
                    title_img.setVisibility(View.VISIBLE);
                    title_av.setVisibility(View.VISIBLE);

                    LOGIN.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)  {
                            final String un = USERID.getText().toString();
                            final String pw = PASSWORD.getText().toString();
                            DatabaseReference dbID = database.getReference(un).child("USERID");
                            DatabaseReference dbPW = database.getReference(un).child("PASSWORD");
                            dbID.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    fbdata1 = dataSnapshot.getValue(String.class);
                                    if (fbdata1!=null){
                                        token = 1;
                                    } else {
                                        Toast.makeText(MainActivity.this,"INVALID USERNAME", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    // Failed to read value
                                    Log.i("FIREBASE: ", "Failed to read value.", error.toException());
                                }
                            });
                            if (token == 1) {
                                dbPW.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        fbdata2 = dataSnapshot.getValue(String.class);
                                        if (fbdata2.equals(String.valueOf(pw))) {
                                            token = 2;
                                            AddData(fbdata1,"Y");
                                            DatabaseReference dbLL = database.getReference(data1).child("LAST_LOGIN");
                                            DatabaseReference dbLS = database.getReference(data1).child("LOGIN_STATUS");
                                            dbLL.setValue(timestamp);
                                            dbLS.setValue("Y");
                                            Toast.makeText(MainActivity.this,"LOGGED IN", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(MainActivity.this, ScanDevice.class));
                                        } else {
                                            Toast.makeText(MainActivity.this,"INVALID PASSWORD", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        // Failed to read value
                                        Log.i("FIREBASE: ", "Failed to read value.", error.toException());
                                    }
                                });
                            }

                        }
                    });

                    SKIP.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v)  {
                            LOGIN_STATUS = "N";
                            AddData("N","N");
                            startActivity(new Intent(MainActivity.this, ScanDevice.class));
                        }
                    });

                }
            }
        });
        videoView.start();
    }

    public void AddData(String newEntry1, String newEntry2){
        boolean insertData = mDatabaseHelper.addData(newEntry1, newEntry2);
    }
}