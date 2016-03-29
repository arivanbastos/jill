package br.com.arivanbastos.jillcore.fitting.functions;

public class PowerTrendLine extends OLSTrendLine {
    @Override
    protected double[] xVector(double x) {
        return new double[]{1,Math.log(x)};
    }

    @Override
    protected boolean logY() {return true;}

}