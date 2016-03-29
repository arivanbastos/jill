package br.com.arivanbastos.jillcore.models.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;

/**
 * Jackson serializer.
 * @author Arivan Bastos <arivanbastos at gmail.com> 
 */
public class DoubleSignalListSerializer extends JsonSerializer<List<SignalSample>> {

    public DoubleSignalListSerializer()
    {
        super();
    }

    @Override
    public void serialize(List<SignalSample> object, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartArray();
        for (SignalSample s : object) {
        	if (s==null)
        	{
        		// Ignores null values.
        	}
        	else
        	{
        		DoubleSignalSample d = (DoubleSignalSample) s;
        		gen.writeNumber(d.getValue());
        	}
        }
        gen.writeEndArray();
    }
}