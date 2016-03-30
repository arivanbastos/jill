package br.com.arivanbastos.signalcaptor.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import br.com.arivanbastos.jillcore.models.signal.BLESignal;
import br.com.arivanbastos.signalcaptor.BaseActivity;
import br.com.arivanbastos.jillcore.models.signal.datasets.BLESignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.signalcaptor.sensors.exceptions.SensorNotSupportedException;

public class BLESensor extends BaseSensor {

    // The sensor tries to read new signal data
    // each 500ms.
    private static final int READ_SIGNAL_INTERVAL = 500;

    // Time in seconds until an BLE beacon be considered unseen.
    private static final int VISIBILITY_LIMIT = 10;

    private static final int REQUEST_ENABLE_BT = 101;

    private static BLEInternalSensor internalSensor =null;

    // sourcesSeenTime stores the time a sensor is seen. If the sensor
    // is not seen by a long time then it is removed from sources list.
    private HashMap<String, Date> sourcesSeenTime = new HashMap<String, Date>();

    public void init() throws SensorNotSupportedException
    {
        super.init();

        if (internalSensor ==null)
            internalSensor = new BLEInternalSensor();

        internalSensor.init();
        internalSensor.registerListener(this);
    }

    public String getSignalTypeId()
    {
        return BLESignal.TYPE;
    }


    // -------------------------------------------------

    //public void onSignalReceived(String beaconMac, int rssi)
    public void onSampleReceived(String signalSourceId, Object value)
    {
        String mac    = signalSourceId;
        DoubleSignalSample sample = null;

        if (value != null) {
            int i = (int) value;
            double rssi = (double) i;
            sample = new DoubleSignalSample(rssi);
        }

        // Devices list (sources).
        addSource("BLE "+mac, mac);
        sourcesSeenTime.put(mac, new Date());
        removeUnseenSources();

        // Calls parent to register sample data.
        super.onSampleReceived(mac, sample);
    }

    private void removeUnseenSources()
    {
        for (String id : sourcesSeenTime.keySet())
        {
            Date lastSeenTime = sourcesSeenTime.get(id);
            double timeInSeconds = ((new Date()).getTime()-lastSeenTime.getTime())/1000;

            // More then 3 seconds the beacon is not seen.
            if (timeInSeconds>VISIBILITY_LIMIT)
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
        return new BLESignalDataSet(signalSourceId);
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
    private class BLEInternalSensor extends InternalSensor
    {
        private BluetoothAdapter mBluetoothAdapter;
        private BLESensorScanCallback scanCallback;

        public BLEInternalSensor() throws SensorNotSupportedException
        {
            // Reads buffer each 500ms.
            super(MODE_FIXED_INTERVAL, 500);

            // Use this check to determine whether BLE is supported on the device.  Then you can
            // selectively disable BLE-related features.
            if (!BaseActivity.currentActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
                throw new SensorNotSupportedException("BLE not supported!");

            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) BaseActivity.currentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null)
                throw new SensorNotSupportedException("Bluetooth not supported!");

            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            if (!mBluetoothAdapter.isEnabled()) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    BaseActivity.currentActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }

            scanCallback = new BLESensorScanCallback(this);
            scanLeDevice(true);
        }

        private void scanLeDevice(final boolean enable)
        {
            if (enable) {
                mBluetoothAdapter.startLeScan(scanCallback);
            } else {
                mBluetoothAdapter.stopLeScan(scanCallback);
            }
        }
    }

    private class BLESensorScanCallback implements BluetoothAdapter.LeScanCallback
    {
        private BLEInternalSensor sensor;
        public BLESensorScanCallback(BLEInternalSensor sensor)  { this.sensor = sensor; }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            //sensor.onSampleReceived(device, rssi, scanRecord);
            sensor.onSampleReceived(device.getAddress(), rssi);
        }
    }
}