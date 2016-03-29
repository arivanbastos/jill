package br.com.arivanbastos.jillcore.models.map.rooms;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import br.com.arivanbastos.jillcore.models.map.rooms.RectangularRoom;

// Jackson annotations for polymorphic serialization/deserialization.
// http://programmerbruce.blogspot.com.br/2011/05/deserialize-json-with-jackson-into.html
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RectangularRoom.class, name = "rectangular")
})

/**
 * Represents a room, in any format, in the map.
 * @author Arivan Bastos <arivanbastos at gmail.com>
 */
public abstract class Room {

    protected String id;
    protected String label = "Room";
    protected String color = "#D2FCE2";

    public Room(String id, String label, String color) {

        this.id = id;
        this.label = label;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}