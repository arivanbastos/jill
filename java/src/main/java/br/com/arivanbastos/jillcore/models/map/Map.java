package br.com.arivanbastos.jillcore.models.map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import br.com.arivanbastos.jillcore.models.map.rooms.RectangularRoom;
import br.com.arivanbastos.jillcore.models.map.rooms.Room;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.math.RectD;

/**
 * Represents a 2D map where the location take proccess.
 *  
 * The map can have points and objects. Points are used to
 * store signal data. Objects are references to location process 
 * (ie beacons or access points).
 *
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.models.map.rooms.Map
 */
public class Map
{
	// Map description.
	protected String description;
	
    // Map dimensions in meters.
    protected Double width;
    protected Double length;

    // Background color.
    protected String color = "#EDEDED";

    // How many pixels correspond 1 meter.
    protected float scale;

    // List of rooms.
    protected List<Room> rooms;

    // List of points.
    // Each point stores signal data from offline phase.
    protected List<MapPoint> points;

    // List of objects.
    // Each object represents a reference to location system.
    // IE: proximity beacons
    protected List<MapObject> objects;

    public Map()
    {
        rooms = new ArrayList<Room>();
        points = new ArrayList<MapPoint>();
        objects = new ArrayList<MapObject>();
    }

    @JsonCreator
    public Map(@JsonProperty("w") Double width, @JsonProperty("l") Double length) 
    {
        this.width = width;
        this.length = length;

        rooms = new ArrayList<Room>();
        points = new ArrayList<MapPoint>();
        objects = new ArrayList<MapObject>();
    }
        
    public String getDescription() 
    {
		return description;
	}

	public void setDescription(String description) 
	{
		this.description = description;
	}

	public Double getWidth() 
	{
        return width;
    }

    public void setWidth(Double width) 
    {
        this.width = width;
    }

    public Double getLength() 
    {
        return length;
    }

    public void setLength(Double length) 
    {
        this.length = length;
    }

    public List<Room> getRooms() 
    {
        return rooms;
    }

    public void setRooms(List<Room> rooms) 
    {
        this.rooms = rooms;
    }

    public float getScale() 
    {
        return scale;
    }

    public void setScale(float scale) 
    {
        this.scale = scale;
    }

    public String getColor() 
    {
        return color;
    }

    public void setColor(String color) 
    {
        this.color = color;
    }

    public void addRoom(Room room)
    {
        rooms.add(room);
    }

    public List<MapPoint> getPoints() 
    {
        return points;
    }

    public void addPoint(MapPoint p)
    {
        points.add(p);
    }

    public void setPoints(List<MapPoint> points) 
    {
        this.points = points;
    }

    public List<MapObject> getObjects() 
    {
        return objects;
    }

    public MapObject getObject(String id)
    {
        for (MapObject object : objects)
            if (object.getId().equals(id))
                return object;

        return null;
    }

    public void addObject(MapObject o)
    {
        for (MapObject object : objects)
        {
            // Replaces an existent object.
            if (object.getId().equals(o.getId())) {
                object.setPosition(o.getPosition());
                return;
            }
        }

        objects.add(o);
    }

    public void setObjects(List<MapObject> objects) 
    {
        this.objects = objects;
    }

    public void removeObjectById(String id)
    {
        for (MapObject object : objects)
            if (object.getId().equals(id))
                objects.remove(object);
    }
    
    public Object getObjectData(String objectId, String data)
    {
    	MapObject o = getObject(objectId);
    	
    	if (o==null)
    		return null;
    	
    	return o.getData().get(data);
    }

    /**
     * Converts a coordinate from room space to
     * map space.
     * @return
     */
    public Point2.Double localToGlobal(double x, double y, String roomId)
    {
        for (Room r : rooms)
        {
            if (r.getId().equals(roomId))
            {
                RectangularRoom room = (RectangularRoom)r;
                RectD bounds = room.getBounds();
                return new Point2.Double(bounds.left.floatValue()+x, bounds.top.floatValue()+y);
            }
        }

        return null;
    }

    /**
     * Converts a coordinate from room space to
     * map space.
     * @return
     */
    public Point2.Double localToGlobal(float x, float y, String roomId)
    {
        return localToGlobal(x, y, roomId);
    }

    /**
     * Converts a coordinate from room space to
     * map space.
     * @return
     */
    public Point2.Double localToGlobal(Point2.Double p, String roomId)
    {
        return localToGlobal(p.getX(), p.getY(), roomId);
    }


    /**
     * Returns the point object related to coordinates.
     * If there is no point, creates one.
     * @param x
     * @param y
     * @return
     */
    public MapPoint getPoint(float x, float y)
    {
        MapPoint searched = new MapPoint(x,y);
        for (MapPoint p : points)
        {
            if (searched.equals(p)) {
                return p;
            }
        }

        MapPoint newPoint = new MapPoint(MapPoint.getDefaultLabel(x, y), x, y);
        points.add(newPoint);
        return newPoint;
    }
    
    /**
     * Removes the point object from map.
     * @param p
     */
    public void removePoint(MapPoint p)
    {
        points.remove(p);
    }

    public String toString()
    {
        String roomsStr = "";
        for (Room r : rooms)
            roomsStr += r.toString()+", ";

        String pointsStr = "";
        for (MapPoint p : points)
            pointsStr += p.toString()+", ";

        return "{Width: "+this.width+", "+
               "Length: "+this.length + ", "+
               "Scale: "+this.scale + ", "+
               "Color: "+this.color + ", "+
               "Rooms: ["+roomsStr + "], "+
               "Points: ["+pointsStr+"]}";
    }
}