package com.uhs.mobileantivirus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AntiPhishing extends AppCompatActivity {

    WebView webView;
    EditText editText;
    ProgressBar progressBar;
    ImageButton back, forward, stop, refresh, homeButton;
    Button goButton;
    ArrayList<String> FlaggedURL = new ArrayList<String>();
    int token = 0;
    String loadURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anti_phishing);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_uhs48dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        editText = (EditText) findViewById(R.id.web_address_edit_text);
        back = (ImageButton) findViewById(R.id.back_arrow);
        forward = (ImageButton) findViewById(R.id.forward_arrow);
        stop = (ImageButton) findViewById(R.id.stop);
        goButton = (Button)findViewById(R.id.go_button);
        refresh = (ImageButton) findViewById(R.id.refresh);
        homeButton = (ImageButton) findViewById(R.id.home);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setMax(100);
        progressBar.setVisibility(View.VISIBLE);
        webView = (WebView) findViewById(R.id.web_view);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("URList.txt"), "UTF-8"));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                FlaggedURL.add(mLine);
            }
        } catch (IOException e) {
            Log.i("Read URL Error: ", String.valueOf(e));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.i("Read IO Error: ", String.valueOf(e));
                }
            }
        }


        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setSupportMultipleWindows(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setBackgroundColor(Color.WHITE);

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    progressBar.setProgress(newProgress);
                    if (newProgress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                        progressBar.setVisibility(ProgressBar.VISIBLE);
                    }
                    if (newProgress == 100) {
                        progressBar.setVisibility(ProgressBar.GONE);
                    }else{
                        progressBar.setVisibility(ProgressBar.VISIBLE);
                    }
                }
            });
        }

        webView.setWebViewClient(new MyWebViewClient());
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                token = 0;
                String tempX = editText.getText().toString();
                if (tempX.substring(0,8).equals("https://")){
                    loadURL = editText.getText().toString();
                } else if (tempX.substring(0,7).equals("http://")) {
                    loadURL = "https://" + (editText.getText().toString()).substring(7,(editText.getText().toString()).length());
                    Toast.makeText(AntiPhishing.this,"auto changed to HTTPS", Toast.LENGTH_SHORT).show();
                } else {
                    loadURL = "https://" + editText.getText().toString();
                }
                for (int i = 0; i < FlaggedURL.size() ; i ++){
                    if (loadURL.equals(FlaggedURL.get(i))){
                        token = 1;
                    }
                }
                try {
                    if(!NetworkState.connectionAvailable(AntiPhishing.this)){
                        Toast.makeText(AntiPhishing.this, R.string.check_connection, Toast.LENGTH_SHORT).show();
                    }else {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        if (token <= 0){
                            webView.loadUrl(loadURL);
                            editText.setText("");
                        } else {
                            new AlertDialog.Builder(AntiPhishing.this)
                                    .setTitle("Malicious Link Detected")
                                    .setMessage("Are you sure you want to continue to this website?")
                                    // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            webView.loadUrl("https://" + editText.getText().toString());
                                            editText.setText("");
                                        }
                                    })

                                    // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(android.R.string.no, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.stopLoading();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("https://www.google.com");
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