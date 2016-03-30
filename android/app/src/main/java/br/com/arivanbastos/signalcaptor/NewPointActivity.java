package br.com.arivanbastos.signalcaptor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.com.arivanbastos.jillcore.models.signal.SignalSource;
import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.models.map.MapPoint;
import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;

import br.com.arivanbastos.signalcaptor.sensors.BLESensor;
import br.com.arivanbastos.signalcaptor.sensors.BaseSensor;
import br.com.arivanbastos.signalcaptor.sensors.exceptions.SensorNotSupportedException;
import br.com.arivanbastos.signalcaptor.sensors.events.ISensorManagerListener;
import br.com.arivanbastos.signalcaptor.sensors.GeoMagneticSensor;
import br.com.arivanbastos.signalcaptor.utils.AndroidUtil;
import br.com.arivanbastos.signalcaptor.utils.ColorUtil;
import br.com.arivanbastos.signalcaptor.utils.Preferences;
import br.com.arivanbastos.signalcaptor.views.ScrollViewWithMaxHeight;

/**
 * Interface to capture and store signal datasets into map model.
 */
public class NewPointActivity extends BaseActivity implements ISensorManagerListener, View.OnFocusChangeListener
{
    private int DEFAULT_SAMPLES_COUNT = 600;

    // The Map model.
    private Map map;
    private MapPoint currentPoint;

    // Tab container.
    // Each signal type (ie: magnetic, wifi, ble)
    // has a tab where its charts is plotted.
    private TabHost tabHost;

    // Stores the charts of each signal type.
    private HashMap<String, LineChart> charts;

    // Stores the Datasets of each signal source.
    // Each signal type may have one or more sources.
    // IE: each BLE beacon is a source of ble signal type.
    private HashMap<String, LineDataSet> chartsDataSets=null;

    // Stores one color for each dataset.
    private static HashMap<String, Integer> datasetsColors=null;

    // Stores a list of app available sensors.
    private List<BaseSensor> sensors;

    // Auxiliar.
    private List<CheckBoxSource> checkboxs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_point);

        if (datasetsColors==null)
            datasetsColors = new HashMap<String, Integer>();
    }

    /**
     * Keeps track of current point.
     * If it is changed by user (using x,y input texts) we need to display
     * the new point data.
     *
     * @param v
     * @param hasFocus
     */
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (!hasFocus)
        {
            try
            {
                MapPoint p = getCurrentPoint();

                if (v.getId() == R.id.labelText)
                {
                    currentPoint.setLabel(getTextView(R.id.labelText).getText().toString());
                }
                else
                {
                    // The current point has changed, update chart.
                    if (!p.equals(currentPoint) && getChartScope() == R.id.currentPoint) {
                        currentPoint = p;
                        loadChartData();
                    }
                }
            }
            catch (Exception e)  {
                // Nothing to do.
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_point, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        map = MainActivity.map;

        // Clear tabs.
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.clearAllTabs();

        // Clear signal data table.
        TableLayout table = (TableLayout)findViewById(R.id.signalsTable);
        table.removeViews(1, table.getChildCount()-1);

        // Populates sensors list.
        sensors = BaseSensor.getRegisteredSensors();

        charts = new HashMap<String, LineChart>();

        // Initialize sensors.
        // After that, sensor will trigger sources events.
        checkboxs = new ArrayList<CheckBoxSource>();
        for (BaseSensor sensor : sensors)
        {
            sensor.registerListener(this);
            try
            {
                sensor.init();
            }
            catch (SensorNotSupportedException sne)
            {
                Toast.makeText(this, R.string.geomagnetic_sensor_error, Toast.LENGTH_LONG).show();
            }
        }

        // Track lost focus event on x,y text inputs.
        findViewById(R.id.xText).setOnFocusChangeListener(this);
        findViewById(R.id.yText).setOnFocusChangeListener(this);
        findViewById(R.id.labelText).setOnFocusChangeListener(this);

        // Sets initial x, y values.
        float x = getIntent().getFloatExtra("x", 0f);
        float y = getIntent().getFloatExtra("y", 0f);
        getTextView(R.id.xText).setText(String.format(Locale.ENGLISH, "%.2f",x));
        getTextView(R.id.yText).setText(String.format(Locale.ENGLISH, "%.2f",y));

        currentPoint = getCurrentPoint();
        getTextView(R.id.labelText).setText(currentPoint.getLabel());

        // Loads point data (chart and table).
        loadChartData();
    }

    protected void onPause(){
        super.onPause();
        for (BaseSensor sensor : sensors)
            sensor.finalize();
    }

    /**
     * Start button tap.
     * @param v
     */
    private List<String> recordedSignalTypes;
    public void onStartTap(View v)
    {
        // Get point object from map.
        try
        {
            currentPoint = getCurrentPoint();
        }
        catch (Exception e)
        {
            Toast.makeText(this, R.string.error_invalid_coordinate, Toast.LENGTH_LONG).show();
            return;
        }

        // Stores the list of listened signals.
        recordedSignalTypes = new ArrayList<String>();

        // Stores, for each signal type, the desired samples amount.
        HashMap<String, Integer> signalsSamplesCount = new HashMap<String, Integer>();
        for (CheckBoxSource cbs : checkboxs)
        {
            if (cbs.getCheckbox().isChecked())
            {
                Preferences.writeBoolean(this.getClass().getSimpleName(), "Source_"+cbs.getSourceId(), true);

                String signalTypeId = cbs.getSignalTypeId();
                if (!recordedSignalTypes.contains(signalTypeId))
                    recordedSignalTypes.add(signalTypeId);

                if (!signalsSamplesCount.containsKey(signalTypeId))
                {
                    ArrayList<View> containers = AndroidUtil.getViewsByTag(this, signalTypeId+"-samples");
                    EditText samplesEditText = (EditText)containers.get(0);
                    try
                    {
                        int samplesCount = Integer.parseInt(samplesEditText.getText().toString());
                        Preferences.writeString(this.getClass().getSimpleName(), "Signal_"+signalTypeId, samplesCount+"");
                        signalsSamplesCount.put(signalTypeId, samplesCount);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(this, R.string.error_samples_count, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else
            {
                Preferences.writeBoolean(this.getClass().getSimpleName(), "Source_"+cbs.getSourceId(), false);
            }
        }

        // Stores, for each signal type, the desired sources.
        HashMap<String, List<String>> signalSources = new HashMap<String, List<String>>();
        for (CheckBoxSource cbs : checkboxs)
        {
            if (cbs.getCheckbox().isChecked())
            {
                if (!signalSources.containsKey(cbs.getSignalTypeId()))
                    signalSources.put(cbs.getSignalTypeId(), new ArrayList<String>());

                List<String> sources = signalSources.get(cbs.getSignalTypeId());
                sources.add(cbs.getSourceId());
            }
        }

        // Begins record process at each sensor.
        for (BaseSensor sensor : sensors)
        {
            String signalTypeId = sensor.getSignalTypeId();
            if (signalSources.containsKey(signalTypeId))
            {
                //Log.i("NewPointActivity", "starting record: "+signalTypeId+ " ("+signalsSamplesCount.get(signalTypeId)+" samples)");
                List<String> sources = signalSources.get(signalTypeId);
                sensor.beginRecord(sources, signalsSamplesCount.get(signalTypeId));
            }
        }
    }

    /**
     * Stop button tap.
     * @param v
     */
    public void onStopTap(View v)
    {
        for (BaseSensor sensor : sensors)
            sensor.endRecord();
    }

    /**
     * Remove button tap.
     * Removes point from map.
     * @param v
     */
    public void onRemoveTap(View v)
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to remove this point and all associated data?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        map.removePoint(getCurrentPoint());
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Clear button tap.
     * Clear all button data.
     * @param v
     */
    public void onClearTap(View v)
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to remove all point data?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MapPoint p = getCurrentPoint();
                        p.clearData();

                        loadChartData();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    // --------------------------------------------------------------

    /**
     * Sources update.
     * Each time a source begins available or unavailable, the sensor
     * fires an event.
     *
     * IE: you become too far from an beacon and the beacon is not more
     *   visible. The BLESensor will trigger an sourceUnavailable event.
     */

    /**
     * Creates a row in signals table to store the signal sources checkboxs.
     * Returns the signal sources checkboxs container.
     * @param signalTypeId
     * @return
     */
    public LinearLayout getSourcesCheckboxsContainer(String signalTypeId)
    {
        // Is there already a row for this signal type?
        LinearLayout checkboxContainer;
        ArrayList<View> containers = AndroidUtil.getViewsByTag(this, signalTypeId+"-checkboxContainer");
        if (containers.size()==0) { // There is NO row

            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            row.setTag(signalTypeId + "-row");

            // Signal name.
            TextView signalType = new TextView(this);
            signalType.setText(signalTypeId);
            signalType.setTextColor(Color.BLACK);
            signalType.setBackgroundResource(R.drawable.table_border);
            signalType.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 17f));
            signalType.setGravity(Gravity.CENTER);

            // Signal samples (of each source).
            EditText signalSamples = new EditText(this);
            signalSamples.setInputType(InputType.TYPE_CLASS_NUMBER);
            signalSamples.setBackgroundResource(R.drawable.table_border);
            signalSamples.setTag(signalTypeId + "-samples");
            signalSamples.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 17f));
            signalSamples.setText(Preferences.readString(this.getClass().getSimpleName(), "Signal_"+signalTypeId, DEFAULT_SAMPLES_COUNT + ""));
            signalSamples.setTextColor(Color.BLACK);
            signalSamples.setEms(10);

            // Container for sources checkboxs.
            checkboxContainer = new LinearLayout(this);
            checkboxContainer.setOrientation(LinearLayout.HORIZONTAL);
            checkboxContainer.setTag(signalTypeId + "-checkboxContainer");
            //checkboxContainer.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 60f));

            LinearLayout left = new LinearLayout(this);
            left.setOrientation(LinearLayout.VERTICAL);
            LinearLayout right = new LinearLayout(this);
            right.setOrientation(LinearLayout.VERTICAL);
            checkboxContainer.addView(left);
            checkboxContainer.addView(right);

            row.addView(signalType);
            row.addView(signalSamples);

            ScrollViewWithMaxHeight scrollView = new ScrollViewWithMaxHeight(this);
            scrollView.setMaxHeight(500);
            //ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 66f));
            scrollView.addView(checkboxContainer);
            row.addView(scrollView);
            //row.addView(checkboxContainer);

            TableLayout table = (TableLayout)findViewById(R.id.signalsTable);
            table.addView(row);
        }
        else // There is a row
        {
            checkboxContainer = (LinearLayout)containers.get(0);
        }

        return checkboxContainer;
    }

    /**
     * ISensorManagerListener interface.
     */

    /**
     * A source becomes AVAILABLE.
     * @param source
     */
    public void onSourceAvailable(final SignalSource source)
    {
        final String className = this.getClass().getSimpleName();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Log.i("NewPointActivity", "onSourceAvailable: " + source.getId() + " " + source.getSignalType());

                CheckBox ch = new CheckBox(getApplicationContext());
                ch.setText(source.getLabel());
                CheckBoxSource checkBoxSource = new CheckBoxSource(ch, source.getId(), source.getSignalType());

                if (!checkboxs.contains(checkBoxSource)) {
                    //Log.i("NewPointActivity", "   adding");
                    LinearLayout checkboxContainer = getSourcesCheckboxsContainer(source.getSignalType());

                    ch.setTextColor(Color.BLACK);
                    ch.setBackgroundColor(Color.WHITE);

                    if (checkboxs.size()%2==0)
                        ((LinearLayout)checkboxContainer.getChildAt(1)).addView(ch);
                    else
                        ((LinearLayout)checkboxContainer.getChildAt(0)).addView(ch);

                    //checkboxContainer.addView(ch);

                    checkboxs.add(new CheckBoxSource(ch, source.getId(), source.getSignalType()));
                    ch.setChecked(Preferences.readBoolean(className, "Source_"+checkBoxSource.getSourceId(), false));
                }
            }
        });
    }

    /**
     * A source becomes UNAVAILABLE.
     * @param source
     */
    public void onSourceUnavailable(final SignalSource source){
        /*
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("NewPointActivity","onSourceUnavailable: "+source.getId());

                ArrayList<View> containers = AndroidUtil.getViewsByTag(activity, source.getSignalType() + "-checkboxContainer");
                if(containers.size()>0)

                {
                    LinearLayout checkboxContainer = (LinearLayout) containers.get(0);
                    List<CheckBoxSource> toRemove = new ArrayList<CheckBoxSource>();
                    for (CheckBoxSource checkBoxSource : checkboxs) {
                        if (checkBoxSource.getSourceId().equals(source.getId()) && checkBoxSource.getSignalTypeId().equals(source.getSignalType())) {
                            checkboxContainer.removeView(checkBoxSource.getCheckbox());
                            //checkboxs.remove(checkBoxSource);
                            toRemove.add(checkBoxSource);
                        }
                    }
                    checkboxs.removeAll(toRemove);
                }
            }
        });
         */
    }


    // -----------------------------------------------------

    /**
     * Creates a TextView for DataTable.
     * Auxiliar to createDataRow.
     * @param tag
     * @param text
     * @return
     */
    private TextView createDataRowText(String tag, String text, float weight)
    {
        TextView signalType = new TextView(this);
        signalType.setText(text);
        signalType.setTextColor(Color.BLACK);
        signalType.setBackgroundResource(R.drawable.table_border);
        signalType.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, weight));
        signalType.setGravity(Gravity.CENTER);
        signalType.setTag(tag);

        return signalType;
    }

    /**
     * Signal begin record event.
     */
    public void onRecordBegin()
    {
        // Disables interface elements.
        setInterfaceEnabled(false);
    }

    /**
     * Signal record progress event.
     * Fired when one new signal sample is captured.
     */
    public void onRecordProgress(final SignalSource source, final SignalSample sampleValue, final SignalDataSet dataSet)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Log.i("NewPointActivity", "onRecordProgress: " + source.getSignalType() + ", " + source.getId() + " (" + dataSet.getSamples().size() + ")");

                int samplesCount = dataSet.size();
                DoubleSignalSample sample = (DoubleSignalSample) sampleValue;

                // Update data table.
                updateDataTable(dataSet);

                // No need to update graph in real time.
                // Update graph.
                //LineDataSet chartDataSet = getChartDataSet(source.getSignalType(), source.getId());
                //addToGraph(chartDataSet, sample);
            }
        });
    }

    /**
     * Fired when one single source has captured the desired samples amount.
     */
    public void onSingleSourceRecordEnd(final SignalSource source, final SignalDataSet dataSet)    {}

    /**
     * Fired when all sources of the informed signal type has captured the desired samples amount.
     */
    public void onAllSourcesRecordEnd(final String signalTypeId, final List<SignalDataSet> dataSets)
    {
        recordedSignalTypes.remove(signalTypeId);

        final NewPointActivity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Stores data in map model.
                for (SignalDataSet dataSet : dataSets)
                    currentPoint.addDataSet(dataSet);

                // Update charts.
                refreshChartOfSignal(signalTypeId);

                // All sources from all signals ended?
                if (recordedSignalTypes.isEmpty()) {
                    // Notifies user.
                    beep();
                    Toast.makeText(activity, R.string.scan_end, Toast.LENGTH_SHORT).show();

                    // Enables interface elements.
                    setInterfaceEnabled(true);

                    CheckBox cb = (CheckBox) findViewById(R.id.saveAfterFinish);
                    if (cb.isChecked()) {
                        MainActivity rootActivity = (MainActivity) BaseActivity.root;
                        rootActivity.onSaveMapTap(null);
                    }
                }
            }
        });
    }

    // -----------------------------------------


    /**
     * Creates a row in the data table to store a
     * signal source data (source, date, count, min, max and avg).
     */
    public void createDataRow(SignalDataSet dataSet)
    {
        SignalSource source=  dataSet.getSource();
        ArrayList<View> containers = AndroidUtil.getViewsByTag(this, dataSet.getId()+"-dataRow");
        if (containers.size()==0) { // There is NO row

            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            row.setTag(dataSet.getId() + "-dataRow");

            // Signal type.
            TextView signalType = createDataRowText(dataSet.getId()+"-signalTypeText", source.getSignalType(), 20f);

            // Source id.
            TextView sourceId= createDataRowText(dataSet.getId()+"-sourceIdText", source.getId(), 35f);

            // Record date.
            TextView date = createDataRowText(dataSet.getId()+"-dateText", "", 20f);

            // Samples recorded.
            TextView samplesCount = createDataRowText(dataSet.getId()+"-samplesCountText", "0", 15f);

            // Min, max and avg.
            TextView min = createDataRowText(dataSet.getId()+"-minText", "---", 15f);
            TextView max = createDataRowText(dataSet.getId()+"-maxText", "---", 15f);
            TextView avg = createDataRowText(dataSet.getId()+"-avgText", "---", 15f);

            row.addView(signalType);
            row.addView(sourceId);
            row.addView(date);
            row.addView(samplesCount);
            row.addView(min);
            row.addView(max);
            row.addView(avg);

            TableLayout table = (TableLayout)findViewById(R.id.dataTable);
            table.addView(row);
        }
    }

    /**
     * Displayd a dataset in the data table.
     */
    public void updateDataTable(SignalDataSet dataSet)
    {
        // Creates a line within table to store
        // datasource info.
        createDataRow(dataSet);
        SignalSource source = dataSet.getSource();

        // Sets row data.
        AndroidUtil.getTextViewByTag(this, dataSet.getId()+"-signalTypeText").setText(source.getSignalType());
        AndroidUtil.getTextViewByTag(this, dataSet.getId()+"-sourceIdText").setText(source.getId());
        AndroidUtil.getTextViewByTag(this, dataSet.getId()+"-samplesCountText").setText(dataSet.size()+"");

        SimpleDateFormat dt = new SimpleDateFormat("MM-dd hh:mm:ss");
        AndroidUtil.getTextViewByTag(this, dataSet.getId()+"-dateText").setText(dt.format(dataSet.getDate()));

        DoubleSignalDataSet doubleSignalDataSet = (DoubleSignalDataSet)dataSet;
        AndroidUtil.getTextViewByTag(this, dataSet.getId()+"-minText").setText(String.format("%.2f", doubleSignalDataSet.getMinValue()));
        AndroidUtil.getTextViewByTag(this, dataSet.getId()+"-maxText").setText(String.format("%.2f", doubleSignalDataSet.getMaxValue()));
        AndroidUtil.getTextViewByTag(this, dataSet.getId()+"-avgText").setText(String.format("%.2f", doubleSignalDataSet.getAverage()));
    }

    // ------------------------------------------

    /**
     * Radiobutton callback for changes in chart data scope.
     * @param v
     */
    public void onChartDataRadioButtonClicked(View v)
    {
        loadChartData();
    }

    /**
     * There is one chart for each signal type.
     * Returns the chart component of a specific signal type.
     * @return
     */
    public LineChart getChart(String signalTypeId)
    {
        if (!charts.containsKey(signalTypeId)) {
            // Create a chart component
            //chart = (LineChart) findViewById(R.id.chart);
            final LineChart chart = new LineChart(this);
            //chart.setUnit("Db");
            chart.setUnit("");
            //chart.setDrawUnitsInChart(true);
            chart.setDrawYValues(false);
            chart.setDrawBorder(false);
            chart.setTouchEnabled(true);
            chart.setDragEnabled(true);
            chart.setNoDataTextDescription("No data gathered.");
            chart.setDescription("Samples");
            chart.setPinchZoom(true); // if disabled, scaling can be done on x- and y-axis separately
            // set an alternative background color
            // mChart.setBackgroundColor(Color.GRAY)

            // Creates a chart tab.
            // http://www.java2s.com/Code/Android/UI/DynamicTabDemo.htm
            // http://www.java2s.com/Code/Android/UI/UsingatabcontentfactoryforthecontentviaTabHostTabSpecsetContentandroidwidgetTabHostTabContentFactory.htm
            TabHost.TabSpec spec = tabHost.newTabSpec(signalTypeId);
            spec.setIndicator(signalTypeId);
            spec.setContent(new TabHost.TabContentFactory() {
                public View createTabContent(String tag) {
                    return chart;
                }
            });
            tabHost.addTab(spec);
            //addContentToTab(tabHost.getChildCount(), chart);

            charts.put(signalTypeId, chart);
            tabHost.invalidate();
        }

        return charts.get(signalTypeId);
    }

    /**
     * Creates a LineDataSet object.
     * @param signalSourceId
     * @param date
     * @return
     */
    public LineDataSet createChartDataSet(String signalSourceId, Date date)
    {
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<Entry>(), signalSourceId);
        int color = getColor(signalSourceId);
        lineDataSet.setColors(new int[]{color});
        lineDataSet.setDrawCircles(false);

        return lineDataSet;
    }

    /**
     * Load initial chart data from map object.
     */
    public void loadChartData()
    {
        // Clear data table.
        TableLayout table = (TableLayout)findViewById(R.id.dataTable);
        table.removeViews(1, table.getChildCount()-1);

        // Show data from all map points.
        if (getChartScope() == R.id.allPoints)
        {
            for (MapPoint p : map.getPoints())
                addToGraph(p);
        }

        // Show data from the current point only
        // (the tapped point or the point informed in X,Y text fields).
        else
        {
            addToGraph(getCurrentPoint());
        }

        refreshCharts();
    }

    /**
     * Add a point and all its data to chart.
     * @param p
     */
    public void addToGraph(MapPoint p)
    {
        for (BaseSensor sensor : sensors)
            refreshChartOfSignal(p, sensor.getSignalTypeId());
    }

    /**
     * Add the data of a specific signal type of the current point
     * to chart.
     */
    public void refreshChartOfSignal(String signalTypeId)
    {
        MapPoint p = getCurrentPoint();
        refreshChartOfSignal(p, signalTypeId);
    }

    /**
     * Add the data of a specific signal type of a specific point
     * to chart.
     */
    public void refreshChartOfSignal(MapPoint p, String signalTypeId)
    {
        LineChart chart = getChart(signalTypeId);
        ArrayList<LineDataSet> currentChartDataSets    = new  ArrayList<LineDataSet>();

        for (SignalDataSet ds : p.getDataSetsOfSignal(signalTypeId))
        {
            //Log.i("NewPointActivity", "addToGraph ("+p+"): "+signalTypeId+", "+ds.getSignalSourceId()+", "+ds.getDate()+", "+ds.size());

            LineDataSet chartDataSet = createChartDataSet(ds.getSignalSourceId(), ds.getDate());

            for (SignalSample s : ds.getSamples())
            {
                if (s==null) {
                    // @todo Each dataset must have its own null value.
                    // Here the null value is fixed -150f.
                    chartDataSet.addEntry(new Entry(-150f, chartDataSet.getEntryCount()));
                }
                else
                {
                    // Add the sample to chart line.
                    DoubleSignalSample doubleSignalSample = (DoubleSignalSample) s;
                    chartDataSet.addEntry(new Entry(doubleSignalSample.getValue().floatValue(), chartDataSet.getEntryCount()));
                }
            }

            // Add the line to chart.
            currentChartDataSets.add(chartDataSet);

            // Updates table.
            updateDataTable(ds);
        }

        refreshChart(chart, currentChartDataSets);
    }

    /**
     * Refreshs all charts.
     */
    public void refreshCharts()
    {
        //for (CheckBoxSource cbs : checkboxs)
        for (BaseSensor sensor : sensors)
            refreshChartOfSignal(sensor.getSignalTypeId());
    }

    /**
     * Refreshs a chart.
     * @param chart
     * @param chartDataSets
     */
    public void refreshChart(LineChart chart, ArrayList<LineDataSet> chartDataSets)
    {
        // Populates X List.
        // Identifies the biggets dataset.
        int maxSize = 0;
        for (LineDataSet dataSet : chartDataSets)
            if (dataSet.getEntryCount()>maxSize)
                maxSize = dataSet.getEntryCount();

        ArrayList<String> xVals = new ArrayList<String>();
        while (xVals.size() < maxSize)
            xVals.add((xVals.size()+1)+"");

        LineData chartData = new LineData(xVals, chartDataSets);
        chart.setData(chartData);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    /**
     * Return the chart data scope (R.id.allPoints or R.id.currentPoint).
     * @return
     */
    public int getChartScope()
    {
        RadioGroup rg = (RadioGroup)findViewById(R.id.chartDataRadioGroup);
        return rg.getCheckedRadioButtonId();
    }

    /**
     * Gets the current point object.
     * @return
     * @throws NumberFormatException
     */
    protected MapPoint getCurrentPoint() throws NumberFormatException
    {
        EditText xInput = (EditText)findViewById(R.id.xText);
        EditText yInput = (EditText)findViewById(R.id.yText);

        // Gets the map coordinate.
        try
        {
            float x = Float.parseFloat(xInput.getText().toString());
            float y = Float.parseFloat(yInput.getText().toString());

            return map.getPoint(x, y);
        }
        catch (Exception e)
        {
            throw e;
        }
    }


    // ------------------------------------------

    /**
     * Interface.
     */

    /**
     *
     * @param enabled
     */
    public void setInterfaceEnabled(boolean enabled)
    {
        // Enable interface elements.
        findViewById(R.id.startButton).setEnabled(enabled);
        findViewById(R.id.stopButton).setEnabled(enabled);
        findViewById(R.id.removeButton).setEnabled(enabled);

        for (CheckBoxSource cbs : checkboxs)
            cbs.getCheckbox().setEnabled(enabled);

        // Always disabled.
        //findViewById(R.id.xText).setEnabled(enabled);
        //findViewById(R.id.yText).setEnabled(enabled);
    }

    /**
     * Rounds a double to 2 decimal places.
     * @param v
     * @return
     */
    private double rnd(double v)
    {
        return (double) Math.round(v * 100) / 100;
    }

    /**
     * Returns a unique color for the given dataset id.
     * @return
     */
    private int getColor(String id)
    {
        if (datasetsColors.get(id) ==null)
            datasetsColors.put(id, ColorUtil.nextUniqueColor());

        return datasetsColors.get(id);
    }

    /**
     * Auxiliar class.
     */
    private class CheckBoxSource
    {
        private CheckBox checkbox;
        private String sourceId;
        private String signalType;

        private CheckBoxSource(CheckBox checkbox, String sourceId, String signalType) {
            this.checkbox = checkbox;
            this.sourceId = sourceId;
            this.signalType = signalType;
        }

        public CheckBox getCheckbox() {
            return checkbox;
        }

        public void setCheckbox(CheckBox checkbox) {
            this.checkbox = checkbox;
        }

        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getSignalTypeId() {
            return signalType;
        }

        public void setSignalTypeId(String signalType) {
            this.signalType = signalType;
        }

        public boolean equals(Object o)
        {
            CheckBoxSource checkBoxSource = (CheckBoxSource)o;
            return checkBoxSource.getSourceId().equals(sourceId) &&
                    checkBoxSource.getSignalTypeId().equals(signalType);
        }
    }
}
