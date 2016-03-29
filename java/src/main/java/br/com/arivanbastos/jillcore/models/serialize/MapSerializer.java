package br.com.arivanbastos.jillcore.models.serialize;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.map.MapPoint;
import br.com.arivanbastos.jillcore.models.map.rooms.RectangularRoom;
import br.com.arivanbastos.jillcore.models.math.RectD;
import br.com.arivanbastos.jillcore.models.signal.GeoMagneticSignal;
import br.com.arivanbastos.jillcore.models.signal.datasets.BLESignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.datasets.GeoMagneticSignalDataSet;

/**
 * Serializes/deserializes a map object using JSON format.
 * Uses Jackson JSON library.
 * 
 * @author Arivan Bastos <arivanbastos at gmail.com> 
 * @see @url http://github.com/FasterXML/jackson
 * @see @url http://www.studytrails.com/java/json/java-jackson-Serialization-polymorphism.jsp
 */
public class MapSerializer
{
    public MapSerializer() {}

    public Map unserialize(InputStream is)
    {
        try {
            String json = IOUtils.toString(is);
            ObjectMapper mapper = new ObjectMapper();
            Map loaded = mapper.readValue(json, Map.class);

            return loaded;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String serialize(Map map)
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String text = mapper.writeValueAsString(map);

            return text;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "";
    }


    public void test()
    {
        Map map = new Map(20d, 20d);
        map.setScale(10f);
        map.addRoom(new RectangularRoom("quarto", "Quarto", "#B2E6F7", new RectD(5d,0d,5d,3d)));

        MapPoint p1 = new MapPoint(2f, 2f);
        MapPoint p2 = new MapPoint(4f, 4f);

        DoubleSignalDataSet doubleSignalDataSet1 = new GeoMagneticSignalDataSet(GeoMagneticSignal.TYPE);
        doubleSignalDataSet1.addSample(new DoubleSignalSample(40d));
        doubleSignalDataSet1.addSample(new DoubleSignalSample(50d));
        doubleSignalDataSet1.addSample(new DoubleSignalSample(60d));

        DoubleSignalDataSet doubleSignalDataSet2 = new BLESignalDataSet("teste");
        doubleSignalDataSet2.addSample(new DoubleSignalSample(70d));
        doubleSignalDataSet2.addSample(new DoubleSignalSample(80d));
        doubleSignalDataSet2.addSample(new DoubleSignalSample(90d));

        p1.addDataSet(doubleSignalDataSet1);
        p1.addDataSet(doubleSignalDataSet2);

        map.addPoint(p1);
        map.addPoint(p2);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(map);
            System.out.println("MAPJSON (write): "+json);

            Map loaded = mapper.readValue(json, Map.class);
            System.out.println("MAPJSON2 (read): "+loaded);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void test2()
    {
        Map map = new Map(5d, 5d);
        map.setScale(100f);
        map.setColor("#F7EBB2");
        map.addRoom(new RectangularRoom("quarto", "Quarto", "#B2E6F7", new RectD(0d,0d,3.3d,3.3d)));

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(map);
            System.out.println("MAPJSON (write): "+json);

            Map loaded = mapper.readValue(json, Map.class);
            System.out.println("MAPJSON2 (read): "+loaded);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
