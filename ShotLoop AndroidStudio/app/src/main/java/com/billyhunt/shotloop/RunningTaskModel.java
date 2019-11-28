package com.billyhunt.shotloop;

/**
 * Running task model class - used to create listener on variable change to inform of running async tasks
 * Idea taken from:
 * http://stackoverflow.com/questions/19026515/how-to-use-interface-to-communicate-between-two-activities
 */
public class RunningTaskModel {

    private RunningTaskModel() {}
    private static RunningTaskModel runningTaskInstance;
    public static RunningTaskModel getInstance() {
        if (runningTaskInstance == null) {
            runningTaskInstance = new RunningTaskModel();
        }
        return runningTaskInstance;
    }

    // Interface to communicate on/off state change
    public interface RunningTaskStateListener {
        void stateChanged();    // Method is implemented in activity
    }
    private RunningTaskStateListener runningTaskListener;

    // set listener
    public void setListener(RunningTaskStateListener listener) {
        runningTaskListener = listener;
    }

    // Variable to hold machine on / off state
    private boolean runningTaskState;

    public void changeState(boolean state) {
        if(runningTaskListener != null){
            runningTaskState = state;
            notifyStateChange();
        }
    }

    public void notifyStateChange() {
        runningTaskListener.stateChanged();
    }

    public boolean getState() {
        return runningTaskState;
    }

    public void removeListener(){
        runningTaskListener = null;
    }

}
