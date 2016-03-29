package br.com.arivanbastos.jillcore.models.signal.datasets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;
import java.util.List;

import br.com.arivanbastos.jillcore.models.serialize.DoubleSignalListDeserializer;
import br.com.arivanbastos.jillcore.models.serialize.DoubleSignalListSerializer;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.GeoMagneticSignal;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;

public class GeoMagneticSignalDataSet extends DoubleSignalDataSet {

    public GeoMagneticSignalDataSet(String signalSourceId)
    {
        this(signalSourceId, new Date());
    }

    @JsonCreator
    public GeoMagneticSignalDataSet(
        @JsonProperty("signalSourceId")String signalSourceId,
        @JsonProperty("date")Date date)
    {
        super(signalSourceId, date);
    }

    @JsonSerialize(using = DoubleSignalListSerializer.class)
    @JsonDeserialize(using = DoubleSignalListDeserializer.class)
    public List<SignalSample> getSamples() {
        return samples;
    }

    @JsonIgnore
    public String getSignalType()
    {
        return GeoMagneticSignal.TYPE;
    }
    
    public SignalDataSet clone()
    {
    	DoubleSignalDataSet doubleSignalDataSet = new BLESignalDataSet(
			this.getSignalSourceId(),
			this.getDate()
		);
    	
    	for (SignalSample s : samples) {
    		DoubleSignalSample sample = (DoubleSignalSample) s;
    		doubleSignalDataSet.addSample(sample);
    	}
    	
    	return doubleSignalDataSet;
    }
    
    public SignalDataSet clonePartial(int size) {
    	return super.clonePartial(new GeoMagneticSignalDataSet(getSignalSourceId(), getDate()), size);
    }

    public String toString()
    {
        return "(GeoMagneticSignalDataSet)"+super.toString();
    }
}
