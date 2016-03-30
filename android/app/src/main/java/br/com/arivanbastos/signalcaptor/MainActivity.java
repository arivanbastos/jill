package br.com.arivanbastos.signalcaptor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.com.arivanbastos.jillcore.models.map.MapObject;
import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.map.MapPoint;
import br.com.arivanbastos.jillcore.models.serialize.MapSerializer;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.utils.MathUtil;

import br.com.arivanbastos.signalcaptor.location.BaseLocationMethod;
import br.com.arivanbastos.signalcaptor.location.LocationMethodParameter;
import br.com.arivanbastos.signalcaptor.location.events.ILocationMethodListener;
import br.com.arivanbastos.signalcaptor.sensors.WifiSensor;
import br.com.arivanbastos.signalcaptor.views.AddMethodDialog;
import br.com.arivanbastos.signalcaptor.views.CreateObjectDialog;
import br.com.arivanbastos.signalcaptor.views.CreatePointDialog;
import br.com.arivanbastos.signalcaptor.views.IMapViewListener;
import br.com.arivanbastos.signalcaptor.views.MapView;
import br.com.arivanbastos.signalcaptor.views.SaveMapDialog;
import br.com.arivanbastos.signalcaptor.views.StartTrackingDialog;
import thinkti.android.filechooser.FileChooser;

public class MainActivity extends BaseActivity implements
        IMapViewListener, ILocationMethodListener
{
    final int FILE_CHOOSER = 1;

    // Map model.
    public static Map map=null;

    // Map drawner.
    private MapView mapView = null;

    // Map data folder.
    private File dataFolder;

    // Loaded file.
    private static String fileName = "new file";


    // Tracking proccess.
    // Stores selected Location Methods and their results.
    private static List<BaseLocationMethod> methods;

    private MethodsListAdapter methodsListAdapter;

    private SharedPreferences sharedPref;

    private TextView debugTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //restoreState(savedInstanceState);
        root = this;

        // Creates the data folder, used to store map files.
        dataFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SignalCaptor");
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                Toast.makeText(this, R.string.error_data_folder, Toast.LENGTH_LONG).show();
            }
        }

        if (methods==null)
            methods = new ArrayList<BaseLocationMethod>();
        else
        {
            for (BaseLocationMethod method : methods)
                method.registerListener(this);
        }

        // Enviroment map.
        mapView = (MapView)findViewById(R.id.mapView);
        mapView.setListener(this);

        // Methods.
        methodsListAdapter = new MethodsListAdapter(methods, this);
        ListView methodsListView = (ListView)findViewById(R.id.methodsListView);
        methodsListView.setAdapter(methodsListAdapter);

        debugTextView = (TextView)findViewById(R.id.debugText);
        debugTextView.setMovementMethod(new ScrollingMovementMethod());

        // Is the first load? Automatically opens a "default.json" map.
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (map==null) {
            String lastOpenedMap = sharedPref.getString("lastOpenedMap", "/sdcard/Documents/SignalCaptor/default.json");
            openFile(lastOpenedMap);
        }
    }

    // http://eigo.co.uk/labs/managing-state-in-an-android-activity/
    /*
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i("MainActivity", "onSaveInstanceState");
        savedInstanceState.putInt("mapGridType", mapView.getGridType());

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        restoreState(savedInstanceState);
    }

    private void restoreState(Bundle savedInstanceState)
    {
        if (savedInstanceState!=null)
        {
            Log.i("MainActivity", "onRestoreInstanceState");
            int mapGridType = savedInstanceState.getInt("mapGridType");
            if (mapGridType == MapView.GRID_TYPE_LOCAL)
                findViewById(R.id.localType).performClick();
        }
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_openmap) {
            onOpenMapTap(null);
        }
        else if (id == R.id.action_savemap) {
            onSaveMapTap(null);
        }
        else if (id == R.id.action_savemap_as) {
            onSaveMapAsTap(null);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();

        updateTitle();
        initMap();

        try {
            (new WifiSensor()).init();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------

    protected void updateTitle()
    {
        setTitle("SignalCaptor ("+fileName+")");
    }

    protected void onPause() {

        super.onPause();
    }

    protected void onStop()
    {
        Log.i("MainActivity", "onStop");
        super.onStop();

        for (BaseLocationMethod method : methods)
            method.removeAllListeners();

        if(startTrackingDialog!= null)
            startTrackingDialog.dismiss();

        if (addMethodDialog!=null)
            addMethodDialog.dismiss();
    }

    // ----------------------------------------------------

    public void initMap()
    {
        if (map ==null)
            return;

        //Log.i("MainActivity", "initMap(): " + map.toString());
        mapView.setMap(map);
        drawnMap();
    }

    public void saveMap(String fileName)
    {
        try {
            MapSerializer s = new MapSerializer();
            String json = s.serialize(map);

            String path = dataFolder.getAbsolutePath();
            File file = new File(path + "/" + fileName + ".json");

            FileOutputStream stream = new FileOutputStream(file);
            stream.write(json.getBytes());
            stream.close();

            this.fileName = fileName;
            updateTitle();

            Toast.makeText(this, "File "+fileName+" was saved successfully.", Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Toast.makeText(this, "Error saving file: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onGridTypeRadioButtonClicked(View v)
    {
        if (v.getId() == R.id.globalType)
            mapView.setGridType(MapView.GRID_TYPE_GLOBAL);
        else
            mapView.setGridType(MapView.GRID_TYPE_LOCAL);

        drawnMap();
    }

    // ----------------------------------------------------

    /**
     * Map load/save.
     */

    public void onOpenMapTap(View v)
    {
        Intent intent = new Intent(this, FileChooser.class);
        ArrayList<String> extensions = new ArrayList<String>();
        extensions.add(".json");
        intent.putStringArrayListExtra("filterFileExtension", extensions);
        intent.putExtra("currentDir", dataFolder.getAbsolutePath());
        startActivityForResult(intent, FILE_CHOOSER);
    }

    /**
     * FileChooser interface response.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((requestCode == FILE_CHOOSER) && (resultCode == -1)) {
            String fileSelected = data.getStringExtra("fileSelected");
            Log.i("MainActivity", "onActivityResult() map file: "+ fileSelected);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("lastOpenedMap", fileSelected);
            editor.commit();

            openFile(fileSelected);
        }
    }

    /**
     * Load map from file.
     * @param path
     */
    public void openFile(String path)
    {
        File f = new File(path);
        try {
            //map.loadMap(fileSelected);
            FileInputStream is  = new FileInputStream(f);
            MapSerializer s     = new MapSerializer();
            map                 = s.unserialize(is);
            fileName            = f.getName().replaceFirst("[.][^.]+$", "");
            updateTitle();

            initMap();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Error loading file: "+e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void onSaveMapTap(View v)
    {
        saveMap(fileName);
    }

    public void onSaveMapAsTap(View v)
    {
        SaveMapDialog saveMapDialog = new SaveMapDialog(this);
        saveMapDialog.show();
    }

    // ----------------------------------------------------
    /**
     * MapView events.
     */

    /**
     *
     * @param p
     */
    public void onMapPointTap(MapPoint p)
    {
        editPoint(p);
    }

    // -----------------------------------------------------------

    /**
     * New Point Dialog.
     */

    /**
     * Displays a X,Y popup.
     * @param v
     */
    public void onNewPointTap(View v)
    {
        CreatePointDialog dialog = new CreatePointDialog(this);
        dialog.show();
    }

    /**
     * Called after the user fill coordinates in CreatePointDialog and
     * tap "ok".
     * @param p
     * @param roomId
     * @throws Exception
     */
    public void createPoint(Point2.Double p, String roomId) throws Exception
    {
        // Converts from local coordinates to global coordinates.
        if (roomId!=null)
            p = map.localToGlobal(p, roomId);

        MapPoint mapPoint = new MapPoint((float)p.getX(), (float)p.getY());
        map.addPoint(mapPoint);
        editPoint(mapPoint);
    }

    public void editPoint(MapPoint p)
    {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("x", p.getX());
        parameters.put("y", p.getY());
        startActivity(NewPointActivity.class, parameters);
    }

    // -----------------------------------------------

    /**
     * New Object Dialog.
     */

    /**
     * Displays new object popup.
     * @param v
     */
    public void onNewObjectTap(View v)
    {
        CreateObjectDialog dialog = new CreateObjectDialog(this);
        dialog.show();
    }

    /**
     * Called after the user fill info in CreateObjectDialog and
     * tap "ok".
     * @param x
     * @param y
     * @throws Exception
     */
    public void saveObject(float x, float y, float z, String id, String roomId)
    {
        Point2.Double p = new Point2.Double(x,y);
        if (roomId!=null)
            p = map.localToGlobal(x, y, roomId);

        //Point3F point = new Point3F(p.getX(), p.getY(), z);
        map.addObject(new MapObject(id, p));

        drawnMap();
    }

    /**
     * Called when user taps a object in map view.
     * @param object
     */
    public void onObjectTap(MapObject object)
    {
        CreateObjectDialog dialog = new CreateObjectDialog(this);
        dialog.setObject(object);
        dialog.show();
    }

    /**
     * Called when user taps "Remove" button in new object dialog.
     * @param id
     */
    public void removeObject(String id)
    {
        map.removeObjectById(id);

        drawnMap();
    }

    // ------------------------------------------------

    /**
     * Methods.
     */

    AddMethodDialog addMethodDialog = null;

    /**
     * Opens a Add Method dialog.
     * @param v
     */
    public void onAddMethodTap(View v)
    {
        if (addMethodDialog==null)
            addMethodDialog = new AddMethodDialog(this);

        addMethodDialog.show();
    }

    /**
     * Called after the user fill method parameters in AddMethodDialog and
     * hits "ok"
     * @param method
     */
    public void addLocationMethod(BaseLocationMethod method)
    {
        method.setMap(map);
        method.registerListener(this);
        method.setCanvas(mapView);

        methods.add(method);
        methodsListAdapter.notifyDataSetChanged();
    }

    /**
     * Removes a method.
     * @param v
     */
    public void onRemoveMethodTap(View v)
    {

    }

    // ------------------------------------------------

    /**
     * Tracking process.
     */

    private static HashMap<Integer, Point2.Double> results = new HashMap<Integer, Point2.Double>();
    private HashMap<Integer, Float> average;
    private HashMap<Integer, Float> min;
    private HashMap<Integer, Float> max;
    private HashMap<Integer, Integer> count;
    private HashMap<Integer, Integer> above1meters;
    private HashMap<Integer, Integer> above2meters;
    private HashMap<Integer, Integer> above3meters;

    // The actual user position.
    private Point2.Double actualPosition;

    // How many tracking results should be collected.
    private int samplesCount;

    // Tracking results in textual form.
    // This text is saved to file.
    private String trackingLog;

    // Start tracking dialog.
    private StartTrackingDialog startTrackingDialog=null;

    /**
     * Tap on "Start Tracking" button.
     * @param v
     */
    public void onStartTrackingTap(View v)
    {
        if (startTrackingDialog==null)
            startTrackingDialog = new StartTrackingDialog(this);

        startTrackingDialog.show();
    }

    /**
     * Tap on "Start" button on StartTrackingDialog.
     * @param actualPosition
     * @param samplesCount
     * @param roomId
     */
    public void startTracking(Point2.Double actualPosition, int samplesCount, String roomId)
    {
        // Converts from local coordinates to global coordinates.
        if (roomId!=null)
            actualPosition = map.localToGlobal(actualPosition, roomId);

        this.samplesCount   = samplesCount;
        this.actualPosition = actualPosition;

        debugTextView.setText("");

        DateFormat format = DateFormat.getDateTimeInstance();
        trackingLog    = "Room: "+roomId+"\n"+
                         "Actual position: "+formatPoint(actualPosition)+"\n"+
                         "Date: "+format.format(new Date())+"\n"+
                         "Samples Count: "+samplesCount+"\n";

        for (BaseLocationMethod method : methods)
            trackingLog += "Method "+method.getId()+": "+method.getName() + "("+method.getParametersDescription()+")\n";

        trackingLog += "-----------------------\n";

        trackingLog += "-\t";
        for (BaseLocationMethod method : methods)
            trackingLog += method.getName()+" ("+method.getId()+")\t";

        trackingLog += "\n";

        average = new HashMap<Integer, Float>();
        min     = new HashMap<Integer, Float>();
        max     = new HashMap<Integer, Float>();
        count   = new HashMap<Integer, Integer>();
        above1meters = new HashMap<Integer, Integer>();
        above2meters = new HashMap<Integer, Integer>();
        above3meters = new HashMap<Integer, Integer>();

        doTracking(methods);
    }

    /**
     * Start the tracking process for each method present in
     * methodds.
     * @param methods
     */
    public void doTracking(List<BaseLocationMethod> methods)
    {
        Log.i("MainActivity", "doTracking");
        mapView.clearLocationPoints();
        mapView.clearMethodsCanvas();

        // Disables "Start Tracking" button.
        // It will be enabled again at end of tracking process.
        if (methods.size()==0)
            return;

        // Stores tracked position for each method.
        results = new HashMap<Integer, Point2.Double>();

        findViewById(R.id.startTrackingButton).setEnabled(false);

        for (BaseLocationMethod method : methods)
        {
            try {
                method.init();
                method.startLocation();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Cant init method "+method.getName()+": "+e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        methodsListAdapter.notifyDataSetChanged();
    }

    /**
     * Called by a method when a tracking debug information is available.
     * @param methodName
     * @param message
     * @param color
     */
    @Override
    public void onDebugInfoAvailable(final String methodName, final String message, final String color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                debugTextView.append(Html.fromHtml("<font color=" + color + " >" + methodName + "</font> " + message + "<br/>"));
            }
        });
    }

    /**
     * Called bu a method when a single tracking step is done.
     * @param method
     * @param trackedPosition
     */
    @Override
    public void onLocationInfoAvailable(final BaseLocationMethod method, final Point2.Double trackedPosition) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            Log.i("MainActivity", "Tracked position: "+trackedPosition);

            if (trackedPosition!=null)
            {
                Point2.Double point = new Point2.Double(trackedPosition.getX(), trackedPosition.getY());
                mapView.addLocationPoint(point, Color.RED);
            }

            results.put(method.getId(), trackedPosition);

            methodsListAdapter.notifyDataSetChanged();

            // Updates map.
            drawnMap();

            // Enables "Start Tracking
            // button if there is no method running.
            Log.i("MainActivity", results.size() + "==" + methods.size());
            if (results.size() == methods.size()) {

                addResultToTrackinLog();

                // Update samples count.
                samplesCount--;

                if (samplesCount<=0) {
                    // All samples collected.
                    endTracking();
                    beep();
                }
                else
                {
                    // There are samples to be collected.
                    doTracking(methods);
                }
            }
            }
        });
    }

    private void addResultToTrackinLog()
    {
        trackingLog +="\t";
        for (BaseLocationMethod method : methods)
        {
            int id = method.getId();
            Point2.Double trackedPosition = results.get(method.getId());
            System.err.println("addResultToTrackinLog: "+trackedPosition + " - "+actualPosition);

            String error = null;
            if (trackedPosition!=null)
            {
                float distance = MathUtil.distance(trackedPosition, actualPosition);

                if (distance<1)
                    above1meters.put(id, above1meters.containsKey(id)?above1meters.get(id)+1:1);
                if (distance<2)
                    above2meters.put(id, above2meters.containsKey(id)?above2meters.get(id)+1:1);
                if (distance<3)
                    above3meters.put(id, above3meters.containsKey(id)?above3meters.get(id)+1:1);

                count.put(id, count.containsKey(id)?count.get(id)+1:1);
                average.put(id, average.containsKey(id)?(average.get(id)+distance)/2:distance);
                min.put(id, min.containsKey(id)?Math.min(min.get(id), distance):distance);
                max.put(id, max.containsKey(id)?Math.max(max.get(id), distance):distance);

                error = distance+"";
            }

            trackingLog += error+"\t";
        }

        trackingLog += "\n";
    }

    /**
     * Called when the whole tracking process is done
     * (when all samples from all methods are collected).
     */
    public void endTracking()
    {
        Log.i("MainActivity", "endTracking()");

        for (BaseLocationMethod method : methods)
            method.finalize();

        findViewById(R.id.startTrackingButton).setEnabled(true);
        findViewById(R.id.startTrackingButton).setEnabled(true);
        findViewById(R.id.startTrackingButton).setEnabled(true);
        findViewById(R.id.startTrackingButton).setEnabled(true);

        trackingLog += "Average:\t";
        for (BaseLocationMethod method : methods)
            trackingLog += (average.containsKey(method.getId())?average.get(method.getId()):"-")+"\t";
        trackingLog += "\n";

        trackingLog += "Min:\t";
        for (BaseLocationMethod method : methods)
            trackingLog += (min.containsKey(method.getId())?min.get(method.getId()):"-")+"\t";
        trackingLog += "\n";

        trackingLog += "Max:\t";
        for (BaseLocationMethod method : methods)
            trackingLog += (max.containsKey(method.getId())?max.get(method.getId()):"-")+"\t";
        trackingLog += "\n";

        trackingLog += "< 1\t";
        for (BaseLocationMethod method : methods)
            trackingLog += (above1meters.containsKey(method.getId())?(float)above1meters.get(method.getId())/(float)count.get(method.getId()):"0")+"\t";
        trackingLog += "\n";

        trackingLog += "< 2\t";
        for (BaseLocationMethod method : methods)
            trackingLog += (above2meters.containsKey(method.getId())?(float)above2meters.get(method.getId())/(float)count.get(method.getId()):"0")+"\t";
        trackingLog += "\n";

        trackingLog += "< 3\t";
        for (BaseLocationMethod method : methods)
            trackingLog += (above3meters.containsKey(method.getId())?(float)above3meters.get(method.getId())/(float)count.get(method.getId()):"0")+"\t";
        trackingLog += "\n";

        trackingLog += "\n\n";
        saveTrackingLog();
    }

    /**
     *  Converts trackingInfo hashmap to tabulated string.
     */
    private String tabulate(HashMap<String, Object> trackingInfo)
    {
        String result = "";

        System.err.println("tabulate()");
        for (String key : trackingInfo.keySet())
        {
            Object value = trackingInfo.get(key);
            String str = key+": "+value.toString();
            result += (result.isEmpty()?str:"\t"+str);
        }
        System.err.println("result: "+result);

        return result;
    }

    /**
     *  Saves tracking results into log file.
     */
    public void saveTrackingLog()
    {
        try {
            String path = dataFolder.getAbsolutePath()+ "/tracking.txt";
            File file = new File(path);

            FileOutputStream stream = new FileOutputStream(file, true);
            //FileOutputStream stream = openFileOutput(path, MODE_APPEND);
            stream.write(trackingLog.getBytes());
            stream.close();
        }
        catch (Exception e){
            Toast.makeText(this, "Error saving file: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String formatPoint(Point2.Double p)
    {
        return String.format(Locale.ENGLISH, "%.3f", p.x)+","+
                String.format(Locale.ENGLISH, "%.3f", p.y);
    }

    // ----------------------------------------------------

    public void drawnMap()
    {
        mapView.drawn();
        mapView.invalidate();
    }

    // ----------------------------------------------------

    /**
     * Getters and setters.
     */

    /**
     *
     * @return
     */
    public MapView getMapView() {
        return mapView;
    }


    // -----------------------------------------------------

    private class MethodsListAdapter extends BaseAdapter {

        private List<BaseLocationMethod> methods;
        private Context context;

        public MethodsListAdapter(List<BaseLocationMethod> methods, Context context)
        {
            this.methods = methods;
            this.context = context;
        }

        @Override
        public int getCount() {
            return methods.size();
        }

        @Override
        public Object getItem(int position) {
            return methods.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.method_list_item, parent, false);
            BaseLocationMethod method = methods.get(position);

            LinearLayout color = (LinearLayout)rowView.findViewById(R.id.methodColor);
            color.setBackgroundColor(method.getColor());

            TextView methodNameText         = (TextView)rowView.findViewById(R.id.methodNameText);
            methodNameText.setText(method.getName());

            TextView methodStatusText       = (TextView)rowView.findViewById(R.id.methodStatusText);
            methodStatusText.setText(method.isRunning()?"Running":"Stopped");
            Log.i("MethodsListAdapter", (method.isRunning()?"Running":"Stopped"));

            TextView methodRunningTimeText  = (TextView)rowView.findViewById(R.id.methodRunningTimeText);
            methodRunningTimeText.setText(method.getRunningTime()+"ms");

            Point2.Double locatedPoint = results.get(method.getId());
            TextView methodResultText = (TextView) rowView.findViewById(R.id.methodResultText);
            methodResultText.setText(locatedPoint!=null?
                String.format(Locale.ENGLISH, "%.3f", locatedPoint.getX())+","+String.format(Locale.ENGLISH, "%.3f", locatedPoint.getY()):"---");

            TextView methodErrorText = (TextView) rowView.findViewById(R.id.methodErrorText);
            methodErrorText.setText(locatedPoint!=null? MathUtil.distance(locatedPoint,actualPosition)+"":"---");

            TextView methodParametersText   = (TextView)rowView.findViewById(R.id.methodParametersText);
            methodParametersText.setText("");
            for (LocationMethodParameter parameter : method.getParameters())
                methodParametersText.append(parameter.getName()+"="+method.getParameterValue(parameter.getName())+"; ");

            return rowView;
        }
    }
}