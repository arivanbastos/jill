package br.com.arivanbastos.signalcaptor.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class InternalSensor {

    // The signals are read from fixed intervals,
    // independent if the sensor see the signal.
    // Uses a buffer system to reach this behavior.
    public static final int MODE_FIXED_INTERVAL  = 1;

    // The signals are read when the sensor reads
    // the signal.
    public static final int MODE_NO_INTERVAL = 2;

    protected HashMap<String, Object> signalBuffer;

    private List<BaseSensor> listeners;

    protected int mode = 1;

    protected int interval;

    protected Timer timer;

    public InternalSensor(int mode, int interval)
    {
        this.signalBuffer   = new HashMap<String, Object>();
        this.listeners      = new ArrayList<>();
        this.mode           = mode;

        if (mode==MODE_FIXED_INTERVAL)
        {
            this.interval = interval;

            final InternalSensor thiss = this;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    thiss.onTimer();
                }
            }, interval, interval);
        }
    }

    public void init()
    {
        this.signalBuffer   = new HashMap<String, Object>();
    }

    public void onTimer()
    {
        for (String id : signalBuffer.keySet())
        {
            Object signalValue = read(id);
            for (BaseSensor listener : listeners)
                listener.onSampleReceived(id, signalValue);
        }
    }

    public void set(String signalSourceId, Object value)
    {
        signalBuffer.put(signalSourceId, value);
    }

    public Object read(String signalSourceId)
    {
        Object result = null;
        if (signalBuffer.containsKey(signalSourceId)) {
            result = signalBuffer.get(signalSourceId);

            // Clears buffer content.
            signalBuffer.put(signalSourceId, null);
        }

        return result;
    }

    public void onSampleReceived(String id, Object sampleValue)
    {
        if (mode == MODE_FIXED_INTERVAL)
        {
            set(id, sampleValue);
        }
        else {
            for (BaseSensor listener : listeners)
                listener.onSampleReceived(id, sampleValue);
        }
    }

    public void registerListener(BaseSensor listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void unregisterListener(BaseSensor listener)
    {
        listeners.remove(listener);
    }
}
