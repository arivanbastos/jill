package br.com.arivanbastos.jillcore.outliers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;

public class PercentFilter extends Filter{
	
	protected float percent = 0.1f;
		
	public PercentFilter(){}
	
	@JsonCreator
	public PercentFilter(@JsonProperty("percent")float percent)
	{
		this.percent = percent;
	}
	
	/**
	 * Computes the average or median or whatever is
	 * used as reference value.
	 * @return
	 */
	protected double computeReferenceValue(DoubleSignalDataSet dataSet)
	{		
		// Computes average.
		double average = 0.0;
		for (SignalSample signalSample : dataSet.getSamples())	
			average += (Double)signalSample.getValue();
		
		average = average/dataSet.getSamples().size();
		
		return average;
	}
		
	private FilterResult filter(DoubleSignalDataSet dataSet)
	{
		double average = computeReferenceValue(dataSet);
		double max = average + (Math.abs(average)*percent);
		double min = average - (Math.abs(average)*percent);
		
		// Removes the points that are 10% >= then average
		// or 10% <= then average.
		List<Double> outliers       = new ArrayList<Double>();
        List<Double> nonOutliers    = new ArrayList<Double>();
        //List<SignalSample> remove   = new ArrayList<SignalSample>();
		
        //String debug = "";
		for (SignalSample signalSample : dataSet.getSamples())
		{			
			Double v = (Double)signalSample.getValue();
			//debug += (min +" | " + v + " | "+max + "\n");
			if (v <= max && v >= min) {
                nonOutliers.add(v);
            }
			else {
                outliers.add(v);
                //remove.add(new DoubleSignalSample(v));
            }
		}
		
		//if (nonOutliers.isEmpty())
			//System.err.println(debug);

        //dataSet.removeSamples(remove);		
		return new FilterResult(outliers, nonOutliers);		
	}
	
	// -------------------------------------------------	
	
	public FilterResult doFilter(DoubleSignalDataSet dataSet)
	{
		FilterResult filterResult  = filter(dataSet);
		List<SignalSample> remove  = new ArrayList<SignalSample>();
		
		for (Double value : filterResult.getOutliers())
			remove.add(new DoubleSignalSample(value));
			
		dataSet.removeSamples(remove);
		
		return filterResult;
	}
	
	@Override
	@JsonIgnore
	public String getName() {
		// TODO Auto-generated method stub
		return "Percent "+(percent*100)+"%";
	}
	
	@JsonIgnore
	public Double getMeasure(DoubleSignalDataSet dataSet)
	{
		DoubleSignalDataSet clone = (DoubleSignalDataSet)dataSet.clone();
		doFilter(clone);
		
		return avoidEmptyResult(clone, dataSet);
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}
}