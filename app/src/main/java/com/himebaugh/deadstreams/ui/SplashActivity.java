/**
 * SplashActivity.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.InitializeDatabaseJobService;
import com.himebaugh.deadstreams.database.LoadDataResultReceiver;
import com.himebaugh.deadstreams.utils.NetworkUtils;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

import static com.himebaugh.deadstreams.database.InitializeDatabaseJobService.DOWNLOAD_RESULT;


public class SplashActivity extends AppCompatActivity implements LoadDataResultReceiver.Receiver {

    private final static String TAG = SplashActivity.class.getSimpleName();

    private TextView mLogoTextView;
    private TextView mMessageTextView;

    private ProgressBar mLoadingIndicator;

    private Boolean mNetworkConnection = false;

    private LoadDataResultReceiver resultReceiver;

    NetworkBroadcastReceiver mNetworkReceiver;
    IntentFilter mNetworkIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate: ");

        if (initialDownloadComplete()) {
            Intent intent = new Intent(SplashActivity.this, ShowListActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_splash);

        mLogoTextView = findViewById(R.id.logoTextView);
        mLogoTextView.setText(R.string.app_name);

        mMessageTextView = findViewById(R.id.messageTextView);

        mLoadingIndicator = findViewById(R.id.progressBar);
        mLoadingIndicator.getIndeterminateDrawable().setColorFilter(0xFFcc0000, android.graphics.PorterDuff.Mode.SRC_ATOP);

        mNetworkReceiver = new NetworkBroadcastReceiver();
        mNetworkIntentFilter = new IntentFilter();
        mNetworkIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        mNetworkConnection = NetworkUtils.isNetworkAvailable(this);

        if (mNetworkConnection) {
            download();
        }

    }

    public void download() {

        if (!initialDownloadComplete()) {
            mMessageTextView.setText(R.string.splash_activity_message_downloading_shows);
            mLoadingIndicator.setVisibility(View.VISIBLE);
            //mLoadingIndicator.setBackgroundColor(Color.BLACK);

            resultReceiver = new LoadDataResultReceiver(new Handler());
            resultReceiver.setReceiver(this);

            Log.i(TAG, "download: call LoadDataJobService.startActionLoadData");
            InitializeDatabaseJobService.startActionLoadData(this, resultReceiver);

            //         if (isSuccessful.contentEquals("True")) {
            //
            //            mLoadingIndicator.setVisibility(View.INVISIBLE);
            //
            //            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            //            SharedPreferences.Editor editor = sharedPreferences.edit();
            //            editor.putBoolean("initial_download_complete", true);
            //            editor.apply();
            //
            //            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            //            startActivity(intent);
            //            finish();
            //        }
        } else {
            Intent intent = new Intent(SplashActivity.this, ShowListActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public Boolean initialDownloadComplete() {
        Log.i(TAG, "initialDownloadComplete: Y/N?");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(getString(R.string.initial_download_complete), false);
    }


    // COMPLETED (7) Override onResume and setup your broadcast receiver. Do this by calling
    // registerReceiver with the ChargingBroadcastReceiver and IntentFilter.
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNetworkReceiver, mNetworkIntentFilter);
    }


    // COMPLETED (8) Override onPause and unregister your receiver using the unregisterReceiver method
    @Override
    protected void onPause() {
        super.onPause();
        // timer.cancel();
        unregisterReceiver(mNetworkReceiver);
    }

    // COMPLETED (1) Create a new method called showCharging which takes a boolean. This method should
    // either change the image of mChargingImageView to ic_power_pink_80px if the boolean is true
    // or R.drawable.ic_power_grey_80px it it's not. This method will eventually update the UI
    // when our broadcast receiver is triggered when the charging state changes.
    private void showConnectivityStatus(boolean isNetworkActive) {
        if (isNetworkActive) {
            // mMessageTextView.setText("Downloading Stations...");
            if (!mNetworkConnection) {

                // Wait for internet connection to really come back
                try {
                    TimeUnit.SECONDS.sleep(3);
                    // Thread.sleep(3000);                 //1000 milliseconds is one second.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                download();
            }
        } else {
            mMessageTextView.setText(R.string.splash_activity_message_no_internet);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mNetworkConnection = false;
        }
    }

    // COMPLETED (2) Create an inner class called ChargingBroadcastReceiver that extends BroadcastReceiver
    private class NetworkBroadcastReceiver extends BroadcastReceiver {
        // COMPLETED (3) Override onReceive to get the action from the intent and see if it matches the
        // Intent.ACTION_POWER_CONNECTED. If it matches, it's charging. If it doesn't match it's not
        // charging.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            boolean connectivityChange = (action.equals("android.net.conn.CONNECTIVITY_CHANGE"));

            if (connectivityChange) {
                // COMPLETED (4) Update the UI using the showCharging method you wrote
                // NetworkUtil.isNetworkActive(context)
                // Util.isNetworkAvailable(context)
                showConnectivityStatus(NetworkUtils.isNetworkAvailable(context));
            }

        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case DOWNLOAD_RESULT:
                if (resultData != null) {
                    // if
                    if (resultData.getBoolean(getString(R.string.initial_download_complete), false)) {
                        Intent intent = new Intent(SplashActivity.this, ShowListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                break;
        }
    }

}