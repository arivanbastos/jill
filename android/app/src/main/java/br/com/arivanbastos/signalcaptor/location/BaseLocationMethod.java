package br.com.arivanbastos.signalcaptor.location;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import br.com.arivanbastos.signalcaptor.location.events.ILocationMethodListener;
import br.com.arivanbastos.signalcaptor.location.exceptions.InvalidParameterValueException;
import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.math.Point2;

public abstract class BaseLocationMethod {

    private static int idCounter = 1;

    // Canvas where debug visual data can be ploted.
    protected LocationMethodCanvas canvas = new FakeCanvas();

    // Listeners to LocationMethod events.
    protected ArrayList<ILocationMethodListener> listeners;

    private long startTime      = 0;
    private long runningTime    = 0;

    private boolean running;

    // A unique ID for each class instance.
    private int id;

    protected Map map;

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
        register(SimpleMagneticLocationMethod.class);
        register(GeoNLocationMethod.class);
        register(MGeoNLocationMethod.class);
    }

    public static List<BaseLocationMethod> getRegisteredMethods()
    {
        Log.i("BaseLocationMethod", "getRegisteredMethods");
        ArrayList<BaseLocationMethod> result = new ArrayList<BaseLocationMethod>();

        for (Class c : methods)
        {
            try {
                BaseLocationMethod method = (BaseLocationMethod) c.newInstance();
                Log.i("BaseLocationMethod", " adding "+method.getName());
                result.add(method);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    // ----------------------------------------------------

    public BaseLocationMethod() {
        id = idCounter++;
        this.listeners = new  ArrayList<ILocationMethodListener>();
    }

    public BaseLocationMethod(Map map) {
        this();
        this.map = map;
    }


    /**
     * The method name.
     */
    public abstract String getName();

    /**
     * A String describing the method.
     * @return
     */
    public abstract String getDescription();

    /**
     * List the parameters for this location method.
     * The paramters are displayed in interface for user
     * configuration;
     */
    public abstract List<LocationMethodParameter> getParameters();

    /**
     * Sets a parameter value
     */
    public abstract void setParameterValue(String parameterName, String value)
        throws InvalidParameterValueException;

    /**
     * Returns a parameter value.
     * @param parameterName
     * @return
     */
    public abstract String getParameterValue(String parameterName);

    public String getParametersDescription()
    {
        String result = "";
        for (LocationMethodParameter parameter : getParameters())
        {
            result += parameter.getName()+"="+getParameterValue(parameter.getName())+"; ";
        }

        return result;
    }

    /**
     * Initializes location process, for example initializing sensors.
     */
    public void init() throws Exception
    {
    }

    /**
     * Starts location process.
     */
    public void startLocation()
    {
        startTime   = Calendar.getInstance().getTimeInMillis();
        running     = true;
    }

    /**
     * Stops location process.
     */
    public void stopLocation()
    {
        Log.i("BaseLocationMethod", "stopLocation()");
        runningTime = Calendar.getInstance().getTimeInMillis() - startTime;
        running     = false;
    }

    protected void onLocationEnd(Point2.Double currentPoint)
    {
        stopLocation();

        for (ILocationMethodListener listener : listeners)
            listener.onLocationInfoAvailable(this, currentPoint);

    }

    public void finalize()
    {
        running = false;
    }

    // -----------------------------------------------

    /**
     * Observer design pattern.
     * @param listener
     */
    public void registerListener(ILocationMethodListener listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeAllListeners()
    {
        listeners = new  ArrayList<ILocationMethodListener>();
    }

    public long getRunningTime() {
        if (running)
            runningTime = Calendar.getInstance().getTimeInMillis() - startTime;

        return runningTime;
    }

    public boolean isRunning() {
        return running;
    }

    public int getId() {
        return id;
    }

    public LocationMethodCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(LocationMethodCanvas canvas) {
        this.canvas = canvas;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    // ------------------------------------------------

    public void debug(String text)
    {
        String name = getName();
        for (ILocationMethodListener listener : listeners)
            listener.onDebugInfoAvailable(name, text, getColor(name+"_"+id));
    }

    public int getColor()
    {
        String color = getColor(getName()+"_"+id);
        return Color.parseColor(color);
    }

    /**
     * Debug.
     */

    private static String[] colours = {
            "#FFFF00", "#00FF00", "#00CCCC", "#FFCC00", "#FF99CC", "#9999FF", "#FF2A00", "#999999"
    };
    private static HashMap<String, String> contextCoulors=new HashMap<String, String>();
    private static int colorIndex = 0;

    private String getColor(String context)
    {
        if (!contextCoulors.containsKey(context)) {
            String color = colours[colorIndex];
            contextCoulors.put(context, color);

            colorIndex++;
            if (colorIndex>=colours.length)
                colorIndex = 0;
        }

        return contextCoulors.get(context);
    }

    private class FakeCanvas implements LocationMethodCanvas
    {
        @Override
        public void drawnCircle(float x, float y, float radius, int color) {

        }
    }
}
