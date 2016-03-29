package br.com.arivanbastos.jillcore.models.map;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.models.math.Point2;

/**
 * Represents a object in the map.
 *
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.models.map.rooms.Map
 */
public class MapObject 
{	
	// Object id.
    private String id;
    
    // Object data.
    public java.util.Map<String, Object> data;
    
    // Object position in the map.
    private Point2.Double position;    

    @JsonCreator
    public MapObject(
    		@JsonProperty("id")String id,
    		@JsonProperty("data")java.util.Map data, 
    		@JsonProperty("position")Point2.Double position) 
    {
        this.id = id;
        this.data = data;
        this.position = position;
    }
    
    public MapObject(String id, Point2.Double position) 
    {
        this(id, new HashMap<String, Object>(), position);
    }

    public String getId() 
    {
        return id;
    }

    public void setId(String id) 
    {
        this.id = id;
    }

    public Point2.Double getPosition() 
    {
        return position;
    }

    public void setPosition(Point2.Double position) 
    {
        this.position = position;
    }

	public java.util.Map<String, Object> getData() 
	{
		return data;
	}

	public void setData(java.util.Map<String, Object> data) 
	{
		this.data = data;
	}
}