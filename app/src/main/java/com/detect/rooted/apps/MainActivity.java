package com.detect.rooted.apps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "RootDetector";
    private TextView resultTextView;
    private Button scanAgainButton;

    private static final List<String[]> KNOWN_ROOT_APPS = new ArrayList<String[]>();
    
    static {
        KNOWN_ROOT_APPS.add(new String[]{"com.dergoogler.mmrl", "com.dergoogler.mmrl.ui.activity.webui.WebUIActivity"});
        KNOWN_ROOT_APPS.add(new String[]{"com.rifsxd.ksunext", "com.rifsxd.ksunext.ui.webui.WebUIActivity"});
        KNOWN_ROOT_APPS.add(new String[]{"com.topjohnwu.magisk", "com.topjohnwu.magisk.ui.surequest.SuRequestActivity"});
        KNOWN_ROOT_APPS.add(new String[]{"com.tsng.hidemyapplist", "com.google.android.gms.ads.AdActivity"});
        KNOWN_ROOT_APPS.add(new String[]{"io.github.huskydg.magisk", "com.topjohnwu.magisk.ui.surequest.SuRequestActivity"});
        KNOWN_ROOT_APPS.add(new String[]{"io.github.vvb2060.magisk", "com.topjohnwu.magisk.ui.surequest.SuRequestActivity"});
        KNOWN_ROOT_APPS.add(new String[]{"me.bmax.apatch", "me.bmax.apatch.ui.WebUIActivity"});
        KNOWN_ROOT_APPS.add(new String[]{"me.weishu.kernelsu", "me.weishu.kernelsu.ui.webui.WebUIActivity"});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        resultTextView = findViewById(R.id.resultTextView);
        scanAgainButton = findViewById(R.id.scanAgainButton);
        
        scanAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetection();
            }
        });
        
        startDetection();
    }
    
    private void startDetection() {
        resultTextView.setText("Scanning...");
        scanAgainButton.setEnabled(false);
        new DetectionTask().execute();
    }
    
    private class DetectionTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {               
            }
            return detectKnownRootApps(MainActivity.this);
        }
        
        @Override
        protected void onPostExecute(List<String> knownRootApps) {
            scanAgainButton.setEnabled(true);
            displayResults(knownRootApps);
        }
    }
    
    private void displayResults(List<String> knownRootApps) {
        StringBuilder result = new StringBuilder();
        
        if (knownRootApps != null && !knownRootApps.isEmpty()) {
            result.append("=== DETECTED ROOT APPS ===\n\n");
            for (String app : knownRootApps) {
                result.append("• ").append(app).append("\n");
            }            
            result.append("\n⚠️ Root management apps found!");
        } else {
            result.append("No root-related apps detected.\n\n");
        }
        
        resultTextView.setText(result.toString());
    }

    private List<String> detectKnownRootApps(Context context) {
        List<String> foundApps = new ArrayList<String>();
        Log.d(TAG, "Checking for " + KNOWN_ROOT_APPS.size() + " known root apps");

        for (String[] appActivity : KNOWN_ROOT_APPS) {
            String packageName = appActivity[0];
            String activityName = appActivity[1];
            
            try {
                Intent intent = new Intent();
                intent.setPackage(packageName);
                intent.setClassName(packageName, activityName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                try {
                    context.startActivity(intent);
                    // If we reach here, the app exists and we can start its activity
                    Log.d(TAG, "Successfully started activity for root app: " + packageName);
                    foundApps.add(packageName);
                } catch (SecurityException e) {
                    // SecurityException indicate the app exists but we can't start it
                    Log.d(TAG, "SecurityException for root app " + packageName + ": " + e.getMessage());
                    foundApps.add(packageName);
                } catch (Exception e) {
                    // Other exceptions likely mean the app isn't installed
                    Log.d(TAG, "App not installed: " + packageName);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking for root app " + packageName, e);
            }
        }

        if (!foundApps.isEmpty()) {
            Log.d(TAG, "Found " + foundApps.size() + " root apps");
        } else {
            Log.d(TAG, "No known root apps found");
        }
        return foundApps;
    }
}
