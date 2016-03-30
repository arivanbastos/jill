package br.com.arivanbastos.signalcaptor.location.events;

import br.com.arivanbastos.signalcaptor.location.BaseLocationMethod;
import br.com.arivanbastos.jillcore.models.math.Point2;

public interface ILocationMethodListener {

    /**
     * Fired by LocationMethod class when a debug information
     * is available.
     */
    public void onDebugInfoAvailable(String methodName, String message, String color);

    /**
     * Fired by LocationMethod class when a location information
     * is available.
     */
    public void onLocationInfoAvailable(BaseLocationMethod method, Point2.Double currentLocation);
}
