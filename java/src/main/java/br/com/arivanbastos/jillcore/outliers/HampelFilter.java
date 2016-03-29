/**
 * Hampel Filter implemenation based on:
 * 	 - http://en.wikipedia.org/wiki/Median_absolute_deviation
 *   - http://exploringdatablog.blogspot.com.br/2012/01/moving-window-filters-and-pracma.html
 */
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

public class HampelFilter extends Filter {
	
	// Use 0 to analyze all data.
	private int windowSize = 0;
	
	// Treshold for outliers detection.
	private float treshold = 3.0f;
	
	public HampelFilter() {}
	
	@JsonCreator
	public HampelFilter(
		@JsonProperty("windowSize")int windowSize, 
		@JsonProperty("treshold")float treshold)
	{
		this.windowSize = windowSize;
		this.treshold 	= treshold;
	}
	
	private FilterResult filter(DoubleSignalDataSet dataSet)
	{	
		//System.out.println("[HampelFilter] doFilter()");
        List<Double> nonOutliers    = new ArrayList<Double>();
		List<Double> outliers       = new ArrayList<Double>();
		
		float factor 				= 1.4826f;		
		List<SignalSample> samples 	= dataSet.getSamples();
		int size = samples.size();
		
		// Get values from sample objects.
		List<Double> values = new ArrayList<Double>();
		for (SignalSample signalSample : samples)
			values.add((Double)signalSample.getValue());
					
		Double median= 0.0;
		Double mad   = 0.0;
		List<Double> desviations=null;
		
		// If there is no window, we need compute median and 
		// deviations just one time.
		if (windowSize == 0)
		{
			median = median(values, 0, size);
			
			desviations = new ArrayList<Double>();
			for (int i2 = 0; i2 < size; i2++)	
			{
				Double value = values.get(i2);				
				desviations.add(Math.abs(value-median));
			}			
			
			// Computes MAD.
			//Collections.sort(desviations);
			//System.out.println(desviations.toString());
			//System.out.println("Median: "+median(desviations, 0, desviations.size()));
			mad = factor * median(desviations, 0, desviations.size());
		}
		
		// Loops trough value detecting outliers.
        //List<SignalSample> remove   = new ArrayList<SignalSample>();
		for (int i= 0; i < size; i++)						
		{
			// There is a window: analyzes neighborhood just within
			// the window.
			if (windowSize > 0)
			{
				int leftLimit 	= Math.max(0, i-windowSize);
				int rightLimit 	= Math.min(leftLimit+windowSize, size-1);
				
				median = median(values, leftLimit, rightLimit);
				
				// Computes desviation from each value to median.
				desviations = new ArrayList<Double>();
				for (int i2 = leftLimit; i2 <= rightLimit; i2++)	
				{
					Double value = values.get(i2);				
					desviations.add(Math.abs(value-median));
				}
			
				// Computes MAD.
				mad = factor * median(desviations, 0, desviations.size());
			}							
			
			// The value is far way from median, so it is a
			// outlier.
			Double value = values.get(i);
			//System.out.println(value+" - " +median + " > "+ treshold+" * "+mad);
			if (Math.abs(value-median) > treshold * mad)
			{
				outliers.add(value);
                //remove.add(new DoubleSignalSample(value));
				//System.out.println("  outlier: "+value);
			}
			else 
			{
                nonOutliers.add(value);
			}
		}

        //dataSet.removeSamples(remove);

		return new FilterResult(outliers, nonOutliers);
	}
	
	/**
	 * Computes a median.
	 * @param list
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	protected Double median(List<Double> list, int fromIndex, int toIndex)
	{
		List<Double> subList = new ArrayList<Double>(list.subList(fromIndex, toIndex));		
		Collections.sort(subList);
		
		int median = (int)Math.floor((toIndex - fromIndex)/2);
		return subList.get(median);
	}
	
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
	
	// --------------------------------------------------	

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public float getTreshold() {
		return treshold;
	}

	public void setTreshold(float treshold) {
		this.treshold = treshold;
	}
}
