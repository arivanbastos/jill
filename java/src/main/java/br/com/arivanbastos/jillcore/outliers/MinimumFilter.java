package br.com.arivanbastos.jillcore.outliers;

import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MinimumFilter extends Filter  {

	@JsonCreator
	public MinimumFilter(){}

	@Override
	public FilterResult doFilter(DoubleSignalDataSet dataSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public Double getMeasure(DoubleSignalDataSet dataSet) {
		return dataSet.getMinValue();
	}
}