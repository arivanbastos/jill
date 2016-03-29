package br.com.arivanbastos.jillcore.outliers;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.arivanbastos.jillcore.models.signal.SignalSample;
import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;

public class MedianFilter extends Filter {

	@JsonCreator
	public MedianFilter(){}
	
	@Override
	public FilterResult doFilter(DoubleSignalDataSet dataSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public Double getMeasure(DoubleSignalDataSet dataSet) {
		return dataSet.getMedian();
	}
}
