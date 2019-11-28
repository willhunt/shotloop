package com.billyhunt.shotloop;

/**
 * Temperature change model class - used to create listener to update temperature display
 * Idea taken from:
 * http://stackoverflow.com/questions/19026515/how-to-use-interface-to-communicate-between-two-activities
 */
public class TempUpdateModel {

    private TempUpdateModel() {}
    private static TempUpdateModel tempUpdateInstance;
    public static TempUpdateModel getInstance() {
        if (tempUpdateInstance == null) {
            tempUpdateInstance = new TempUpdateModel();
        }
        return tempUpdateInstance;
    }

    // Interface to communicate on/off state change
    public interface TempUpdateListener {
        void tempChanged();    // Method is implemented in activity
    }
    private TempUpdateListener tempUpdateListener;

    // set listener
    public void setListener(TempUpdateListener listener) {
        tempUpdateListener = listener;
    }

    // Variable to hold current temperature
    private double currentTemp;

    public void updateTemperature(double temp) {
        if(tempUpdateListener != null){
            currentTemp = temp;
            notifyTempChange();
        }
    }

    public void notifyTempChange() {
        tempUpdateListener.tempChanged();
    }

    public double getTemp() {
        return currentTemp;
    }

    public void removeListener(){
        tempUpdateListener = null;
    }

}
