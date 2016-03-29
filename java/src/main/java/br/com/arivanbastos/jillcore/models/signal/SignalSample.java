package br.com.arivanbastos.jillcore.models.signal;

/*
// Jackson annotations for polymorphic serialization/deserialization.
// http://programmerbruce.blogspot.com.br/2011/05/deserialize-json-with-jackson-into.html
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DoubleSignalSample.class, name = "double"),
})
*/
public abstract class SignalSample implements Comparable
{
    protected Object value;
    /*
    @JsonCreator
    public SignalSample(@JsonProperty("value")Object value) {
        this.value = value;
    }
    */
    public SignalSample(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString()
    {
        return "{Value: "+value+"}";
    }

    public boolean equals(Object o)
    {
        SignalSample other = (SignalSample)o;

        return other.getValue().equals(this.getValue());
    }
}
