package br.com.arivanbastos.signalcaptor.views.map;

import android.graphics.Color;
import android.graphics.Paint;

import br.com.arivanbastos.signalcaptor.views.MapView;

/**
 * Created by Administrador on 22/06/2015.
 */
public class Circle extends Shape{
    public float x, y, radius;
    public int color;

    public Circle(float x, float y, float radius, int color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void drawn(MapView map) {
        map.getMapPaint().setStyle(Paint.Style.STROKE);
        map.getMapPaint().setColor(color);
        map.getMapCanvas().drawCircle(x, y, radius, map.getMapPaint());
    }
}
