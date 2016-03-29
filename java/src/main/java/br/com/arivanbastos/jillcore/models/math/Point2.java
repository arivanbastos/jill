package br.com.arivanbastos.jillcore.models.math;

import br.com.arivanbastos.jillcore.models.map.MapPoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a 2D point (float and double formats).
 * @author Arivan Bastos <arivanbastos at gmail.com>
 */
public interface Point2 
{
    public class Double
    {
        public double x;
        public double y;

        public Double()
        {
        }

        @JsonCreator
        public Double(@JsonProperty("x")double x, @JsonProperty("y")double y) 
        {
            this.x = x;
            this.y = y;
        }

        public double getX() 
        {
            return x;
        }

        public void setX(double x) 
        {
            this.x = x;
        }

        public double getY()
        {
            return y;
        }

        public void setY(double y) 
        {
            this.y = y;
        }
        
        public String toString()
        {
        	return "("+x+","+y+")";
        }
    }
    
    public class Float
    {
        public float x;
        public float y;

        public Float()
        {
        }

        @JsonCreator
        public Float(@JsonProperty("x")float x, @JsonProperty("y")float y) 
        {
            this.x = x;
            this.y = y;
        }

        public float getX() 
        {
            return x;
        }

        public void setX(float x) 
        {
            this.x = x;
        }

        public float getY() 
        {
            return y;
        }

        public void setY(float y) 
        {
            this.y = y;
        }
        
        public String toString()
        {
        	return "("+x+","+y+")";
        }
        
        @Override
        public boolean equals(Object o)
        {
        	Point2.Float p = (Point2.Float)o;
        	
            //return (p.getY()==this.getY()) && (p.getX()==this.getX());
            float distance = (float) Math.sqrt(Math.pow(p.getX()-this.getX(), 2) +Math.pow(p.getY()-this.getY(), 2));
            
            // Points with distance lesser then 1cm are
            // considered the same.
            return distance<0.01;
        }
    }
}