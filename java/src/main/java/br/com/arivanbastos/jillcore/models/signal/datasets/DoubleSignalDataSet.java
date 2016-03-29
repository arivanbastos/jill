package br.com.arivanbastos.jillcore.models.signal.datasets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import br.com.arivanbastos.jillcore.models.serialize.DoubleSignalListDeserializer;
import br.com.arivanbastos.jillcore.models.serialize.DoubleSignalListSerializer;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GeoMagneticSignalDataSet.class, name = "geomagnetic"),
        @JsonSubTypes.Type(value = BLESignalDataSet.class, name = "ble"),
        @JsonSubTypes.Type(value = WiFiSignalDataSet.class, name = "wifi"),
})
public abstract class DoubleSignalDataSet extends SignalDataSet
{
    protected Double average;

    public DoubleSignalDataSet(){
        this(null, new Date());
    }

    @JsonCreator
    public DoubleSignalDataSet(
        @JsonProperty("signalSourceId")String signalSourceId,
        @JsonProperty("date")Date date)
    {
        super(signalSourceId, date);
    }

    public Double getAverage()
    {
    	int valuesCount = 0;
        Double sum = 0.0d;

        for (SignalSample s : samples) 
        {
        	if (s!=null)
        	{
	    		DoubleSignalSample d = (DoubleSignalSample)s;
	    		sum += d.getValue();    		
	    		valuesCount++;
        	}
        }
    
        if (valuesCount > 0)
        	average = ((double)sum/(double)valuesCount);
    	else
    		average = null;
        
        return average;
    }

    @JsonIgnore
    public double getCachedAverage()
    {
        return this.average;
    }

    public void setAverage(double average)
    {
        this.average = average;
    }

    @JsonIgnore
    public Double getMaxValue()
    {
        double v = -Double.MAX_VALUE;

        for (SignalSample s : samples) 
        {
        	if (s!=null)
        	{
	            DoubleSignalSample d = (DoubleSignalSample) s;
	            if (d.getValue() > v)
	                v = d.getValue();
        	}
        }

        return v==-Double.MAX_VALUE?null:v;
    }

    @JsonIgnore
    public Double getMinValue()
    {
        double v = Double.MAX_VALUE;

        for (SignalSample s : samples) 
        {
        	if (s!=null)
        	{        	
	            DoubleSignalSample d = (DoubleSignalSample) s;
	            if (d.getValue() < v)
	                v = d.getValue();
        	}
        }

        return v==Double.MAX_VALUE?null:v;
    }
    
    @JsonIgnore
    public Double getMedian()
    {
    	DoubleSignalDataSet clone = (DoubleSignalDataSet)clone();
    	
    	// Clone the list, in order to keep the
    	// original List unchanged
    	List<SignalSample> signals = clone.getSamples();
    	
    	// Orders the list.
    	Collections.sort(signals);
    	
    	// Gets the median.
    	int mIndex = signals.size()/2;
    	if (signals.size()%2==0)
    	{    		
    		DoubleSignalSample s1 = (DoubleSignalSample)signals.get(mIndex);
    		DoubleSignalSample s2 = (DoubleSignalSample)signals.get(mIndex-1);
    		
    		return (s1.getValue()+s2.getValue())/2;
    	}
    	else
    	{
    		return ((DoubleSignalSample)signals.get(mIndex)).getValue();
    	}   	
    }

    @JsonSerialize(using = DoubleSignalListSerializer.class)
    @JsonDeserialize(using = DoubleSignalListDeserializer.class)
    public List<SignalSample> getSamples() {
        return samples;
    }
    
    public int getNonNullSamplesSize()
    {
    	int size = 0;
    	for (SignalSample s : samples) 
        	if (s!=null)
        		size++;
    	
    	return size;
    }

    public String toString()
    {
        return "(DoubleSignalDataSet)"+super.toString();
    }
}
