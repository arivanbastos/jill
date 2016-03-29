package br.com.arivanbastos.jillcore.fitting.functions;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

/**
 * Represents a a*(x/txPower)^b+c function.
 * Grabbed from stackoverflow.
 * 
 * @see http://stackoverflow.com/questions/11335127/how-to-use-java-math-commons-curvefitter
 * @see https://www.symbolab.com/solver/partial-derivative-calculator
 */
public class PropagationModelFunction implements ParametricUnivariateFunction {
	
	private double txPower;
	public PropagationModelFunction(double txPower)
	{
		this.txPower = txPower;
	}
	
    public double value(double t, double... parameters) {
    	double factor = t/txPower;
        return parameters[0] * Math.pow(factor, parameters[1]) + parameters[2];
    }
    
    // Jacobian matrix of the above. In this case, this is just an array of
    // partial derivatives of the above function, with one element for each parameter.
    public double[] gradient(double t, double... parameters) {
        final double a = parameters[0];
        final double b = parameters[1];
        final double c = parameters[2];
        
        double factor = t/txPower;

        return new double[] {
        	Math.pow(factor, b),
            a * Math.pow(factor, b) * Math.log(factor),
            1
        };
    }
}