package br.com.arivanbastos.jillcore.outliers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;

/**
 * Interquartile filter. 
 * @author Administrador
 * @see https://www.youtube.com/watch?v=9aDHbRb4Bf8
 * @see http://www.r-bloggers.com/a-kernel-density-approach-to-outlier-detection/
 */
public class IQRFilter extends Filter {
	
	private float factor = 1.5f;
	
	public IQRFilter(){}
	
	@JsonCreator
	public IQRFilter(@JsonProperty("factor")float factor)
	{
		this.factor = factor;
	}
		
	private FilterResult filter(DoubleSignalDataSet dataSet)
	{
		List<Double> values = new ArrayList<Double>();
		for (SignalSample signalSample : dataSet.getSamples())	
			values.add((Double)signalSample.getValue());
						
		Collections.sort(values);		
		
		// Quantidade par		
		Double q1, q3;
		if (values.size()%2==0)
		{
			int med = values.size()/2;
			q1 = median(values, 0, med-1);
			q3 = median(values, med, values.size()-1);
		}
		else
		{
			int med = (int)Math.floor(values.size()/2);
			q1 = median(values, 0, med);
			q3 = median(values, med, values.size()-1);
		}
		
		///System.out.println(values);
		//System.out.println("Q1: "+q1+", Q3: "+q3);
		
		Double dif = q3 - q1;
		Double max = q3 + (dif*factor);
		Double min = q1 - (dif*factor);
				
		List<Double> outliers       = new ArrayList<Double>();
        List<Double> nonOutliers    = new ArrayList<Double>();
        //List<SignalSample> remove   = new ArrayList<SignalSample>();
		
		for (SignalSample signalSample : dataSet.getSamples())
		{
			Double v = (Double)signalSample.getValue();
			if (v <= max && v >= min) {
                nonOutliers.add(v);
            }
			else {
                outliers.add(v);
                //remove.add(new DoubleSignalSample(v));
            }
		}

        //dataSet.removeSamples(remove);
		
		return new FilterResult(outliers, nonOutliers);
	}
	
	private Double median(List<Double> orderedList, int begin, int end)
	{
		int dif = (end-begin);//+1;
		if (dif%2==0)
		{
            int index = begin + (int)Math.floor(dif/2);
            return orderedList.get(index);
		}
		else
		{
            int index = begin + (int)Math.floor(dif/2);
            return (orderedList.get(index)+orderedList.get(index+1))/2;
		}
	}
	
	// -----------------------------------------------
		
	public FilterResult doFilter(DoubleSignalDataSet dataSet)
	{
		FilterResult filterResult = filter(dataSet);
		List<SignalSample> remove   = new ArrayList<SignalSample>();
		for (Double value : filterResult.getOutliers())
			remove.add(new DoubleSignalSample(value));
			
		dataSet.removeSamples(remove);
		
		return filterResult;
	}
	
	@JsonIgnore
	public Double getMeasure(DoubleSignalDataSet dataSet)
	{
		DoubleSignalDataSet clone = (DoubleSignalDataSet)dataSet.clone();
		doFilter(clone);
		
		return avoidEmptyResult(clone, dataSet);
	}

	public float getFactor() {
		return factor;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}
}