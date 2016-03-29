package br.com.arivanbastos.jillcore.fitting;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.fitting.DistanceMeasures;

/**
 * Fits a gaussian function.
 * Uses Apache Commons Math to fit the function. 
 * 
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see https://commons.apache.org/proper/commons-math/
 * @see br.com.arivanbastos.jillcore.CurveFitter
 */
public class GaussianFitter extends CurveFitter {

	final int MAX_ITERATIONS = 1000000;
	
	private Gaussian gaussian=null;
	private double[] parameters=null;
	
	public GaussianFitter() {}
	
	@JsonCreator
	public GaussianFitter(@JsonProperty("parameters")double[] parameters) throws Exception
	{
		this.parameters = parameters;
		
		if (parameters!=null)
			gaussian = new Gaussian(parameters[0], parameters[1], parameters[2]);
	}
	
	public GaussianFitter(DistanceMeasures distanceMeasures) throws Exception
	{
		computeRegression(distanceMeasures);
	}
	
	public void computeRegression(DistanceMeasures distanceMeasures)
			throws Exception
	{
		WeightedObservedPoints obs = new WeightedObservedPoints();
		//String points = "";
		for (double xy[] : distanceMeasures.getData())
		{
			//points += "("+xy[0]+","+xy[1]+")";
			obs.add(xy[0], xy[1]);
		}
		
		// The results will be norm, mean, sigma, where norm will be your amplitude.
		try
		{
			parameters 	= GaussianCurveFitter.create().withMaxIterations(MAX_ITERATIONS).fit(obs.toList());
			//System.out.println(Arrays.toString(parameters));
			gaussian 	= new Gaussian(parameters[0], parameters[1], parameters[2]);
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
	public double fit(double x) throws FittingException
	{
		return super.checkDouble(gaussian.value(x));
	}

	public double[] getParameters() {
		return parameters;
	}

	public void setParameters(double[] parameters) {
		this.parameters = parameters;
	}
	
	public GaussianFitter clone()
	{
		try
		{
			return new GaussianFitter(parameters);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;		
	}
}