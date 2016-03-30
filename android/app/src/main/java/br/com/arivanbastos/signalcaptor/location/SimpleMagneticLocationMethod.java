package br.com.arivanbastos.signalcaptor.location;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import br.com.arivanbastos.jillcore.location.BaseLocationAlgorithm;
import br.com.arivanbastos.jillcore.location.SimpleMagneticLocationAlgorithm;
import br.com.arivanbastos.jillcore.models.signal.SignalSource;
import br.com.arivanbastos.signalcaptor.location.exceptions.InvalidParameterValueException;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.signalcaptor.sensors.GeoMagneticSensor;
import br.com.arivanbastos.signalcaptor.sensors.events.ISensorManagerListener;

public class SimpleMagneticLocationMethod extends BaseLocationMethod implements ISensorManagerListener
{
    // Sensor.
    private GeoMagneticSensor sensor;

    // For how many miliseconds the method should gather and average mangetic signals
    // to indentify location.
    private long windowSize = 100;

    // Timer.
    private Timer timer;

    // ---------------------------------------------

    /**
     * Info.
     */

    /**
     *
     * @return
     */
    @Override
    public String getName()
    {
        return "Simple Magnetic";
    }

    @Override
    public String getDescription()
    {
        return "Averages magnetic signal and look for the dataset with the most nearest average. "+
               "Needs magnetic signals to work.";
    }

    @Override
    public List<LocationMethodParameter> getParameters() {
        ArrayList<LocationMethodParameter> result = new ArrayList<LocationMethodParameter>();
        result.add(new LocationMethodParameter("Window Size (ms)", LocationMethodParameter.TYPE_INT, 100));

        return result;
    }

    @Override
    public void setParameterValue(String parameterName, String value)
        throws InvalidParameterValueException {
        if (parameterName.equals("Window Size (ms)")) {
            try {
                this.windowSize = Long.parseLong(value);
            } catch (Exception e) {
                throw new InvalidParameterValueException();
            }
        }
    }

    public String getParameterValue(String parameterName)
    {
        if (parameterName.equals("Window Size (ms)")) {
            return windowSize+"";
        }

        return null;
    }

    @Override
    public void init() throws Exception
    {
        super.init();

        sensor = new GeoMagneticSensor();
        sensor.registerListener(this);
        sensor.init();
    }

    // --------------------------------------------

    /**
     * Tracking implementation.
     */

    /**
     *
     */
    @Override
    public void startLocation() {

        super.startLocation();

        sensor.beginRecord();

        // Starts a timer to stop tracking after windowSize
        // millseconds.
        Log.i("SimpleMagnetic", "startLocation() "+windowSize);
        timer = new Timer();
        timer.schedule(new SMTimerTask(this), windowSize);
    }

    @Override
    public void stopLocation() {
        super.stopLocation();

        sensor.endRecord();
    }

    public void endRecord() {
        sensor.endRecord();
    }

    public void finalize()
    {
        super.finalize();
        sensor.finalize();
    }

    // ------------------------------------------------------

    public void onSourceAvailable(SignalSource source){
    }

    public void onSourceUnavailable(SignalSource source){
    }

    @Override
    public void onRecordBegin() {
        // Nothing to do.
    }

    @Override
    public void onRecordProgress(SignalSource source, SignalSample sampleValue, SignalDataSet dataSet){
        // Nothing to do.
    }

    @Override
    public void onSingleSourceRecordEnd(SignalSource source, SignalDataSet dataSet){

    }

    @Override
    public void onAllSourcesRecordEnd(String signalTypeId, List<SignalDataSet> dataSets) {

        BaseLocationAlgorithm algorithm = new SimpleMagneticLocationAlgorithm(map);
        Point2.Double result = algorithm.run(dataSets);

        // Broadcasts onLocationInfoAvailable() event with
        // the identified point.
        super.onLocationEnd(result);
    }

    /**
     * Auxiliar Timer class.
     */
    private class SMTimerTask extends TimerTask {
        private SimpleMagneticLocationMethod locationMethod;

        private SMTimerTask(SimpleMagneticLocationMethod locationMethod) {
            this.locationMethod = locationMethod;
        }

        @Override
        public void run() {
            locationMethod.endRecord();
        }
    }
}
