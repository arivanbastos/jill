package br.com.arivanbastos.jillcore.models.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;

/**
 * Jackson deserializer.
 * @author Arivan Bastos <arivanbastos at gmail.com> 
 */
public class DoubleSignalListDeserializer extends JsonDeserializer<List<SignalSample>>
{
    public DoubleSignalListDeserializer()
    {
        super();
    }

    @Override
    public List<SignalSample> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        ObjectCodec oc = p.getCodec();
        JsonNode node = oc.readTree(p);

        ArrayList<SignalSample> samples = new ArrayList<SignalSample>();
        for (int i = 0; i< node.size(); i++)
        {
            JsonNode value = node.get(i);
            
            // Ignores null values.
            if (value.asText().equals("null")) ;
            else
            	samples.add(new DoubleSignalSample(value.asDouble()));
        }

        return samples;
    }
}