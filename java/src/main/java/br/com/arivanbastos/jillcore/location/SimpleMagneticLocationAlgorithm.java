package br.com.arivanbastos.jillcore.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.map.MapPoint;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.signal.GeoMagneticSignal;
import br.com.arivanbastos.jillcore.models.signal.datasets.GeoMagneticSignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.outliers.NoFilter;

/**
 * A simple magnetic-based algorithm that uses fingerprinting.
 * Just search for the cell with the closer magnetic intensity in 
 * the fingerprinting map.
 */
public class SimpleMagneticLocationAlgorithm extends BaseLocationAlgorithm 
{
	/**
	 * constructor
	 * @param map
	 */
    public SimpleMagneticLocationAlgorithm(Map map) 
    {
        super(map, new NoFilter());
    }
    
    /**
     * Gets the name.
     */
    public String getName()
    {
    	return "SimpleMagnetic";
    }
    
    /**
     * Gets the description.
     */
    public String getDescription()
    {
    	return "";
    }

    /**
     * Implements the algorithm.
     */
    @Override
    public Point2.Double run(List<SignalDataSet> dataSets)
    {
    	if (dataSets.size() == 0)
    		return null;
    	
        // In magnetic signals there is only one sirgnal source.
        SignalDataSet data = dataSets.get(0);
        GeoMagneticSignalDataSet geoMagneticSignalDataSet = (GeoMagneticSignalDataSet)data;
        MapPoint selected = null;

        if (geoMagneticSignalDataSet!=null) {

            Double avg = geoMagneticSignalDataSet.getAverage();

            logger.info("Magnetic field average: "+avg);

            double selectedDiff = Double.MAX_VALUE;

            // Loop trough map points comparing obtained average
            // with the point dataset average.
            for (MapPoint p : map.getPoints()) {
                ArrayList<SignalDataSet> pointDataSets = p.getDataSetsOfSource(GeoMagneticSignal.TYPE);

                // The point does not have sensor data.
                if (pointDataSets.isEmpty())
                    continue;

                Double datasetsAvg = 0d;

                for (SignalDataSet ds : pointDataSets) {
                    GeoMagneticSignalDataSet dsGeo = (GeoMagneticSignalDataSet) ds;
                    datasetsAvg += dsGeo.getAverage();
                }

                datasetsAvg = (double) datasetsAvg / (double) pointDataSets.size();
                double diff = Math.abs(avg - datasetsAvg);

                //Log.i("SimpleMagnetic", "Comparing: " + selected + " with " + p + "("+selectedDiff+" > "+diff+") ("+avg+"-"+datasetsAvg+")");

                // Checks whether current point has a smaller average difference.
                if (selected == null || selectedDiff > diff) {
                    selected = p;
                    selectedDiff = diff;
                }
            }
        }
        else
        {
        	logger.info("No magnetic field detected.");
        }

        // Broadcasts onLocationInfoAvailable() event with
        // the identified point.
        return new Point2.Double(selected.getX(),selected.getY());
    }
}
