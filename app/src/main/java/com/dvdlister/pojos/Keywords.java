package com.dvdlister.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Jean-Paul on 10/6/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Keywords
{
    private String id;

    private Words[] words;

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public Words[] getKeywords ()
    {
        return words;
    }

    public void setKeywords (Words[] words)
    {
        this.words = words;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", keywords = "+words+"]";
    }
}