package com.dvdlister.pojos;

/**
 * Created by Jean-Paul on 10/7/2017.
 */

public class OmdbRatings
{
    private String Source;

    private String Value;

    public String getSource ()
    {
        return Source;
    }

    public void setSource (String Source)
    {
        this.Source = Source;
    }

    public String getValue ()
    {
        return Value;
    }

    public void setValue (String Value)
    {
        this.Value = Value;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Source = "+Source+", Value = "+Value+"]";
    }
}

