package br.com.arivanbastos.jillcore.outliers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import br.com.arivanbastos.jillcore.models.signal.datasets.DoubleSignalDataSet;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HampelFilter.class	, name = "HampelFilter"),
        @JsonSubTypes.Type(value = IQRFilter.class		, name = "IQRFilter"),
        @JsonSubTypes.Type(value = MaximumFilter.class	, name = "MaximumFilter"),
        @JsonSubTypes.Type(value = MedianFilter.class	, name = "MedianFilter"),
        @JsonSubTypes.Type(value = PercentFilter.class	, name = "PercentFilter"),
})
public abstract class Filter {
	
	/**
	 * Returns the filter name.
	 * @return
	 */
	@JsonIgnore
	public String getName() {
		return this.getClass().getSimpleName().replace("Filter", "");
	}
	
	/**
	 * Filters the dataset, removing identified outliers.
	 * Deprecated: use getMeasure instead.
	 * 
	 * This method DOES CHANGE the data set.
	 * 
	 * @see getMeasure 
	 * @param dataSet
	 * @return
	 */
	@Deprecated 
    public abstract FilterResult doFilter(DoubleSignalDataSet dataSet);
	
	/**
	 * Returns an reliable median/averaged value from
	 * the datasource. This values can be typically used
	 * to estimate the distance from the sender.
	 * 
	 * The value is computed from reliable measures present
	 * in dataset. In other words: the values not filtered out
	 * by filter are used to compute the measure.
	 * 
	 * This method DOES NOT CHANGE the data set.
	 * 
	 * @param dataSet
	 * @return
	 */
	public abstract Double getMeasure(DoubleSignalDataSet dataSet);
	
	public Double avoidEmptyResult(DoubleSignalDataSet resultDataSet, DoubleSignalDataSet originalDataSet)
	{
		return resultDataSet.isEmpty()?originalDataSet.getAverage():resultDataSet.getAverage();
	}
	
	public String toString()
	{
		return getName();
	}
}