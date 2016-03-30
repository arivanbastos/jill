package br.com.arivanbastos.signalcaptor.sensors.events;

import java.util.HashMap;
import java.util.List;

import br.com.arivanbastos.jillcore.models.signal.SignalSource;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;

public interface ISensorManagerListener {

    // New source available.
    public void onSourceAvailable(SignalSource source);

    // A source turns unavailable.
    public void onSourceUnavailable(SignalSource source);

    // Record proccess begin event.
    public void onRecordBegin();

    // Record process progress event.
    // Fired for each listened signal source.
    public void onRecordProgress(SignalSource source, SignalSample sampleValue, SignalDataSet dataSet);

    // Fired when a single source have done capturing signal data.
    public void onSingleSourceRecordEnd(SignalSource source, SignalDataSet dataSet);

    // Fired when all sources have done capturing signal data.
    public void onAllSourcesRecordEnd(String signalTypeId, List<SignalDataSet> dataSets);
}
