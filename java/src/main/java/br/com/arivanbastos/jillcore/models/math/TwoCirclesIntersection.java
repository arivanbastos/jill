package br.com.arivanbastos.jillcore.models.math;

/**
 * Represents a intersection between two circles.
 * @author Arivan Bastos <arivanbastos at gmail.com>
 */
public class TwoCirclesIntersection
{
	private Point2.Double p1;
	private Point2.Double p2;
	
	public TwoCirclesIntersection(Point2.Double p1, Point2.Double p2)
	{
		super();
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public Point2.Double getP1()
	{
		return p1;
	}
	
	public void setP1(Point2.Double p1)
	{
		this.p1 = p1;
	}
	
	public Point2.Double getP2()
	{
		return p2;
	}
	
	public void setP2(Point2.Double p2)
	{
		this.p2 = p2;
	}
	
	public String toString()
	{
		return "["+(p1!=null?p1.getX()+","+p1.getY():"---")+"; "+(p2!=null?p2.getX()+","+p2.getY():"---")+"]";
	}
}