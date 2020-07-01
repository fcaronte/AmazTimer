package me.micrusa.amaztimer.utils;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.micrusa.amaztimer.R;
import me.micrusa.amaztimer.TCX.Constants;
import me.micrusa.amaztimer.TCX.SaveTCX;
import me.micrusa.amaztimer.TCX.data.Lap;
import me.micrusa.amaztimer.TCX.data.TCXData;
import me.micrusa.amaztimer.TCX.data.Trackpoint;
import me.micrusa.amaztimer.defValues;

@SuppressWarnings("CanBeFinal")
public class hrSensor implements SensorEventListener {
    private Context context;
    private SensorManager sensorManager;
    private Sensor hrSens;
    private TextView hrText;
    private final latestTraining latestTraining = new latestTraining();
    private long startTime;
    private int accuracy = 2;
    private int latestHr = 0;
    private String latestHrTime;

    //All tcx needed stuff
    private String currentLapStatus = Constants.STATUS_RESTING;
    private Lap currentLap;
    private TCXData TCXData;

    public hrSensor(Context c, TextView hr) {
        //Setup sensor manager, sensor and textview
        this.sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            this.hrSens = sensorManager.getDefaultSensor(defValues.HRSENSOR);
        }
        this.hrText = hr;
        this.context = c;
        this.TCXData = new TCXData();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int v = (int) event.values[0];
        if (isAccuracyValid() && v > 25 && v < 230 /*Limit to range 25-230 to avoid fake readings*/) {
            //Get hr value and set the text if battery saving mode is disabled
            if (!new file(defValues.SETTINGS_FILE, this.context).get(defValues.SETTINGS_BATTERYSAVING, defValues.DEFAULT_BATTERYSAVING)
                    && !new file(defValues.SETTINGS_FILE, this.context).get(defValues.SETTINGS_REPSMODE, defValues.DEFAULT_REPSMODE))
                this.hrText.setText(String.valueOf(v));
            //Send hr value to latestTraining array
            latestTraining.addHrValue(v);
            //Set latest hr value
            this.latestHr = v;
            String currentDate = new SimpleDateFormat(Constants.DATE_FORMAT).format(new Date()) + Constants.CHAR_DATETIME + new SimpleDateFormat(Constants.TIME_FORMAT).format(new Date()) + Constants.CHAR_AFTERTIME;
            //Create Trackpoint and add it to current Lap
            if (!currentDate.equals(this.latestHrTime))
                currentLap.addTrackpoint(new Trackpoint(v, new Date()));
            this.latestHrTime = currentDate;
        } else {
            //Logger.info("hrSensor: unvalid heart rate: " + String.valueOf(v) + " with " + String.valueOf(this.accuracy) + " accuracy");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor param1Sensor, int param1Int) {
        this.accuracy = param1Int;
    }

    public void registerListener() {
        //Clean all values to avoid merging other values
        latestTraining.cleanAllValues(this.context);
        //Register listener with delay in defValues class
        this.sensorManager.registerListener(this, this.hrSens, defValues.HRSENSOR_DELAY);
        //Register start time
        this.startTime = System.currentTimeMillis();
    }

    public int getLatestValue(){
        return this.latestHr;
    }

    private boolean isAccuracyValid(){
        return this.accuracy >= defValues.ACCURACY_RANGE[0] && this.accuracy <= defValues.ACCURACY_RANGE[1];
    }

    public void unregisterListener() {
        //Unregister listener to avoid battery drain
        this.sensorManager.unregisterListener(this, this.hrSens);
        //Save time and send it to latestTraining
        long endTime = System.currentTimeMillis();
        int totalTimeInSeconds = (int) (endTime - startTime) / 1000;
        latestTraining.saveDataToFile(this.context, totalTimeInSeconds);
        if (new file(defValues.SETTINGS_FILE, this.context).get(defValues.SETTINGS_TCX, defValues.DEFAULT_TCX)) {
            addCurrentLap();
            boolean result = SaveTCX.saveToFile(this.TCXData);
            resetTcxData();
            utils.setLang(this.context, new file(defValues.SETTINGS_FILE, this.context).get(defValues.SETTINGS_LANG, defValues.DEFAULT_LANG));
            if (result)
                Toast.makeText(this.context, R.string.tcxexporting, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this.context, R.string.tcxerror, Toast.LENGTH_SHORT).show();
        } else {
            resetTcxData();
        }
    }

    private void resetTcxData(){
        this.currentLap = null;
        this.TCXData = new TCXData();
    }

    private void addCurrentLap(){
        if (this.currentLap != null) {
            this.currentLap.setIntensity(this.currentLapStatus);
            this.currentLap.endLap(System.currentTimeMillis());
            file bodyFile = new file(defValues.BODY_FILE, this.context);
            this.currentLap.calcCalories(bodyFile.get(defValues.SETTINGS_AGE, defValues.DEFAULT_AGE),
                    bodyFile.get(defValues.SETTINGS_WEIGHT, defValues.DEFAULT_WEIGHT),
                    bodyFile.get(defValues.SETTINGS_MALE, defValues.DEFAULT_MALE));
            this.TCXData.addLap(this.currentLap);
        }
    }

    public void newLap(String lapStatus){
        this.addCurrentLap();
        this.currentLapStatus = lapStatus;
        this.currentLap = new Lap();
    }
}