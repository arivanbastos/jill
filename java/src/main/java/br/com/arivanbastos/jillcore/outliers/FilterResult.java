package br.com.arivanbastos.jillcore.outliers;

import java.util.List;

public class FilterResult 
{	
	private List<Double> outliers;
    private List<Double> nonOutliers;

	public FilterResult(List<Double> outliers, List<Double> nonOutliers)
	{	
		this.outliers = outliers;
		this.nonOutliers = nonOutliers;
	}
	
	public List<Double> getOutliers() 
	{
		return outliers;
	}
	
	public void setOutliers(List<Double> outliers) 
	{
		this.outliers = outliers;
	}
	
	public List<Double> getFilteredDataSet()
	{
		return nonOutliers;
	}
	
	public void setFilteredDataSet(List<Double> filteredDataSet)
	{
		this.nonOutliers = filteredDataSet;
	}
}