package com.billyhunt.shotloop;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;    // Display filtered messages in console from this log


public class homeActivity extends baseWirelessActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.i(PROGRESS_DEBUG_TAG, "onCreate"); // Log this message
        // Set temperature request flag to false
        setRequestTempFlag(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(PROGRESS_DEBUG_TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(PROGRESS_DEBUG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(PROGRESS_DEBUG_TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(PROGRESS_DEBUG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(PROGRESS_DEBUG_TAG, "onDestroy");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        // Switch to appropriate activity
        switch (item.getItemId()) {
            // Settings
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, settingsActivity.class);
                startActivity(settingsIntent);
                return true;
            // About
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, aboutActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
