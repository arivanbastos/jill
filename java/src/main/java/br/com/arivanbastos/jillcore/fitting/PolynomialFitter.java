package br.com.arivanbastos.jillcore.fitting;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.arivanbastos.jillcore.fitting.DistanceMeasures;

/**
 * Fits a polynomial function.
 * Uses Apache Commons Math to fit the function. 
 *   
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see https://commons.apache.org/proper/commons-math/
 * @see br.com.arivanbastos.jillcore.CurveFitter
 */
public class PolynomialFitter extends CurveFitter 
{
	private PolynomialFunction polynomial;
	private double[] parameters=null;
	private int degree;
	
	public PolynomialFitter(int degree)
	{
		this.degree = degree;
	}
	
	@JsonCreator
	public PolynomialFitter(@JsonProperty("parameters")double[] parameters)
	{
		this.parameters = parameters;
		
		if (parameters!=null)
			this.polynomial = new PolynomialFunction(parameters);
	}
	
	public PolynomialFitter(DistanceMeasures distanceMeasures, int degree)
	{
		this(degree);
		computeRegression(distanceMeasures);
	}
	
	public void computeRegression(DistanceMeasures distanceMeasures)
	{
		//this.degree = degree;
		WeightedObservedPoints obs = new WeightedObservedPoints();
		for (double xy[] : distanceMeasures.getData())
			obs.add(xy[0], xy[1]);
		
		double[] parameters = PolynomialCurveFitter.create(degree).fit(obs.toList());
		polynomial = new PolynomialFunction(parameters);
	}
		
	public double fit(double x) throws FittingException
	{
		return super.checkDouble(polynomial.value(x));
	}
	
	// ------------------------------

	public double[] getParameters() 
	{
		return parameters;
	}

	public void setParameters(double[] parameters) 
	{
		this.parameters = parameters;
	}
	
	/**
	 * Returns the fitter name.
	 * @return
	 */
	@JsonIgnore
	public String getName() 
	{
		return "PolynomialFitter"+degree;
	}
	
	public PolynomialFitter clone()
	{
		return new PolynomialFitter(parameters);
	}
}