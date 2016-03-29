package br.com.arivanbastos.jillcore.location;

import java.util.HashMap;

import br.com.arivanbastos.jillcore.fitting.CurveFitter;
import br.com.arivanbastos.jillcore.fitting.FittingException;
import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.outliers.Filter;

/**
 * An specialization from class BaseLocationAlgorithm.
 * Represents a localization method range based (like lateration).
 * 
 * The DistanceEstimator will use a CurveFitter to, given an signal average 
 * (like RSSI or TOF), estimate the device distance.
 * This class allows there is one fitting function for each
 * device in map.
 * 
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.location.BaseLocationAlgorithm
 * @see br.com.arivanbastos.jillcore.location.DistanceEstimator
 */
abstract public class DistanceBasedLocationAlgorithm extends BaseLocationAlgorithm {
	
    // Used to estimate distance to an device,
	// given an measure (like RSSI) to that device.
    protected HashMap<String, DistanceEstimator> distanceEstimators;

    public DistanceBasedLocationAlgorithm(Map map) 
    {
        super(map, null);
        distanceEstimators = new HashMap<String, DistanceEstimator>();
    }
    
    /**
     * Remove all distance estimators.
     */
    public void clearDistanceEstimators()
    {
    	distanceEstimators.clear();
    }
    
    /**
     * Sets a fitter to algorithm use with the given device.
     * @param beaconMac
     * @param fitter
     */
    public void setDistanceEstimator(String beaconMacOrSignalType, DistanceEstimator estimator)
    {
    	distanceEstimators.put(beaconMacOrSignalType, estimator);
    }
    
    /**
     * Gets the distance estimator of a given object.
     * @param beaconMac
     * @return
     */
    public DistanceEstimator getDistanceEstimatorTo(String beaconMac)
    {
    	// Searchs for a estimator specific to device.
    	if (distanceEstimators.containsKey(beaconMac))
    	{    		
    		return distanceEstimators.get(beaconMac);
    	}
    	    	
    	else
    	{
    		// Searchs for a estimator specific to device signal type.
    		String type = (String)map.getObjectData(beaconMac, "type");    		
    		if (distanceEstimators.containsKey(type))
    		{
    			return distanceEstimators.get(type);
    		}
    		
    		// Returns a default estimator.
	    	else if (distanceEstimators.containsKey("__default"))
	    	{
	    		return distanceEstimators.get("__default");
	    	}
    	}
    	
    	return null;
    }
    
    /**
     * Sets a default fitter.
     */
    public void setDefaultDistanceEstimator(DistanceEstimator estimator)
    {
    	distanceEstimators.put("__default", estimator);
    }
    
    /**
     * Gets the default fitter.
     * @return
     */
    public DistanceEstimator getDefaultDistanceEstimator()
    {
    	if (distanceEstimators.containsKey("__default"))
    	{
    		return distanceEstimators.get("__default");
    	}
    	
    	return null;
    }
}