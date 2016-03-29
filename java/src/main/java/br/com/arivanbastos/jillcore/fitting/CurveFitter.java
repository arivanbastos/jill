package br.com.arivanbastos.jillcore.fitting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AltBeaconFitter.class, name = "AltBeaconFitter"),
        @JsonSubTypes.Type(value = GaussianFitter.class, name = "JILLGaussianCurveFitter"),
        @JsonSubTypes.Type(value = LogFitter.class, name = "JILLLogCurveFitter"),
        @JsonSubTypes.Type(value = PolynomialFitter.class, name = "JILLPolynomialCurveFitter"),
        @JsonSubTypes.Type(value = JILLPropagationModelSimpleCurveFitter.class, name = "JILLPropagationModelSimpleCurveFitter"),
        @JsonSubTypes.Type(value = SimpleRegressionFitter.class, name = "JILLSimpleRegressionCurveFitter"),
})

/**
 * Represents a class that, given a set of pairs (x,y) 
 * (called sample points) can infer a new x from a new y. 
 * In most cases this class will make a curve fitting process.
 * 
 * @author Arivan Bastos <arivanbastos at gmail.com>
 */
public abstract class CurveFitter {
		
	/**
	 * Fits (predicts) a value.
	 * This method should be called after computeRegression() method, 
	 * cause it computes the function from sample points.
	 * 
	 * @see setDistanceMeasures
	 * @param x
	 * @return
	 */
	public abstract double fit(double x) throws FittingException;
	
	/**
	 * Computes the regressed function given a set of
	 * distances/measures (x,y values).
	 * 
	 * @param distanceMeasures
	 * @throws Exception
	 */
	public abstract void computeRegression(DistanceMeasures distanceMeasures) throws Exception;
	
	/**
	 * Makes a copy of this object.
	 */
	public abstract CurveFitter clone();
	
	/**
	 * Returns the fitter name.
	 * @return
	 */
	@JsonIgnore
	public String getName() 
	{
		return this.getClass().getSimpleName().replace("Fitter", "");
	}
	
	/**
	 * Auxiliary method to check if a valid result was obtained.
	 * @param r Value to be checked.
	 * @return The same value provided.
	 * @throws FittingException
	 */
	public double checkDouble(double r) throws FittingException
	{
		if (Double.isNaN(r) || Double.isInfinite(r))
			throw new FittingException();
		
		return r;
	}

	/**
	 * toString()
	 */
	public String toString()
	{
		return getName();
	}
}