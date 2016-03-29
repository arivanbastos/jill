package br.com.arivanbastos.jillcore.location;

import br.com.arivanbastos.jillcore.fitting.CurveFitter;
import br.com.arivanbastos.jillcore.fitting.FittingException;
import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.outliers.Filter;

/**
 * Computes an distance estimation using a given filter and a given
 * curve fitter. Indeed it does:
 *  
 *  1) Computes a representative value x from a samples dataset removing outliers from that.
 *  2) Fits (predicts) y from a x value, using an given CurveFitter object.
 *  
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.fitting.CurveFitter
 * @see br.com.arivanbastos.jillcore.outliers.Filter
 */
public class DistanceEstimator 
{	
	// Filter used to remove outliers from the samples dataset.
	private Filter filter;
	
	// Fitter used to infer a y given a new x.
	private CurveFitter estimator;
	
	public DistanceEstimator(Filter filter, CurveFitter estimator) 
	{
		super();
		this.filter = filter;
		this.estimator = estimator;
	}	
	
    /**
     * Estimates a distance to the given beacon given
     * an set of samples measured from that beacon.
     * 
     * @param beaconMac
     * @param dataSet
     * @return
     */
    public Double estimateDistance(String beaconMac, SignalDataSet dataSet)
    {       	
    	try
    	{
    		Double average = filter.getMeasure((DoubleSignalDataSet) dataSet);
    		
    		// Filter filtered out all values.
    		// Uses average.
    		if (average == null)
    			throw new Exception("Filter filtered out all values!");
    		
	    	Double result = estimator.fit(average);
	    	return result;
    	}
    	catch (FittingException e) 
    	{
    		
    	}
    	catch (Exception e)
    	{
    		//System.err.println(e.getMessage());
		}
    	
    	return null;
    }
    
    /**
     * Returns the filter.
     * @return
     */
	public Filter getFilter() 
	{
		return filter;
	}

	/**
	 * Sets the filter.
	 * @param filter
	 */
	public void setFilter(Filter filter) 
	{
		this.filter = filter;
	}

	/**
	 * Returns the estimator.
	 * @return
	 */
	public CurveFitter getEstimator() 
	{
		return estimator;
	}
	
	/**
	 * Sets the estimator.
	 * @param estimator
	 */
	public void setEstimator(CurveFitter estimator) 
	{
		this.estimator = estimator;
	}
	
	/**
	 * toString()
	 */
    public String toString()
    {
    	return "(Filter: "+filter.getName()+", Fitter: "+estimator.getName()+")";
    }
}