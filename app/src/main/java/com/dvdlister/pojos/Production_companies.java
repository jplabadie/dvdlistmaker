package com.dvdlister.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Jean-Paul on 10/3/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Production_companies
{
    private String id;

    private String name;

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", name = "+name+"]";
    }
}
