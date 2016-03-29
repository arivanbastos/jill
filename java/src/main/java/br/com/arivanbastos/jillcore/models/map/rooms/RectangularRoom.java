package br.com.arivanbastos.jillcore.models.map.rooms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.models.math.RectD;

/**
 * Represents a rectangular room in the map.
 * 
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.models.map.rooms.Room
 */
public class RectangularRoom extends Room {

    private RectD bounds;

    @JsonCreator
    public RectangularRoom(
            @JsonProperty("id")String id,
            @JsonProperty("label")String label,
            @JsonProperty("color")String color,
            @JsonProperty("bounds")RectD bounds) {
        super(id, label, color);
        this.bounds = bounds;
    }

    public RectD getBounds() {
        return bounds;
    }

    public void setBounds(RectD bounds) {
        this.bounds = bounds;
    }

    public String toString()
    {
        return "{(RectangularRoom) "+bounds+"}";
    }
}