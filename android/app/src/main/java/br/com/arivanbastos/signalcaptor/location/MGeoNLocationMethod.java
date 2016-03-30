package br.com.arivanbastos.signalcaptor.location;

import java.util.HashMap;
import java.util.List;

import br.com.arivanbastos.jillcore.location.BaseLocationAlgorithm;
import br.com.arivanbastos.jillcore.location.MGeoNLocationAlgorithm;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.signal.datasets.SignalDataSet;
import br.com.arivanbastos.signalcaptor.location.exceptions.InvalidParameterValueException;

public class MGeoNLocationMethod extends GeoNLocationMethod {

    private long weightFactor = 1;

    public String getName() {
        return "M-GeoN";
    }

    @Override
    public List<LocationMethodParameter> getParameters() {
        List<LocationMethodParameter> result = super.getParameters();
        result.add(new LocationMethodParameter("Weight Factor", LocationMethodParameter.TYPE_INT, 1));
        return result;
    }

    @Override
    public void setParameterValue(String parameterName, String value)
            throws InvalidParameterValueException {
        if (parameterName.equals("Weight Factor")) {
            try {
                this.weightFactor = Long.parseLong(value);
            } catch (Exception e) {
                throw new InvalidParameterValueException();
            }
        }
        else super.setParameterValue(parameterName, value);
    }

    public String getParameterValue(String parameterName)
    {
        if (parameterName.equals("Weight Factor")) {
            return weightFactor+"";
        }

        return super.getParameterValue(parameterName);
    }

    @Override
    public void onAllSourcesRecordEnd(String signalTypeId, List<SignalDataSet> dataSets)
    {
        debug("Samples collection finished.");

        BaseLocationAlgorithm algorithm = new MGeoNLocationAlgorithm(map, weightFactor);
        Point2.Double result = algorithm.run(dataSets);

        super.onLocationEnd(result);
    }
}
