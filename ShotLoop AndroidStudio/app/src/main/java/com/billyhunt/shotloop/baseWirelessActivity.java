package com.billyhunt.shotloop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base activity to implement wireless communication functionality
 */
public class baseWirelessActivity extends AppCompatActivity implements AsyncResponse {

    // Create message log filter
    public static final String PROGRESS_DEBUG_TAG = "filterMessages";
    // Create lof for network connections
    public static final String NETWORK_DEBUG_TAG = "NetworkStatusMessages";

    // Commands TO Arduino
    public static final String     REQUEST_ON = "n";                         // Turn machine on
    public static final String     REQUEST_OFF = "f";                        // Turn machine off
    public static final String     REQUEST_CONNECT = "c";                    // Check devices are talking to each other
    public static final String     REQUEST_DISCONNECT = "d";                 // Close connection
    public static final String     REQUEST_TEMP = "t";                       // Request temperature reading
    // Commands FROM Arduino
    public static final char     CONFIRM_ON = 'N';                         // Turn machine on
    public static final char     CONFIRM_OFF = 'F';                        // Turn machine off
    public static final char     CONFIRM_CONNECT = 'C';                    // Check devices are talking to each other
    public static final char     CONFIRM_DISCONNECT = 'D';                 // Close connection
    public static final char     CONFIRM_TEMP = 'T';                       // Close connection
    public static final char     CONFIRM_GAIN = 'G';                       // Confirm gains have been changed


    // Handler for periodic temperature update (or next wifi command)
    Handler tempUpdateHandler = new Handler();
    // Flags for temperature update status
    private volatile boolean requestTempFlag;
    private volatile boolean gettingTempFlag;


    /*
     * Method to call when connect WiFi button is pressed
     */
    public void connectWiFi (View v) {
        Log.i(PROGRESS_DEBUG_TAG, "connectWiFi");
        // send connect command
        if(!sendCommand(REQUEST_CONNECT, v, true)) {
            basicToast("Could not connect");
        }
    }

    /*
     * Method to call when disconnect WiFi button is pressed
     */
    public void disconnectWiFi(View v) {
        Log.i(PROGRESS_DEBUG_TAG, "disconnectWiFi");

        // Stop temperature updates
        //stopTempUpdate();

        if(!sendCommand(REQUEST_DISCONNECT, v, true)) {
            basicToast("Could not disconnect");
        }
    }

    /*
     * Method to call when turn on / off button is pressed
     */
    public void toggleWiFi (View v) {
        // Get current on / off variable value
        boolean currentState = OnoffModel.getInstance().getState();

        // Stop temperature updates
        //stopTempUpdate();

        // Turn machine on
        if(!currentState) {                         // If off
            Log.i(PROGRESS_DEBUG_TAG, "onWiFi");    // Turn on
            // send connect command
            if(!sendCommand(REQUEST_ON, v, true)) {
                basicToast("Could not turn machine on");
            }

        // Turn machine off
        } else {
            Log.i(PROGRESS_DEBUG_TAG, "offWiFi");
            if(!sendCommand(REQUEST_OFF, v, true)) {
                basicToast("Could not turn machine off");
            }
        }
    }

    /*
    * Method to set requestTempFlag
    */
    public synchronized void setRequestTempFlag(Boolean set) {
        requestTempFlag = set;
    }
    /*
     * Method to get requestTempFlag
     */
    public synchronized boolean getRequestTempFlag() {
        return requestTempFlag;
    }
    /*
     * Method to set gettingTempFlag
     */
    public synchronized void setGettingTempFlag(Boolean set) {
        gettingTempFlag = set;
    }
    /*
    * Method to get gettingTempFlag
    */
    public synchronized boolean getGettingTempFlag() {
        return gettingTempFlag;
    }

    /*
     * Method used to request and update temperature
     */
    public void requestTemp (View v) {
        Log.i(PROGRESS_DEBUG_TAG, "requestTemp");

        // Update temperature
        if(!sendCommand(REQUEST_TEMP, v, true)) {
            basicToast("Could not update temperature");
        }
    }

    /*
     * Method used to resume temperature updates
     */
    public void resumeTempUpdate () {
        // Start requesting temperature
        //TempUpdateModel.getInstance().setTempRequestFlag(true);
        //requestTempFlag = true;
        //gettingTempFlag = true;
        setRequestTempFlag(true);
        setGettingTempFlag(true);

        // New runnable to execute in separate thread
        Runnable tempRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(PROGRESS_DEBUG_TAG, "tempRunnableRun");
                nextCommand();
            }
        };
        new Thread(tempRunnable).start();
    }

    /*
     * Method used to stop temperature updates
     */
    public void stopTempUpdate () {
        // Stop requesting temperature
        //TempUpdateModel.getInstance().setTempRequestFlag(false);
        //requestTempFlag = false;
        setRequestTempFlag(false);
    }

    /*
     * Method used to request PID gain changes
     */
    public void changePidGains (View v, float P, float I, float D) {
        Log.i(PROGRESS_DEBUG_TAG, "changePidGains");
        // Create string for arduino
        String gainChange = "P" + Float.toString(P) + "I" + Float.toString(I) + "D" + Float.toString(D);
        // Send command to change PID gains
        if(!sendCommand(gainChange, v, true)) {
            basicToast("Could not change PID gains");
        }
    }

    /*
     * Check that app is connected to network
     */
    public boolean isOnline() {
        // Connectivity manager answers queries about the state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // NetworkInfo describes the status of a network interface of a given type
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Send message to arduino and return response
     */
    public Boolean sendCommand(String command, View v, boolean alertBool) {
        Log.i(PROGRESS_DEBUG_TAG, "sendCommand");
        // Check network connection
        if (isOnline()) {
            Log.i(NETWORK_DEBUG_TAG, "WiFi confirmed as on");
            // Get IP address
            SharedPreferences sharedPrefIP = PreferenceManager.getDefaultSharedPreferences(this);
            String ipAddress = sharedPrefIP.getString("pref_machine_IP", null);
            if (ipAddress == null) {
                // Check that there is an IP address
                basicToast("Enter IP address in settings");
                return false;
            }
            // Get port
            SharedPreferences sharedPrefPort = PreferenceManager.getDefaultSharedPreferences(this);
            String portNumber = sharedPrefPort.getString("pref_machine_port", null);
            if (portNumber == null) {
                // Check that there is an IP address
                basicToast("Enter IP address in settings");
                return false;
            }
            // Send command
            HttpRequestAsyncTask sendTask = new HttpRequestAsyncTask(v.getContext(), command, ipAddress, portNumber, alertBool);
            sendTask.execute();

            // Do something (?) with the interface class invoked by sendTask
            sendTask.delegate = this;

        } else {
            // display error
            basicToast("WiFi not connected");
            return false;
        }
        return true;
    }

    /*
     * Run after response received from AsyncTask
     */
    public void responseServer(String output) {
        Log.i(PROGRESS_DEBUG_TAG, "responseServer");
        // Check if connected
        switch (output.charAt(0)) {
            case CONFIRM_CONNECT: {
                Log.i(NETWORK_DEBUG_TAG, "Connected");
                // Get machine name
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String machineNamePref = sharedPref.getString("pref_machine_name", getString(R.string.home_machine_name));
                String connectToastStr = String.format("Connected to %s", machineNamePref);
                basicToast(connectToastStr);

                // Start operation activity if connection successful
                Intent operationIntent = new Intent(this, operationActivity.class);
                startActivity(operationIntent);

                // Update temperature
                //resumeTempUpdate();

                break;

            } case CONFIRM_DISCONNECT: {
                Log.i(NETWORK_DEBUG_TAG, "Disconnected");
                // Get machine name
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String machineNamePref = sharedPref.getString("pref_machine_name", getString(R.string.home_machine_name));
                String disconnectToastStr = String.format("Disconnected from %s", machineNamePref);
                basicToast(disconnectToastStr);

                // Start home activity if disconnection successful
                Intent operationIntent = new Intent(this, homeActivity.class);
                startActivity(operationIntent);
                break;

            } case CONFIRM_ON: {
                Log.i(NETWORK_DEBUG_TAG, "Machine on");
                // Get current state
                boolean currentState = OnoffModel.getInstance().getState();
                // Check not currently on
                if (!currentState){
                    // Change flag to on
                    OnoffModel.getInstance().changeState(true);
                }
                // Update temperature
                //resumeTempUpdate();
                break;

            } case CONFIRM_OFF: {
                Log.i(NETWORK_DEBUG_TAG, "Machine off");
                // Get current state
                boolean currentState = OnoffModel.getInstance().getState();
                // Check not currently off
                if (currentState){
                    // Change flag to on
                    OnoffModel.getInstance().changeState(false);
                }
                // Update temperature
                //resumeTempUpdate();
                break;

            }case CONFIRM_TEMP: {
                // Parse temperature from output
                String tempString = output.substring(1);
                double tempDouble = Double.parseDouble(tempString);
                // Update temperature in interface with listener
                TempUpdateModel.getInstance().updateTemperature(tempDouble);
                // Log temperature in memory (for graphing)
                break;

            } case CONFIRM_GAIN: {
                basicToast("Gains updated");
                break;

            } default: {
                if (output.equals("ERROR")) {
                    basicToast("Machine not responding");
                } else {
                    basicToast("Unknown response: " + output);
                }
                break;

            }
        }
    }

    /*
     * Decides on next command when temp update is running
     */
    public void nextCommand() {
        Log.i(PROGRESS_DEBUG_TAG, "nextCommand");

        // Loop while temperature flag is set
        while (getRequestTempFlag()){
            // Wait for time period
            waitPeriod();
            // Request next temperature
            tempUpdateHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Update temperature
                    final View currentView = findViewById(android.R.id.content);
                    // Update temperature
                    requestTemp(currentView);
                }
            });
        }
        // Let it be known that temperature updates have stopped
        tempUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                setGettingTempFlag(false);
            }
        });
    }

    /**
     * An AsyncTask is needed to execute HTTP requests in the background so that they do not
     * block the user interface.
     */
    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {
        // Instantiate interface for saving response
        public AsyncResponse delegate = null;
        // declare variables needed
        private String requestReply, ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;
        private String command;
        private boolean alertBool;                  // Decides weather alert dialog is shown on request

        /**
         * The asyncTask class constructor. Assigns the values used in its other methods.
         */
        public HttpRequestAsyncTask(Context context, String command, String ipAddress, String portNumber, boolean alertBool) {
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("Status response:")
                    .setCancelable(true)
                    .create();

            this.command = command;
            this.ipAddress = ipAddress;
            this.portNumber = portNumber;
            this.alertBool = alertBool; // Flag to show progress alert dialog
        }

        /**
         * Description: This function is executed before the HTTP request is sent to ip address.
         * The function will set the dialog's message and display the dialog.
         */
        @Override
        protected void onPreExecute() {
            // Show dialog box if alert boolean set
            if(alertBool) {
                alertDialog.setMessage("Sending data, please wait...");
                if(!alertDialog.isShowing()) {
                    Log.i(PROGRESS_DEBUG_TAG, "onPreExecute");
                    alertDialog.show();
                }
            }
        }

        /**
         * Description: Sends the request to the ip address
         */
        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(PROGRESS_DEBUG_TAG, "doInBackground");
            // Show dialog box if alert boolean set
            if(alertBool) {
                alertDialog.setMessage("Data sent, waiting for reply from server...");
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            }
            /*
            // Check and wait if temperature update is occurring
            while (getGettingTempFlag() && !getRequestTempFlag()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            */
            // Send request to arduino and save response
            requestReply = sendRequest(command, ipAddress, portNumber);
            return null;
        }

        /**
         * This function is executed after the HTTP request returns from the ip address.
         * The function sets the dialog's message with the reply text from the server and display the dialog
         * if it's not displayed already (in case it was closed by accident);
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(PROGRESS_DEBUG_TAG, "onPostExecute");
            if(alertBool) {
                alertDialog.hide(); // Hide dialog
            }
            delegate.responseServer(requestReply);
        }
    }

    /*
     * Send HTTP request
     */
    public String sendRequest(String command, String ipAddress, String portNumber) {
        Log.i(PROGRESS_DEBUG_TAG, "sendRequest");
        String serverResponse = "ERROR";        // Initialise return value

        try {
            // Define the URL e.g. http://myIPaddress:myport/?command
            String urlString = String.format("http://%s:%s/?%s:", ipAddress, portNumber, command);
            Log.i(NETWORK_DEBUG_TAG, urlString);
            URL url = new URL(urlString);
            // Open connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Allow outward data
            urlConnection.setDoOutput(true);
            // Increase speed by specifying max length of data chunk (0 default = 1024)
            urlConnection.setChunkedStreamingMode(0);
            // Get output stream to write data to
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            // Write output data
            out.write(command.getBytes());

            // Read data
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            serverResponse = inputstream2string(in);
            // Disconnect
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        // return the server's reply/response text
        Log.i(NETWORK_DEBUG_TAG, serverResponse);
        return serverResponse;
    }

    /*
    * Converts an Input stream to a string
    */
    public String inputstream2string(InputStream stream){
        // Create string builder to append read charaters
        StringBuilder sb = new StringBuilder();
        // Initialise next character object
        int nextInt;
        try{
            while ((nextInt = stream.read()) != -1) {
                // Cast int as char (inputSTream reads data in int's)
                Character nextChar = (char) nextInt;
                sb.append(nextChar);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        // Return data as string
        return sb.toString();
    }

    /*
    * Show toast for user feedback, errors, etc
    */
    public void basicToast(String toastText)
    {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, toastText, duration);
        toast.show();
    }

    /*
     * Wait to give Arduino a chance
     */
    private void waitPeriod() {
        // Wait for 1 second before requesting temperature
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

