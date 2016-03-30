package br.com.arivanbastos.signalcaptor.views;

import br.com.arivanbastos.jillcore.models.map.MapObject;
import br.com.arivanbastos.jillcore.models.map.MapPoint;

public interface IMapViewListener
{
    public void onMapPointTap(MapPoint p);
    public void onObjectTap(MapObject o);
}