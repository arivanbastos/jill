package br.com.arivanbastos.jillcore.models.signal.datasets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.WiFiSignal;

public class WiFiSignalDataSet extends DoubleSignalDataSet {

    public WiFiSignalDataSet(String signalSourceId)
    {
        this(signalSourceId, new Date());
    }

    @JsonCreator
    public WiFiSignalDataSet(
        @JsonProperty("signalSourceId")String signalSourceId,
        @JsonProperty("date")Date date)
    {
        super(signalSourceId, date);
    }

    @JsonIgnore
    public String getSignalType()
    {
        return WiFiSignal.TYPE;
    }
    
    public SignalDataSet clone()
    {
    	DoubleSignalDataSet doubleSignalDataSet = new BLESignalDataSet(
			this.getSignalSourceId(),
			this.getDate()
		);
    	
    	for (SignalSample s : samples) {
    		DoubleSignalSample sample = new DoubleSignalSample(((DoubleSignalSample)s).getValue());
    		doubleSignalDataSet.addSample(sample);
    	}
    	
    	return doubleSignalDataSet;
    }
    
    public SignalDataSet clonePartial(int size) {
    	return super.clonePartial(new WiFiSignalDataSet(getSignalSourceId(), getDate()), size);
    }

    public String toString()
    {
        return "(WiFiSignalDataSet)"+super.toString();
    }
}