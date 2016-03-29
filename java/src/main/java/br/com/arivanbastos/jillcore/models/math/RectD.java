package br.com.arivanbastos.jillcore.models.math;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a rectangle of doubles.
 * @author Arivan Bastos <arivanbastos at gmail.com>
 */
public class RectD 
{
    public Double top;
    public Double left;
    public Double right;
    public Double bottom;

    @JsonCreator
    public RectD(
            @JsonProperty("l")Double left,
            @JsonProperty("t")Double top,
            @JsonProperty("r") Double right,
            @JsonProperty("b") Double bottom) 
    {
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }

    @JsonIgnore
    public Double getWidth()
    {
        return Math.abs(this.left-this.right);
    }

    @JsonIgnore
    public Double getLength()
    {
        return Math.abs(this.top-this.bottom);
    }

    public String toString()
    {
        return left+", "+top+", "+right+", "+bottom;
    }
}
