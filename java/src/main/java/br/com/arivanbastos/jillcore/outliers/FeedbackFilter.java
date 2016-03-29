package br.com.arivanbastos.jillcore.outliers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import br.com.arivanbastos.jillcore.fitting.AltBeaconFitter;
import br.com.arivanbastos.jillcore.fitting.GaussianFitter;
import br.com.arivanbastos.jillcore.fitting.JILLPropagationModelSimpleCurveFitter;
import br.com.arivanbastos.jillcore.fitting.LogFitter;
import br.com.arivanbastos.jillcore.fitting.PolynomialFitter;
import br.com.arivanbastos.jillcore.fitting.SimpleRegressionFitter;
import br.com.arivanbastos.jillcore.models.signal.DoubleSignalSample;
import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;

public class FeedbackFilter extends Filter
{
	// Weights. The values in this array should
	// sum 1 and this array size will define how many previous
	// measures will be used in the algorithm.
	private Double[] weights;
	
	@JsonCreator
	public FeedbackFilter(@JsonProperty("weights")Double[] weights)
	{
		super();
		this.weights = weights;
	}

	@Override
	public FilterResult doFilter(DoubleSignalDataSet dataSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public Double getMeasure(DoubleSignalDataSet dataSet) {
		int samplesCount = 0;
		List<Double> values = new ArrayList<Double>();
		
		for (SignalSample sample : dataSet.getSamples())
		{
			DoubleSignalSample dSample = (DoubleSignalSample) sample;
			if (sample.getValue()!=null)
			{				
				values.add(dSample.getValue());
				samplesCount++;
				
				if (samplesCount>=weights.length)
					break;
			}			
		}
		
		double sum=0.0d;
		int length = samplesCount>weights.length?weights.length:samplesCount;
		for (int i = 0; i < length; i++)
			sum += weights[i];
		
		double result=0.0d;
		for (int i = 0; i < length; i++)
			result += (weights[i]/sum) * values.get(i);
		
		return result==0?dataSet.getAverage():result;
	}
	
	public Double[] getWeights() {
		return weights;
	}

	public void setWeights(Double[] weights) {
		this.weights = weights;
	}
}