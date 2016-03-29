package br.com.arivanbastos.jillcore.outliers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;

/**
 * This filter do NOT filter anything.
 * It's just for comparison purposes.
 */
public class NoFilter extends Filter {
	
	@JsonCreator
	public NoFilter(){}
	
	public FilterResult doFilter(DoubleSignalDataSet dataSet)
	{
		List<Double> outliers       = new ArrayList<Double>();
        List<Double> nonOutliers    = new ArrayList<Double>();
        
        // All points ARE NOT outliers.
        for (SignalSample signalSample : dataSet.getSamples())
		{
        	if (signalSample==null)
        		continue;
        	
			Double v = (Double)signalSample.getValue();
			nonOutliers.add(v);
		}
                
		return new FilterResult(outliers, nonOutliers);
	}
	
	@JsonIgnore
	public String getName()
	{
		return "NoFilter";
	}
	
	@JsonIgnore
	public Double getMeasure(DoubleSignalDataSet dataSet)
	{
		return dataSet.getAverage();
	}
}
