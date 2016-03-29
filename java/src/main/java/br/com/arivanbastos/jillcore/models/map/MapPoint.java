package br.com.arivanbastos.jillcore.models.map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Locale;

import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;

/**
 * Represents a point in the map. A point is used to store
 * signal data.
 *
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.models.map.rooms.Map
 */
public class MapPoint
{
    // Label.
    private String label;

    // Map position.
    private float x;
    private float y;

    // Stores recorded raw signals at this point.
    private ArrayList<SignalDataSet> dataSets;

    @JsonCreator
    public MapPoint(@JsonProperty("label") String label, @JsonProperty("x") float x, @JsonProperty("y") float y)
    {
        this(x,y);
        this.label = label;
    }

    public MapPoint(float x, float y)
    {
        this.x = x;
        this.y = y;

        this.label 		= MapPoint.getDefaultLabel(x, y);
        this.dataSets 	= new ArrayList<SignalDataSet>();
    }

    public String getLabel() 
    {
        return label;
    }

    public void setLabel(String label) 
    {
        this.label = label;
    }

    public void setX(float x) 
    {
        this.x = x;
    }

    public void setY(float y) 
    {
        this.y = y;
    }

    public float getX() 
    {
        return x;
    }

    public float getY() 
    {
        return y;
    }

    public ArrayList<SignalDataSet> getDataSets() 
    {
        return dataSets;
    }

    public ArrayList<SignalDataSet> getDataSetsOfSource(String sourceId) 
    {
        ArrayList<SignalDataSet> result = new ArrayList<SignalDataSet>();
        for (SignalDataSet ds : dataSets)
        {
            if (ds.getSignalSourceId().equals(sourceId))
            	result.add(ds);
               
        }
        return result;
    }

    public ArrayList<SignalDataSet> getDataSetsOfSignal(String signalTypeId) 
    {

        ArrayList<SignalDataSet> result = new ArrayList<SignalDataSet>();
        for (SignalDataSet ds : dataSets)
        {
            if (ds.getSignalType().equals(signalTypeId))
                result.add(ds);
        }
        return result;
    }

    public void setDataSets(ArrayList<SignalDataSet> dataSets)
    {
        this.dataSets = dataSets;
    }

    public void addDataSet(SignalDataSet dataSet)
    {
        this.dataSets.add(dataSet);
    }

    public void clearData()
    {
        this.dataSets = new ArrayList<SignalDataSet>();
    }

    public boolean equals(MapPoint p)
    {
        //return (p.getY()==this.getY()) && (p.getX()==this.getX());
        float distance = (float) Math.sqrt(Math.pow(p.getX()-this.getX(), 2) +Math.pow(p.getY()-this.getY(), 2));

        // Points with distance lesser then 1cm are
        // considered the same.
        return distance<0.01;
    }

    public String toString()
    {
        return  "("+this.label+": "+String.format(Locale.ENGLISH, "%.2f",this.x)+", "+String.format(Locale.ENGLISH, "%.2f",this.y)+")";
    }

    public static String getDefaultLabel(float x, float y)
    {
        return String.format(Locale.ENGLISH, "%.2f",x)+"x"+String.format(Locale.ENGLISH, "%.2f",y);
    }
}