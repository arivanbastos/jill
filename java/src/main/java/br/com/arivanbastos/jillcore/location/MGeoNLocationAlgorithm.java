package br.com.arivanbastos.jillcore.location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import br.com.arivanbastos.jillcore.location.GeoNLocationAlgorithm;
import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.math.Circle;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.math.TwoCirclesIntersection;
import br.com.arivanbastos.jillcore.outliers.Filter;
import br.com.arivanbastos.jillcore.outliers.NoFilter;

/**
 * Modified version of GeoN algorithm.
 * To points closer to the tracked device are given more weight 
 * in the location .
 * 
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.location.GeoNLocationAlgorithm
 * @see br.com.arivanbastos.jillcore.location.DistanceBaseLocationAlgorithm
 */
public class MGeoNLocationAlgorithm extends GeoNLocationAlgorithm {

    // For how many miliseconds the method should gather and average BLE signals
    // to identify location.
    private long weightFactor = 1;
    
    public String getName()
    {
    	return "MGeoN";
    }
    
    public String getDescription()
    {
    	return "weightFactor: "+weightFactor;
    }

   
	public MGeoNLocationAlgorithm(Map map, long weightFactor)
	{
		super(map);
		
		this.weightFactor = weightFactor;
	}


	@Override
	public Point2.Double geoN(List<Circle> circles)
    {
        if (circles.size() < 2)
            return null;

        // Computes the radius sum between pairs of circles.
        // This sums are used to compute circle weights.
        Double totalRadiusSum = 0d;
        HashMap<Point2.Double, Double> pointsWeights = new HashMap<Point2.Double, Double>();
        HashMap<String, Double> radiusSum = new HashMap<String, Double>();
        for (int i = 0; i < circles.size()-1; i++) {
            for (int j = i + 1; j < circles.size(); j++) {
                Circle ci = circles.get(i);
                Circle cj = circles.get(j);

                double sum = Math.pow(ci.getRadius()+cj.getRadius(),weightFactor) ;
                radiusSum.put(i+"_"+j, sum);
                totalRadiusSum += sum;
            }
        }

        // @see The Geo-n Localization Algorithm
        // https://www.mi.fu-berlin.de/inf/groups/ag-tech/projects/ls2/ipin.pdf
        List<Point2.Double> ip  = new ArrayList<Point2.Double>();
        List<Point2.Double> aip = new ArrayList<Point2.Double>();

        for (int i = 0; i < circles.size()-1; i++)
        {
            for (int j = i+1; j < circles.size(); j++)
            {
                //System.out.println("Circle "+i+" VS circle "+j);

                Circle ci = circles.get(i);
                Circle cj = circles.get(j);

                // Identifies intersection between circles
                TwoCirclesIntersection tci = findIntersection(ci, cj);
                
                // Computes the points weights.
                Double pointWeigth = 1 - (radiusSum.get(i+"_"+j)/totalRadiusSum);

                // One intersection: aproximated
                if (tci.getP2()==null&&tci.getP1()!=null)
                {
                    pointsWeights.put(tci.getP1(), pointWeigth);
                    aip.add(tci.getP1());
                }

                // Two intersections
                else if (tci.getP1()!=null && tci.getP2()!=null)
                {
                    pointsWeights.put(tci.getP1(), pointWeigth);
                    pointsWeights.put(tci.getP2(), pointWeigth);

                    ip.add(tci.getP1());
                    ip.add(tci.getP2());
                }

                else
                {
                    // @todo: this condition is happening, but it SHOULD NOT!
                }
            }
        }

        trackingInfo.put("ip size bf", ip.size());
        trackingInfo.put("aip size", ip.size());

        //System.out.println("AIP before F1");
        //printPoints2(aip);

        // Filter 1

        // Remove intersection points that are
        // not contained in ate least N-2 circles.
        List<Point2.Double> toRemove = new ArrayList<Point2.Double>();
        for (Point2.Double point : ip)
        {
            // Count how many circles contains the point.
            int count = 0;
            for (Circle circle : circles)
            {
                if (circle.contains(point))
                    count++;
            }

            if (count < circles.size()-2)
                toRemove.add(point);
        }
        ip.removeAll(toRemove);

        trackingInfo.put("ip size af", ip.size());

        // Weighted intersection points
        List<WeightedPoint> wip = new ArrayList<WeightedPoint>();

        for (Point2.Double point : ip)
            wip.add(new WeightedPoint(pointsWeights.get(point), point));
            //wip.add(new WeightedPoint(IP_WEIGHT, point));

        for (Point2.Double point : aip)
            wip.add(new WeightedPoint(pointsWeights.get(point), point));
            //wip.add(new WeightedPoint(AIP_WEIGHT, point));

        trackingInfo.put("wip size bf", wip.size());

        //System.out.println("WIP before F2");
        //printPoints(wip);

        // For each point, computes the sum of distances
        // to all other points.
        double[] distancesSums = new double[wip.size()];
        double[] distancesSumsCopy = new double[wip.size()];

        // Filter 2

        // "Note that this median filter is only applied if the number of intersection
        //  points in WIP is not smaller than 3 because otherwise filtering would only
        //  leave one weighted intersection point that is probably farther from the true
        //  position than the result using the whole list."
        trackingInfo.put("median", "-");
        if (wip.size() >= 3)
        {
            for (int i = 0; i < wip.size(); i++)
            {
                double sum = 0.0;
                for (int j = 0; j < wip.size(); j++)
                {
                    if (i!=j)
                        sum += distance(wip.get(i).point, wip.get(j).point);
                }

                distancesSums[i] = sum;
                distancesSumsCopy[i] = sum;
            }

            // "Next, line 30 obtains the median of the distance array."
            Arrays.sort(distancesSumsCopy);
            int div = (int)Math.floor(distancesSumsCopy.length/2);
            double median= distancesSumsCopy[div+1];

            // "Then the points whose value in distance is larger than the median are eliminated."
            List<WeightedPoint> toRemoveWIP = new ArrayList<WeightedPoint>();
            for (int i = 0; i < distancesSums.length; i++)
            {
                double dist = distancesSums[i];
                if (dist > median)
                    toRemoveWIP.add(wip.get(i));
            }
            wip.removeAll(toRemoveWIP);

            trackingInfo.put("median", median);
        }

        trackingInfo.put("wip size af", wip.size());

        // "Finally the position of the unlocalized node is estimated as
        //  the weighted centroid of the remaining intersection points, as expressed in Eq. (5)"
        Point2.Double result = new Point2.Double();
        double wSum = 0;

        for (int i = 0; i < wip.size(); i++)
        {
            WeightedPoint wp = wip.get(i);

            result.x = result.x + (wp.weight * wp.point.x);
            result.y = result.y + (wp.weight * wp.point.y);
            wSum = wSum + wp.weight;
        }

        result.x = Math.min(Math.max(result.x / wSum, 0), map.getWidth());
        result.y = Math.min(Math.max(result.y / wSum, 0), map.getLength());

        return result;
    }
}
