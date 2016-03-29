package br.com.arivanbastos.jillcore.fitting;

import java.util.TreeMap;

/**
 * Auxiliar class.
 * Just stores the samples that will be used with computeRegression() 
 * method.
 *  
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.CurveFitter
 */
public class DistanceMeasures {

	private TreeMap<Double, Double> data;
	
	public DistanceMeasures()
	{
		data = new TreeMap<Double, Double>();
	}
	
	/**
	 * Add a distance/measure pair.
	 * @param distance
	 * @param value
	 */
	public void add(double distance, double value)
	{
		data.put(distance, value);
	}
		
	/**
	 * Returns the signal at the given distance.
	 * @return
	 */
	public Double getSignal(double distance)
	{
		return data.get(distance);
	}
	
	/**
	 * Returns the signal/distance data ordered by
	 * distance.
	 * @return
	 */
	public double[][] getData()
	{
		double[][] result = new double[data.size()][2];
		
		int i = 0;
		for (double distance : data.keySet())
		{
			double signalValue = data.get(distance);
			result[i][0] = signalValue;
			result[i][1] = distance;
					
			i++;
		}
		
		return result;
	}
	
	/**
	 * Check if the set is empty.
	 * @return
	 */
	public boolean isEmpty()
	{
		return data.isEmpty();
	}
	
	/**
	 * toString()
	 */
	public String toString()
	{
		String result = "";
		for (Double distance : data.keySet())
			result += "("+distance+","+data.get(distance)+"),";
		
		return result;
	}
}