package com.billyhunt.shotloop;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class operationActivity extends baseWirelessActivity implements OnoffModel.OnoffStateListener, TempUpdateModel.TempUpdateListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        Log.i(PROGRESS_DEBUG_TAG, "onCreate");

        // Set listener for on / off variable to this class
        OnoffModel.getInstance().setListener(this);
        TempUpdateModel.getInstance().setListener(this);

        // Set on/off state to OFF
        OnoffModel.getInstance().changeState(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(PROGRESS_DEBUG_TAG, "onResume");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(PROGRESS_DEBUG_TAG, "onStop");
        // Remove listener
        OnoffModel.getInstance().removeListener();
        TempUpdateModel.getInstance().removeListener();
        // Disconnect from arduino
        boolean currentState = OnoffModel.getInstance().getState();
        if(currentState){
            // turn off espresso machine
            View currentView = findViewById(android.R.id.content);
            toggleWiFi(currentView);
        }
    }


    // Change button text
    @Override
    public void stateChanged() {
        // Save reference to on/off button
        final Button toggleButton = (Button) findViewById(R.id.button_onoff);
        // Save reference to image view
        final ImageView silviaImage = (ImageView) findViewById(R.id.imageView_Silvia_Top);
        // Get current on / off variable value
        boolean currentState = OnoffModel.getInstance().getState();
        // Update display
        if (currentState) {
            // Change text
            String offText = getString(R.string.button_off);
            toggleButton.setText(offText);
            // Change picture
            silviaImage.setImageResource(R.drawable.silvia_illustration_top_on);
        } else {
            // Change graphics to off mode
            String onText = getString(R.string.button_on);
            toggleButton.setText(onText);
            // Change picture
            silviaImage.setImageResource(R.drawable.silvia_illustration_top_off);
        }
    }

    // Update temperature display
    public void tempChanged() {
        Log.i(PROGRESS_DEBUG_TAG, "tempChanged");
        // Save reference to temperature display
        final TextView tempDisplay = (TextView) findViewById(R.id.temp_display);
        // Get current temperature
        Double temp = TempUpdateModel.getInstance().getTemp();
        // Create string with units
        String tempString = Double.toString(temp) + "°C";
        // Update temperature display
        tempDisplay.setText(tempString);

//        // Request new temperature
//        View currentView = findViewById(android.R.id.content);
//        if (TempUpdateModel.getInstance().getTempRequestFlag()) { // Check if request flag is set
//            requestTemp(currentView);
//        } else {
//            // Flag that next command can commence
//        }
//        // Request new temperature
//        View currentView = findViewById(android.R.id.content);
//        // Execute next command if queued
//        nextCommand(currentView);

    }



}
