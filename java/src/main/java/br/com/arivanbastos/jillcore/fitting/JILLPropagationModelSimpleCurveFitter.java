package br.com.arivanbastos.jillcore.fitting;

import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.fitting.functions.PropagationModelFunction;

/**
 * Fits a a*(x/txPower)^b+c function.
 * Uses Apache Commons Math to fit the function. 
 * 
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see https://commons.apache.org/proper/commons-math/
 * @see br.com.arivanbastos.jillcore.fitting.functions.PropagationModelFunction
 * @see br.com.arivanbastos.jillcore.CurveFitter
 */
public class JILLPropagationModelSimpleCurveFitter extends CurveFitter 
{

	private PropagationModelFunction function;
	private double[] parameters;
	
	public JILLPropagationModelSimpleCurveFitter() {}
	
	@JsonCreator
	public JILLPropagationModelSimpleCurveFitter(@JsonProperty("parameters")double[] parameters) 
	{
		this.parameters = parameters;
	}
	
	public JILLPropagationModelSimpleCurveFitter(DistanceMeasures distanceMeasures)
	{
		computeRegression(distanceMeasures);
	}
	
	public void computeRegression(DistanceMeasures distanceMeasures)
	{
		// txtPower is the signal strengh 1m far away from source.
		double txPower = distanceMeasures.getSignal(1.0);
		double[] guess = {0.42093, 6.9476, 0.54992};
		
		WeightedObservedPoints obs = new WeightedObservedPoints();
		for (double xy[] : distanceMeasures.getData())
			obs.add(xy[0], xy[1]);
		
		function 	= new PropagationModelFunction(txPower);
		parameters 	= SimpleCurveFitter.create(function, guess).fit(obs.toList());
	}

	public double fit(double x) throws FittingException
	{
		return super.checkDouble(function.value(x, parameters));
	}

	// ----------------------------------------

	public double[] getParameters() {
		return parameters;
	}

	public void setParameters(double[] parameters) {
		this.parameters = parameters;
	}
	
	public JILLPropagationModelSimpleCurveFitter clone()
	{
		return new JILLPropagationModelSimpleCurveFitter(parameters);
	}
	
	public String getName()
	{
		return "JILLPMSimpleCurve";
	}
}