package br.com.arivanbastos.signalcaptor.location;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import br.com.arivanbastos.jillcore.location.BaseLocationAlgorithm;
import br.com.arivanbastos.jillcore.location.GeoNLocationAlgorithm;
import br.com.arivanbastos.jillcore.models.signal.SignalSource;
import br.com.arivanbastos.signalcaptor.location.exceptions.InvalidParameterValueException;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.signalcaptor.sensors.BLESensor;
import br.com.arivanbastos.signalcaptor.sensors.events.ISensorManagerListener;

public class GeoNLocationMethod extends BaseLocationMethod implements ISensorManagerListener
{
    // Sensor.
    private BLESensor sensor;

    // For how many miliseconds the method should gather and average BLE signals
    // to indentify location.
    private long windowSize = 1000;

    // Timer.
    private Timer timer;

    // ---------------------------------------------

    @Override
    public String getName() {
        return "GeoN";
    }

    @Override
    public String getDescription() {
        return "A cluster algorithm that uses circle intersections to find an aproximated location estimative.";
    }

    @Override
    public List<LocationMethodParameter> getParameters() {
        ArrayList<LocationMethodParameter> result = new ArrayList<LocationMethodParameter>();
        result.add(new LocationMethodParameter("Window Size (ms)"   , LocationMethodParameter.TYPE_INT, 1000));
        //result.add(new LocationMethodParameter("0 meters RSSI"      , LocationMethodParameter.TYPE_INT, -55));
        //result.add(new LocationMethodParameter("RSSI lost per meter", LocationMethodParameter.TYPE_INT, 3));

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
        // @todo This should be dynamic based on current device.
        // @see http://altbeacon.github.io/android-beacon-library/distance-calculations.html
        sensor = new BLESensor();
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
        Log.i("GeoN", "startLocation() " + windowSize);
        timer = new Timer();
        timer.schedule(new GeoNTimerTask(this), windowSize);
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


    // --------------------------------------------------

    /**
     * ISensorManagerListener
     */

    @Override
    public void onSourceAvailable(SignalSource source) {}

    @Override
    public void onSourceUnavailable(SignalSource source) {}

    @Override
    public void onRecordBegin() {}

    @Override
    public void onRecordProgress(SignalSource source, SignalSample sampleValue, SignalDataSet dataSet) {}

    @Override
    public void onSingleSourceRecordEnd(SignalSource source, SignalDataSet dataSet) {}

    @Override
    public void onAllSourcesRecordEnd(String signalTypeId, List<SignalDataSet> dataSets)
    {
        debug("Samples collection finished.");

        BaseLocationAlgorithm algorithm = new GeoNLocationAlgorithm(map);
        Point2.Double result = algorithm.run(dataSets);

        super.onLocationEnd(result);
    }

    // ---------------------------------------------

    /**
     * Auxiliar Timer class.
     */
    private class GeoNTimerTask extends TimerTask {
        private GeoNLocationMethod locationMethod;

        private GeoNTimerTask(GeoNLocationMethod locationMethod) {
            this.locationMethod = locationMethod;
        }

        @Override
        public void run() {
            locationMethod.endRecord();
        }
    }
}
