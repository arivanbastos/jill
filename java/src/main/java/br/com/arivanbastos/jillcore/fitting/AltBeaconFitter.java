package br.com.arivanbastos.jillcore.fitting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Altbeacon fitter with default parameters.
 * 
 * @see https://github.com/AltBeacon/android-beacon-library
 * @see br.com.arivanbastos.jillcore.CurveFitter
 */
public class AltBeaconFitter extends CurveFitter {
	
	// Distance estimation parameters.
    private double c1;
    private double c2;
    private double c3;
    
    // Device txPower.
    private double txPower;
	
	public AltBeaconFitter()
	{
		// Default Altbeacon parameters.
		c1 = 0.42093;
		c2 = 6.9476;
		c3 = 0.54992;
	}
	
	public AltBeaconFitter(double txPower)
	{
		this();
		this.txPower = txPower;
	}
	
	@JsonCreator
	public AltBeaconFitter(
		@JsonProperty("txPower")double txPower,
		@JsonProperty("c1")double c1,
		@JsonProperty("c2")double c2,
		@JsonProperty("c3")double c3
			)
	{
		this.txPower = txPower;
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
	}
	
	public AltBeaconFitter(DistanceMeasures distanceMeasures)
	{
		this();		
		computeRegression(distanceMeasures);
	}	
		
	@Override
	public double fit(double x) throws FittingException{
		if (x == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }


        double ratio = x*1.0/txPower;
        double distance;
        if (ratio < 1.0) {
            distance =  Math.pow(ratio,10);
        }
        else {
            distance =  (c1)*Math.pow(ratio,c2) + c3;
        }
        
        return super.checkDouble(distance);
	}
	
	@Override
	public void computeRegression(DistanceMeasures distanceMeasures)
	{
		 txPower = distanceMeasures.getSignal(1.0);
	}	
	
	public AltBeaconFitter clone()
	{
		return new AltBeaconFitter(txPower, c1, c2, c3);
	}
	
	public double getC1() {
		return c1;
	}

	public void setC1(double c1) {
		this.c1 = c1;
	}

	public double getC2() {
		return c2;
	}

	public void setC2(double c2) {
		this.c2 = c2;
	}

	public double getC3() {
		return c3;
	}

	public void setC3(double c3) {
		this.c3 = c3;
	}

	public double getTxPower() {
		return txPower;
	}

	public void setTxPower(double txPower) {
		this.txPower = txPower;
	}
}
