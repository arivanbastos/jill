package br.com.arivanbastos.jillcore.utils;

import br.com.arivanbastos.jillcore.models.math.Point2;

public class MathUtil {

    public static float distance(Point2.Double p1, Point2.Double p2)
    {
        return (float) Math.sqrt(Math.pow(p1.getX()-p2.getX(), 2) +Math.pow(p1.getY()-p2.getY(), 2));
    }
}
