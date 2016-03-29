package br.com.arivanbastos.jillcore.fitting;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.fitting.DistanceMeasures;

/**
 * Fits a first degree (y=ax+b) function.
 * Uses Apache Commons Math to fit the function. 
 *   
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see https://commons.apache.org/proper/commons-math/
 * @see br.com.arivanbastos.jillcore.CurveFitter
 */
public class SimpleRegressionFitter extends CurveFitter 
{
	private SimpleRegression simpleRegression;
	
	private double slope;
	private double intercept;
	
	public SimpleRegressionFitter() {}
	
	public SimpleRegressionFitter(DistanceMeasures distanceMeasures)
	{
		computeRegression(distanceMeasures);
	}
	
	@JsonCreator
	public SimpleRegressionFitter(
		@JsonProperty("slope")double slope, 
		@JsonProperty("intercept")double intercept)
	{
		this.slope 		= slope;
		this.intercept 	= intercept;
	}
	
	public void computeRegression(DistanceMeasures distanceMeasures)
	{
		simpleRegression = new SimpleRegression(true);		
		for (double xy[] : distanceMeasures.getData())
			simpleRegression.addData(xy[0], xy[1]);
		
		slope 		= simpleRegression.getSlope();
		intercept 	= simpleRegression.getIntercept();
	}
	
	public double fit(double x) throws FittingException
	{
		return super.checkDouble(slope*x + intercept);
	}
	
	// -------------------------------------

	public double getSlope() 
	{
		return slope;
	}

	public void setSlope(double slope) 
	{
		this.slope = slope;
	}

	public double getIntercept() 
	{
		return intercept;
	}

	public void setIntercept(double intercept) 
	{
		this.intercept = intercept;
	}
	
	public SimpleRegressionFitter clone()
	{
		return new SimpleRegressionFitter(slope, intercept);
	}
}