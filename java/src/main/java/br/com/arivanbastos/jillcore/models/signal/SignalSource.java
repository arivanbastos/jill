package br.com.arivanbastos.jillcore.models.signal;

public class SignalSource {
    private String label;
    private String id;
    private String signalType;

    public SignalSource(String label, String id, String signalType) {
        this.label = label;
        this.id = id;
        this.signalType = signalType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }
}