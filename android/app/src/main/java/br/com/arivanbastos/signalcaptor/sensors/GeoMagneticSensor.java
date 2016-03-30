package br.com.arivanbastos.signalcaptor.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import br.com.arivanbastos.jillcore.models.signal.GeoMagneticSignal;
import br.com.arivanbastos.signalcaptor.BaseActivity;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.GeoMagneticSignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.signalcaptor.sensors.exceptions.SensorNotSupportedException;

public class GeoMagneticSensor extends BaseSensor
        implements SensorEventListener
{
    private SensorManager mSensorManager;

    public void init() throws SensorNotSupportedException
    {
        super.init();

        mSensorManager = (SensorManager) BaseActivity.currentActivity.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor==null)
            throw new SensorNotSupportedException("Can't find magnetometer!");

        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);

        addSource(getSignalTypeId(), getSignalTypeId());
    }

    public String getSignalTypeId()
    {
        return GeoMagneticSignal.TYPE;
    }

    public void beginRecord()
    {
        List<String> listenedSources = new ArrayList<String>();
        listenedSources.add(getSignalTypeId());
        super.beginRecord(listenedSources);
    }

    @Override
    public SignalDataSet instantiateDataSetForSource(String signalSourceId) {
        return new GeoMagneticSignalDataSet(signalSourceId);
    }

    public void onSampleReceived(String signalSourceId, Object signalValue)
    {
        double mag = (double)signalValue;

        // Calls parent to register sample data.
        super.onSampleReceived(getSignalTypeId(), new DoubleSignalSample(mag));
    }

    public void finalize()
    {
        super.finalize();

        if (inited)
            mSensorManager.unregisterListener(this);
    }

    // ----------------------------------

    /**
     * SensorEventListener interface implementation.
     */

    /**
     *
     * @param sensor
     * @param accuracy
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    /**
     * Receives magnetic sensor data.
     * @param event
     */
    public void onSensorChanged(SensorEvent event)
    {
        if (!running) return ;

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            float[] mag =  (float[])event.values;
            onSampleReceived(getSignalTypeId(), Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1] + mag[2] * mag[2]));
        }
    }
}
