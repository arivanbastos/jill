package br.com.arivanbastos.jillcore.models.signal;

import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.datasets.BLESignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.GeoMagneticSignalDataSet;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BLESignalDataSet.class, name = "ble"),
        @JsonSubTypes.Type(value = GeoMagneticSignalDataSet.class, name = "geomagnetic")
})
public class DoubleSignalSample extends SignalSample
{
    /*
    @JsonCreator
    public DoubleSignalSample(
            @JsonProperty("value")Object value) {
        super(value);
    }
    */
    public DoubleSignalSample(Object value) {
        super(value);
    }

    public Double getValue() {
        return (Double)value;
    }

    public String toString()
    {
        return "(DoubleSignalSample)"+super.toString();
    }

    
	public int compareTo(Object arg0) {
		DoubleSignalSample otherSignalSample = (DoubleSignalSample)arg0;

		if (this.getValue() > otherSignalSample.getValue())
			return 1;
		else if (this.getValue() < otherSignalSample.getValue())
			return -1;
		else return 0;
	}
}