package br.com.arivanbastos.jillcore.location;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.outliers.Filter;

/**
 * Is a class that actually performs location process.
 * In order the location can take place, we need:
 * 
 *   1) A map: where the location occurs.
 *   2) Sets of signalsets: signal sets from signal emitters in map.
 *   3) A filter: to remove outliers from item 2. 
 * 
 * @author Arivan Bastos <arivanbastos at gmail.com>
 */
public abstract class BaseLocationAlgorithm {	
	
    // Map where location occurs.
    protected Map map;
        
    // Used to filter outliers.
    protected Filter filter;
    
    // Stores tracking information, with
    // debug purposes only.
    protected HashMap<String, Object>trackingInfo;
    
    protected Logger logger;
    
    public BaseLocationAlgorithm(Map map, Filter filter) 
    {
    	logger 			= Logger.getLogger(this.getClass().getName());
    	trackingInfo 	= new HashMap<String, Object>();
        
    	this.map 		= map;
    	this.filter		= filter;
    }

    /**
     * Do the location process.
     * @param dataSets
     * @return
     */
    public abstract Point2.Double run(List<SignalDataSet> dataSets);
    
    /**
     * @return String the location algorithm name.
     */
    public abstract String getName();
    
    /**
     * @return String the location algorithm description.
     */
    public abstract String getDescription();

    // -----------------------------------------------
    
    /**
     * Adds a track info.
     * @param id
     * @param value
     */
    public void setTrackingInfo(String id, String value)
    {
    	trackingInfo.put(id, value);
    }
    
    /**
     * Recovers a track info.
     * @param id
     * @return
     */
    public Object getTrackingInfo(String id)
    {
    	return trackingInfo.get(id);
    }
    
    /**
     * Remove all track info.
     */
    public void clearTrackingInfo()
    {
    	trackingInfo.clear();
    }
    
    /**
     * Return the filter.
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
     * Returns the map.
     * @return
     */
    public Map getMap() {
        return map;
    }
    
    /**
     * Set the map.
     * @param map
     */
    public void setMap(Map map) {
        this.map = map;
    }
}