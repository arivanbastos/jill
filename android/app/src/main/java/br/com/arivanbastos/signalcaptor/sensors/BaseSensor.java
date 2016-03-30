package br.com.arivanbastos.signalcaptor.sensors;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.arivanbastos.jillcore.models.signal.SignalSource;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.signalcaptor.sensors.events.ISensorManagerListener;
import br.com.arivanbastos.signalcaptor.sensors.exceptions.SensorNotSupportedException;

public abstract class BaseSensor {

    // Wheter record process is running or not.
    protected boolean running = false;

    // How many samples does we want?
    protected int maximumSamplesCount = 0;

    // Datasets of each source.
    private HashMap<String, SignalDataSet> dataSets;

    // List of ALL signal sources.
    protected List<SignalSource> sources;

    // List of ids of LISTENED signal sources.
    // Only signal of this sources will be stored and
    // dispatched at endRecord event.
    protected List<String> listenedSources;

    // List of ids of LISTENED signal source
    // that reacord is done.
    protected List<String> doneListenedSources;

    // Listeners to SensorManager events.
    protected ArrayList<ISensorManagerListener> listeners;

    // Was the sensor inited?
    protected boolean inited;

    // ------------------------------------------------------------

    /**
     * List of available location methods.
     */
    private static List<Class> methods = new ArrayList<Class>();

    public static void register(Class c)
    {
        methods.add(c);
    }

    static {
        register(BLESensor.class);
        register(GeoMagneticSensor.class);
        register(WifiSensor.class);
    }

    public static List<BaseSensor> getRegisteredSensors()
    {
        Log.i("BaseSensor", "getRegisteredSensors");
        ArrayList<BaseSensor> result = new ArrayList<BaseSensor>();

        for (Class c : methods)
        {
            try {
                BaseSensor sensor = (BaseSensor) c.newInstance();
                result.add(sensor);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    // ------------------------------------------------------------

    public BaseSensor()
    {
        listeners   = new ArrayList<ISensorManagerListener>();
        sources     = new ArrayList<SignalSource>();
        dataSets    = new HashMap<String, SignalDataSet>();
    }

    public void init() throws SensorNotSupportedException
    {
        inited = true;
    }

    // Returns the signal type of sensor (ie: magnetic, ble, wifi, etc).
    // Should be unique for each sensor type.
    public abstract String getSignalTypeId();
    public abstract SignalDataSet instantiateDataSetForSource(String signalSourceId);

    // -----------------------------------------------------------

    /**
     * Sources.
     * A source represents a single signal emitter, for example,
     * a wifi modem or a bluetooth beacon.
     *
     * When the application needs to display signal sources it can
     * call getSources(). The childs class should implement the logic
     * to capture the sources, using device sensors.
     *
     * Some signal types, such magnetic, will have just one source.
     *
     * @see br.com.arivanbastos.signalcaptor.sensors.BLESensor
     */

    public void addSource(String label, String id)
    {
        for (SignalSource s : sources)
            if (s.getId().equals(id))
                return ;

        SignalSource signalSource = new SignalSource(label, id, getSignalTypeId());
        sources.add(signalSource);

        for (ISensorManagerListener listener : listeners)
            listener.onSourceAvailable(signalSource);
    }

    public void removeSource(String id)
    {
        List<SignalSource> toRemove = new ArrayList<SignalSource>();
        for (SignalSource signalSource : sources) {
            if (signalSource.getId().equals(id)) {
                //sources.remove(signalSource);
                toRemove.add(signalSource);

                for (ISensorManagerListener listener : listeners)
                    listener.onSourceUnavailable(signalSource);
            }
        }

        sources.removeAll(toRemove);
    }

    public List<SignalSource> getSources() {
        return sources;
    }

    protected SignalSource getSource(String signalSourceId)
    {
        for (SignalSource source : sources)
        {
            if (source.getId().equals(signalSourceId))
                return source;
        }
        return null;
    }

    protected SignalDataSet getSourceDataSet(String signalSourceId)
    {
        SignalDataSet dataSet = dataSets.get(signalSourceId);
        if (dataSet==null)
        {
            dataSet = instantiateDataSetForSource(signalSourceId);
            dataSets.put(signalSourceId, dataSet);
        }

        return dataSet;
    }

    // ---------------------------------------------------------------

    /**
     * Records signal from the listed sources.
     * Each source may represent, ie, a widfi modem or bluetooth beacon.
     * @param listenedSources
     */

    public void beginRecord(List<String> listenedSources, int maximumSamplesCount)
    {
        this.maximumSamplesCount = maximumSamplesCount;
        beginRecord(listenedSources);
    }

    /**
     * Gathers data from ALL sources.
     */
    public void beginRecord()
    {
        beginRecord(null);
    }

    public void beginRecord(List<String> listenedSources)
    {
        this.listenedSources = listenedSources;
        this.dataSets        = new HashMap<String, SignalDataSet>();
        this.doneListenedSources = new ArrayList<String>();

        for (ISensorManagerListener listener : listeners)
            listener.onRecordBegin();

        running = true;
    }

    public abstract void onSampleReceived(String signalSourceId, Object value);

    public void onSampleReceived(String signalSourceId, SignalSample value)
    {
        if (!running)
            return;

        // Should the source be listened?
        if ((listenedSources==null || listenedSources.contains(signalSourceId)) &&
            (!doneListenedSources.contains(signalSourceId)))
        {
            SignalDataSet dataSet = getSourceDataSet(signalSourceId);

            // Adds the signal to source dataset.
            dataSet.addSample(value);

            // Fire progress event.
            for (ISensorManagerListener listener : listeners)
                listener.onRecordProgress(getSource(signalSourceId), value, dataSet);

            boolean reachLimit = (maximumSamplesCount > 0 && dataSet.getSamples().size() >= maximumSamplesCount);

            // Have we reached the samples limit?
            if (reachLimit)
            {
                // Record done.
                doneListenedSources.add(signalSourceId);

                // Notify listeners that source have done capturing
                // signals.
                for (ISensorManagerListener listener : listeners)
                    listener.onSingleSourceRecordEnd(getSource(signalSourceId), dataSet);

                if (listenedSources!=null)
                {
                    // Checks whether all sources have done.
                    if (doneListenedSources.size()==listenedSources.size())
                        endRecord();
                }
            }
        }
    }

    /**
     * Terminates record proccess.
     */
    public void endRecord()
    {
        if(running) {
            running = false;

            List<SignalDataSet> dataSetsList = new ArrayList<SignalDataSet>(dataSets.values());

            for (ISensorManagerListener listener : listeners)
                listener.onAllSourcesRecordEnd(getSignalTypeId(), dataSetsList);
        }
    }

    public void finalize()
    {
        listeners = new ArrayList<ISensorManagerListener>();
    }

    /**
     * Observer design pattern.
     * @param listener
     */
    public void registerListener(ISensorManagerListener listener)
    {
        listeners.add(listener);
    }

    // -------------------------------------------------------------


    public int getMaximumSamplesCount() {
        return maximumSamplesCount;
    }

    public void setMaximumSamplesCount(int maximumSamplesCount) {
        this.maximumSamplesCount = maximumSamplesCount;
    }
}
