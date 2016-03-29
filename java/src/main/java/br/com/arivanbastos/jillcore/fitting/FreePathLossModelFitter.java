package br.com.arivanbastos.jillcore.fitting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Fitter based on classical PathLoss model:
 * 
 * 	  PL = PL(d0) + 10.N.log(d/d0)
 * 
 * N is a constant:
 *   Free Space 2
 *   In-building line-of-sight  1.6 to 1.8
 *   Obstructed in building  4 to 6
 *   Ostructed in factories  2 to 3
 *   
 * Articles:
 *    Evaluation of the realiabilit of RSSI for Indoor Localization (formula reference)
 *    Dynamic path-loss estimation using a particle filter (formula reference)
 *    Indoor propagation modeling at 2.4GHZ for iEEE 802.11 networls (N value reference)
 *    
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see br.com.arivanbastos.jillcore.CurveFitter
 */
public class FreePathLossModelFitter extends CurveFitter {

	double txPower = -54;
	double n = 2;
	
	public FreePathLossModelFitter() {}
	
	public FreePathLossModelFitter(double n)
	{
		this.n = n;
	}
	
	@JsonCreator
	public FreePathLossModelFitter(
		@JsonProperty("txPower")double txPower, 
		@JsonProperty("n")double n)
	{
		this.txPower = txPower;
		this.n = n;
	}
	
	public FreePathLossModelFitter(DistanceMeasures distanceMeasures, double n)
	{
		this(n);
		
		computeRegression(distanceMeasures);
	}
	
	@Override
	public double fit(double x) throws FittingException{
		double exp = (x + Math.abs(txPower))/(-1*n*10);
	    return super.checkDouble(Math.pow(10.0, exp));
	}

	@Override
	public void computeRegression(DistanceMeasures distanceMeasures)
	{
		 txPower = distanceMeasures.getSignal(1.0);		
	}
	
	// ----------------------------------------

	public double getTxPower() 
	{
		return txPower;
	}

	public void setTxPower(double txPower) 
	{
		this.txPower = txPower;
	}

	public double getN() 
	{
		return n;
	}

	public void setN(double n) 
	{
		this.n = n;
	}	

	@JsonIgnore
	public String getName() 
	{
		return "FreePathLoss"+n;
	}
	
	public FreePathLossModelFitter clone()
	{
		return new FreePathLossModelFitter(txPower, n);
	}
}
