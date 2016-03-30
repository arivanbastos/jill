package br.com.arivanbastos.signalcaptor.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import br.com.arivanbastos.jillcore.models.signal.WiFiSignal;
import br.com.arivanbastos.jillcore.models.signal.datasets.WiFiSignalDataSet;
import br.com.arivanbastos.signalcaptor.BaseActivity;
import br.com.arivanbastos.jillcore.models.signal.datasets.BLESignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.signalcaptor.sensors.exceptions.SensorNotSupportedException;

public class WifiSensor extends BaseSensor {

    // Time in seconds until an wifi be considered unseen.
    private static final int VISIBILITY_LIMIT = 10;

    private static WifiInternalSensor internalSensor =null;

    // sourcesSeenTime stores the time a sensor is seen. If the sensor
    // is not seen by a long time then it is removed from sources list.
    private HashMap<String, Date> sourcesSeenTime = new HashMap<String, Date>();

    public void init() throws SensorNotSupportedException
    {
        super.init();

        System.err.println("WiFiSensor::init()");
        if (internalSensor ==null)
            internalSensor = new WifiInternalSensor();

        internalSensor.registerListener(this);
    }

    public String getSignalTypeId()
    {
        return WiFiSignal.TYPE;
    }


    // -------------------------------------------------

    //public void onSignalReceived(String name, String mac, int rssi)
    public void onSampleReceived(String signalSourceId, Object value)
    {
        String name     = signalSourceId;
        DoubleSignalSample sample = null;

        if (value != null) {
            int i = (int) value;
            double rssi = (double) i;
            sample = new DoubleSignalSample(rssi);
        }

        // Devices list (sources).
        addSource(name, name);
        sourcesSeenTime.put(name, new Date());
        removeUnseenSources();

        // Calls parent to register sample data.
        super.onSampleReceived(name, sample);
    }

    private void removeUnseenSources()
    {
        Object ids[] = sourcesSeenTime.keySet().toArray();
        for (Object oid : ids)
        {
            String id = (String)oid;
            Date lastSeenTime = sourcesSeenTime.get(id);
            double timeInSeconds = ((new Date()).getTime()-lastSeenTime.getTime())/1000;

            if (timeInSeconds > VISIBILITY_LIMIT)
            {
                removeSource(id);
                sourcesSeenTime.remove(id);
            }
        }
    }


    // ------------------------------------------

    /**
     * Record proccess.
     */

    @Override
    public SignalDataSet instantiateDataSetForSource(String signalSourceId) {
        return new WiFiSignalDataSet(signalSourceId);
    }

    public void finalize()
    {
        super.finalize();

        if (inited)
            internalSensor.unregisterListener(this);
    }

    // -------------------------------------------

    /**
     * Encapsulates Android BLE sensor logic, allowing register more then
     * 1 ble signal listener.
     */
    private class WifiInternalSensor extends InternalSensor
    {
        private List<WifiSensor> listeners;

        public WifiInternalSensor() throws SensorNotSupportedException
        {
            // Reads buffer each 3000ms.
            super(MODE_FIXED_INTERVAL, 3000);

            System.err.println("WifiInternalSensor()");

            listeners = new ArrayList<WifiSensor>();

            final WifiManager wifi = (WifiManager) BaseActivity.currentActivity.getSystemService(Context.WIFI_SERVICE);
            if (wifi.isWifiEnabled() == false)
                wifi.setWifiEnabled(true);

            wifi.startScan();
        }

        public void onTimer()
        {
            final WifiManager wifi = (WifiManager) BaseActivity.currentActivity.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> results = wifi.getScanResults();
            for (ScanResult s : results) {
                //System.err.println(s.SSID+" - "+s.BSSID + ": " + s.level +", "+ s.frequency);
                onSampleReceived(s.SSID, s.BSSID, s.level);
            }

            super.onTimer();

            wifi.startScan();
        }

        public void onSampleReceived(String name, String mac, int rssi)
        {
            name = name + " " + mac.substring(9);
            super.onSampleReceived(name, rssi);
        }
    }
}