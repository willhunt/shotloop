package com.billyhunt.shotloop;

/**
 * on / off model class - used to create listener on variable change to inform machine on / off status
 * Idea taken from:
 * http://stackoverflow.com/questions/19026515/how-to-use-interface-to-communicate-between-two-activities
 */
public class OnoffModel {

    private OnoffModel() {}
    private static OnoffModel onoffInstance;
    public static OnoffModel getInstance() {
        if (onoffInstance == null) {
            onoffInstance = new OnoffModel();
        }
        return onoffInstance;
    }

    // Interface to communicate on/off state change
    public interface OnoffStateListener {
        void stateChanged();    // Method is implemented in activity
    }
    private OnoffStateListener onoffListener;

    // set listener
    public void setListener(OnoffStateListener listener) {
        onoffListener = listener;
    }

    // Variable to hold machine on / off state
    private boolean onoffState;

    public void changeState(boolean state) {
        if(onoffListener != null){
            onoffState = state;
            notifyStateChange();
        }
    }

    public void notifyStateChange() {
        onoffListener.stateChanged();
    }

    public boolean getState() {
        return onoffState;
    }

    public void removeListener(){
        onoffListener = null;
    }

}
