package br.com.arivanbastos.signalcaptor.location;

public class LocationMethodParameter {

    public static int TYPE_INT = 0;

    private String name;
    private int type;
    private Object defaultValue;

    public LocationMethodParameter(String name, int type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
