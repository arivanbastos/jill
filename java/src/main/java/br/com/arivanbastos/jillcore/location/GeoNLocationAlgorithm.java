package br.com.arivanbastos.jillcore.location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.map.MapObject;
import br.com.arivanbastos.jillcore.models.math.Circle;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.math.TwoCirclesIntersection;
import br.com.arivanbastos.jillcore.models.signal.datasets.BLESignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.outliers.Filter;
import br.com.arivanbastos.jillcore.outliers.FilterResult;
import br.com.arivanbastos.jillcore.outliers.NoFilter;

/**
 * Implements the Geo-N location algoritm.
 * 
 * @see https://www.mi.fu-berlin.de/inf/groups/ag-tech/projects/ls2/ipin.pdf
 * @see br.com.arivanbastos.jillcore.location.DistanceBaseLocationAlgorithm
 */
public class GeoNLocationAlgorithm extends DistanceBasedLocationAlgorithm 
{			
	/**
	 * Constructor.
	 * @param map
	 */
	public GeoNLocationAlgorithm(Map map)
	{
		super(map);
	}
	
	/**
	 * Returns the method name.
	 */
    public String getName()
    {
    	return "GeoN";
    }
    
    /**
     * Returns a method description.
     */
    public String getDescription()
    {
    	return "";
    }
  
    /**
     * Implemens the location method.
     */
    @Override
    public Point2.Double run(List<SignalDataSet> dataSets)
    {    	
    	logger.info("Samples collection finished.");

        List<Circle> circles = new ArrayList<Circle>();

        // Estimates the distance to each beacon.
        for (SignalDataSet ds : dataSets)
        {
        	String beaconMac = ds.getSignalSourceId();        	
        	
            // MapObject IDs don't have ':' character. Instead they have '_'.
            MapObject object = map.getObject(beaconMac.replace(':', '_'));

            if (object==null)
            {
                // Unknow beacon.
            	logger.info("Unknow beacon "+beaconMac+".");
            }
            else
            {
            	DistanceEstimator estimator = getDistanceEstimatorTo(object.getId());
        		Double distance = estimator.estimateDistance(beaconMac, ds);      
                if (distance==null)
                	continue;  
                
                // Gets beacon position.
                Point2.Double position = new Point2.Double(object.getPosition().getX(), object.getPosition().getY());

                // Creates the circle that represents the signal area.
                circles.add(new Circle(position, distance));
            }
        }
        
        trackingInfo.put("circles", circles);

        logger.info(circles.size() + " circles identified.");

        // Run algorithm.
        return geoN(circles);
    }

    // --------------------------------------------------

    /**
     * Actual algorithm.
     */

    // "In our experiments, assigning equal weight showed the best
    // results whereas in simulations weighting real intersection
    // points three times higher than approximated ones gave the
    // best performance."
    public static final double IP_WEIGHT    = 1.0d;
    public static final double AIP_WEIGHT   = 1.0d;

    /**
     * Implements the Geo-N algorithm.
     * @param circles
     * @return
     */
    public Point2.Double geoN(List<Circle> circles)
    {
        if (circles.size() < 2)
            return null;

        List<Point2.Double> ip = new ArrayList<Point2.Double>();
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

                // One intersection: aproximated
                if (tci.getP2()==null&&tci.getP1()!=null)
                {
                    aip.add(tci.getP1());
                }

                // Two intersections
                else if (tci.getP1()!=null && tci.getP2()!=null)
                {
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
        {
            wip.add(new WeightedPoint(IP_WEIGHT, point));
        }

        for (Point2.Double point : aip)
        {
            wip.add(new WeightedPoint(AIP_WEIGHT, point));
        }

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

    /**
     * Computes the intersection between two circles. There is three cases:
     *   1) The circles are the same: no intersection.
     *   2) The circles intersect in two points.
     *   3) The circles are far away, so we compute the intersection between the line
     *   		that pass grough the circles center and each circle.
     * http://www.vb-helper.com/howto_circle_circle_intersection.html
     * @return
     */
    public TwoCirclesIntersection findIntersection(Circle c0, Circle c1)
    {
        // Find the distance between the centers.
        double dx = c0.getCenter().getX() - c1.getCenter().getX();
        double dy = c0.getCenter().getY() - c1.getCenter().getY();

        double distance = Math.sqrt((dx*dx) + (dy*dy));

        // See how many solutions there are.
        if (distance==0 && c0.getRadius()==c1.getRadius())
        {
            // The circles are the same
            // This should never happen        	
            return new TwoCirclesIntersection(null, null);
        }

        // There is no intersection.
        else if ((distance > c0.getRadius()+c1.getRadius()) || // the circles are far away
                (distance < Math.abs(c0.getRadius()-c1.getRadius()))) // one circle contains another
        {       	
            // Finds the intersection between the line that pass through the circles center
            // and each circle.
            List<Point2.Double> intersectionC0 = getCircleLineIntersectionPoint(c0.getCenter(), c1.getCenter(), c0);
            List<Point2.Double> intersectionC1 = getCircleLineIntersectionPoint(c0.getCenter(), c1.getCenter(), c1);

            // @todo: sometimes the intersections are not having sizing 2.
            // Why?
            if (intersectionC0.size()==2 && intersectionC1.size()==2) {
                Point2.Double c00 = intersectionC0.get(0);
                Point2.Double c01 = intersectionC0.get(1);
                Point2.Double c10 = intersectionC1.get(0);
                Point2.Double c11 = intersectionC1.get(1);

                double dc00c10 = distance(c00, c10);
                double dc00c11 = distance(c00, c11);
                double dc01c10 = distance(c01, c10);
                double dc01c11 = distance(c01, c11);

                double min = Math.min(dc00c10, Math.min(dc00c11, Math.min(dc01c10, dc01c11)));
                
                if (min == dc00c10)
                    return new TwoCirclesIntersection(middle(c00, c10), null);
                else if (min == dc00c11)
                    return new TwoCirclesIntersection(middle(c00, c11), null);
                else if (min == dc01c10)
                    return new TwoCirclesIntersection(middle(c01, c10), null);
                else
                    return new TwoCirclesIntersection(middle(c01, c11), null);
            }
            else
            {                
                return new TwoCirclesIntersection(null, null);
            }
        }

        // Find the intersection points.
        else
        {
            // Find a and h.
            double a = ((c0.getRadius()*c0.getRadius()) - (c1.getRadius()*c1.getRadius()) + (distance*distance))/(2*distance);
            double h = Math.sqrt((c0.getRadius() * c0.getRadius()) - (a * a));

            // Find p2.
            Point2.Double p2 = new Point2.Double();
            p2.x = c0.getCenter().getX() + (a * (c1.getCenter().getX()-c0.getCenter().getX()) / distance);
            p2.y = c0.getCenter().getY() + (a * (c1.getCenter().getY()-c0.getCenter().getY()) / distance);

            // Find the intersection points.
            Point2.Double i1 = new Point2.Double();
            Point2.Double i2 = new Point2.Double();

            i1.x = p2.getX() + h * (c1.getCenter().getY() - c0.getCenter().getY())/distance;
            i1.y = p2.getY() - h * (c1.getCenter().getX() - c0.getCenter().getX())/distance;

            i2.x = p2.getX() - h * (c1.getCenter().getY() - c0.getCenter().getY())/distance;
            i2.y = p2.getY() + h * (c1.getCenter().getX() - c0.getCenter().getX())/distance;

            if (i1.equals(i2))
                return new TwoCirclesIntersection(i1, null);
            else
                return new TwoCirclesIntersection(i1, i2);
        }
    }
    
    /**
     * Computes a intersection between a circle and a line.
     * @param pointA
     * @param pointB
     * @param circle
     * @return
     */
    public List<Point2.Double> getCircleLineIntersectionPoint(
            Point2.Double pointA,
            Point2.Double pointB,
            Circle circle)
    {
        Point2.Double center = circle.getCenter();
        double radius = circle.getRadius();

        double baX = pointB.x - pointA.x;
        double baY = pointB.y - pointA.y;
        double caX = center.x - pointA.x;
        double caY = center.y - pointA.y;

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - radius * radius;

        // Circles with same center.
        // Avoids 0 division.
        if (a==0)
        	a = 0.00001;
        
        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0)
        {
            return Collections.emptyList();
        }

        // if disc == 0 ... dealt with later
        double tmpSqrt 			= Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        Point2.Double p1 = new Point2.Double(
            pointA.x - baX * abScalingFactor1,
            pointA.y - baY * abScalingFactor1
        );

        if (disc == 0)
        {
            // abScalingFactor1 == abScalingFactor2
            return Collections.singletonList(p1);
        }

        Point2.Double p2 = new Point2.Double(
                pointA.x - baX * abScalingFactor2,
                pointA.y - baY * abScalingFactor2
        );

        return Arrays.asList(p1, p2);
    }
    
    /**
     * Computes the intersection between two points.
     * @param p1
     * @param p2
     * @return
     */
    public double distance(Point2.Double p1, Point2.Double p2)
    {
        return Math.sqrt(Math.pow(p1.getX()-p2.getX(), 2) + Math.pow(p1.getY()-p2.getY(), 2));
    }
    
    /**
     * Computes the middle of two points.
     * @param p1
     * @param p2
     * @return
     */
    public Point2.Double middle(Point2.Double p1, Point2.Double p2)
    {
        return new Point2.Double((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2);
    }

    /**
     * Auxiliar debug method.
     * @param wp
     */
    public void printPoints(List<WeightedPoint> wp)
    {
        for (WeightedPoint p : wp)
            System.out.print(p.point+", ");

        System.out.println();
    }

    /**
     * Auxiliar debug method.
     * @param wp
     */
    public void printPoints2(List<Point2.Double> points)
    {
        for (Point2.Double p : points)
            System.out.print(p+", ");

        System.out.println();
    }
    
    /**
     * Auxiliar class.
     */
    protected class WeightedPoint
    {
        public double weight;
        public Point2.Double point;
        public WeightedPoint(double weight, Point2.Double point) {
            super();
            this.weight = weight;
            this.point = point;
        }

        public boolean equals(Object o)
        {
            WeightedPoint wp = (WeightedPoint)o;
            return (wp.weight==this.weight && wp.point.equals(this.point));
        }
    }
}
