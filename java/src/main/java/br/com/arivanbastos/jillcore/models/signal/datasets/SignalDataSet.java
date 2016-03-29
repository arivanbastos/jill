package br.com.arivanbastos.jillcore.models.signal.datasets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.SignalSource;

// Jackson annotations for polymorphic serialization/deserialization.
// http://programmerbruce.blogspot.com.br/2011/05/deserialize-json-with-jackson-into.html
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DoubleSignalDataSet.class, name = "double"),
})
public abstract class SignalDataSet
{
    protected String signalSourceId;
    protected List<SignalSample> samples;
    protected Date date;

    public abstract SignalDataSet clone();
    public abstract SignalDataSet clonePartial(int size);
    public abstract String getSignalType();

    public SignalDataSet()
    {
        this(null, new Date());
    }

    @JsonCreator
    public SignalDataSet(
        @JsonProperty("signalSourceId")String signalSourceId,
        @JsonProperty("date")Date date)
    {
        this.signalSourceId = signalSourceId;
        this.date           = date;
        samples = new ArrayList<SignalSample>();
    }

    public SignalDataSet(ArrayList<SignalSample> samples)
    {
        this.samples = samples;
    }

    // -----------------------------------------------------

    public List<SignalSample> getSamples() {
        return samples;
    }

    public void setSamples(ArrayList<SignalSample> samples) {
        this.samples = samples;
    }

    public void addSample(SignalSample sample) {
    	//if (sample!=null)
    		samples.add(sample);
    }

    public void removeSample(SignalSample sample) {
        samples.remove(sample);
    }

    public void removeSamples(List<SignalSample> samplesToRemove) {
        samples.removeAll(samplesToRemove);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSignalSourceId() {
        return signalSourceId;
    }

    public void setSignalSourceId(String signalSourceId) {
        this.signalSourceId = signalSourceId;
    }

    public int size(){
        return samples.size();
    }
    
    public boolean isEmpty()
    {
    	return samples.isEmpty();
    }

    @JsonIgnore
    public SignalSource getSource() {
        return new SignalSource(signalSourceId, signalSourceId, getSignalType());
    }

    @JsonIgnore
    public String getId() {
        return signalSourceId+"_"+date;
    }
    
    public SignalDataSet clonePartial(SignalDataSet freshInstance, int size) {
    	Random random = new Random();
    	
    	// Gets size random values from samples.
    	for (int i = 0; i < size; i++)
    	{
    		SignalSample sample = samples.get(random.nextInt(samples.size()));
    		freshInstance.addSample(sample);
    	}
    	
    	return freshInstance;
    }

    public String toString()
    {
        String signalsStr = "source="+signalSourceId;
        for (SignalSample s : samples)
        {
        	signalsStr += (s!=null?s.toString():"null")+", ";	
        }            

        return "["+signalsStr+"]";
    }
}
