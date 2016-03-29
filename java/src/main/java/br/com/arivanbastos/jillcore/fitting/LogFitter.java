package br.com.arivanbastos.jillcore.fitting;

import br.com.arivanbastos.jillcore.fitting.functions.LogTrendLine;
import br.com.arivanbastos.jillcore.fitting.functions.TrendLine;

/**
 * Fits a logarithm function.
 * Uses Apache Commons Math to fit the function. 
 *   
 * @author Arivan Bastos <arivanbastos at gmail.com>
 * @see https://commons.apache.org/proper/commons-math/
 * @see br.com.arivanbastos.jillcore.CurveFitter
 */
public class LogFitter extends CurveFitter {

	double txPower;
	TrendLine t;
	
	public LogFitter() {}
	
	public LogFitter(DistanceMeasures distanceMeasures)
	{
		computeRegression(distanceMeasures);
	}
	
	public void computeRegression(DistanceMeasures distanceMeasures)
	{
		txPower = distanceMeasures.getSignal(1.0);
		
		//System.out.println("txPower: "+txPower);
		
		double[][] sampleData = new double[distanceMeasures.getData().length][2];				
		
		int i = 0;
		for (double xy[] : distanceMeasures.getData())
		{			
			// Some sensor values contains errors.
			if (xy[0]>0)
				continue;
			
			//System.out.println((xy[0]/txPower)+","+xy[1]+ " ("+xy[0]+")");
			sampleData[i][0] = xy[0]/txPower;
			sampleData[i][1] = xy[1];
			i++;
		}
			
		t = new LogTrendLine();
		t.setValues(sampleData);
	}
	
	public double fit(double x) throws FittingException
	{
		//System.out.println("fit: "+(x/txPower)+"="+t.predict(x/txPower));
		return super.checkDouble(t.predict(x/txPower));	
	}
	
	public LogFitter clone()
	{
		System.err.println("[LogFitter] clone() is not implemented!");
		return null;
	}
}