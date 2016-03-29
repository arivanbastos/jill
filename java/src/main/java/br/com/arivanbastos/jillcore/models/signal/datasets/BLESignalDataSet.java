package br.com.arivanbastos.jillcore.models.signal.datasets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

import br.com.arivanbastos.jillcore.models.signal.BLESignal;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;

public class BLESignalDataSet extends DoubleSignalDataSet {

    public BLESignalDataSet(String signalSourceId)
    {
        this(signalSourceId, new Date());
    }

    @JsonCreator
    public BLESignalDataSet(
        @JsonProperty("signalSourceId")String signalSourceId,
        @JsonProperty("date")Date date)
    {
        super(signalSourceId, date);
    }

    @JsonIgnore
    public String getSignalType()
    {
        return BLESignal.TYPE;
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
    	return super.clonePartial(new BLESignalDataSet(getSignalSourceId(), getDate()), size);
    }

    public String toString()
    {
        return "(BLESignalDataSet)"+super.toString();
    }
}