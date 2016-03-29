package br.com.arivanbastos.jillcore.models.math;

/**
 * Represents a circle.
 * @author Arivan Bastos <arivanbastos at gmail.com>
 */
public class Circle
{
	// The center.
	private Point2.Double center;
	
	// The radius.
	private double radius;
	
	public Circle(Point2.Double center, double radius) 
	{
		super();
		this.center = center;
		this.radius = radius;
	}

	public Point2.Double getCenter()
	{
		return center;
	}
	
	public void setCenter(Point2.Double center)
	{
		this.center = center;
	}
	
	public double getRadius() 
	{
		return radius;
	}
	
	public void setRadius(double radius) 
	{
		this.radius = radius;
	}		
	
	public boolean contains(Point2.Double point)
	{
		double dx = point.getX() - center.getX();
		double dy = point.getY() - center.getY();
		return ((dx * dx) + (dy * dy)) <= (radius * radius);
	}
	
	public String toString()
	{
		return "["+center.getX()+","+center.getY()+" "+radius+"]";
	}
}
