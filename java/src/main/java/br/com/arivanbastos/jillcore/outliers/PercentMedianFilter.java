package br.com.arivanbastos.jillcore.outliers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;

public class PercentMedianFilter extends PercentFilter {

	@JsonCreator
	public PercentMedianFilter(@JsonProperty("percent")float percent)
	{
		super(percent);
	}
	
	protected double computeReferenceValue(DoubleSignalDataSet dataSet)
	{		
		return dataSet.getMedian();
	}
	
	@Override
	@JsonIgnore
	public String getName() {
		// TODO Auto-generated method stub
		return "Percent Median "+(percent*100)+"%";
	}
}