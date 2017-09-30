package com.dvdlister.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Jean-Paul on 9/29/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Items
{
    private String model;

    private String weight;

    private String asin;

    private Offers[] offers;

    private String elid;

    private String upc;

    private String size;

    private String ean;

    private String title;

    private String dimension;

    private String color;

    private String description;

    private String[] images;

    private String brand;

    private String lowest_recorded_price;

    private String highest_recorded_price;

    public String getModel ()
    {
        return model;
    }

    public void setModel (String model)
    {
        this.model = model;
    }

    public String getWeight ()
    {
        return weight;
    }

    public void setWeight (String weight)
    {
        this.weight = weight;
    }

    public String getAsin ()
    {
        return asin;
    }

    public void setAsin (String asin)
    {
        this.asin = asin;
    }

    public Offers[] getOffers ()
    {
        return offers;
    }

    public void setOffers (Offers[] offers)
    {
        this.offers = offers;
    }

    public String getElid ()
    {
        return elid;
    }

    public void setElid (String elid)
    {
        this.elid = elid;
    }

    public String getUpc ()
    {
        return upc;
    }

    public void setUpc (String upc)
    {
        this.upc = upc;
    }

    public String getSize ()
    {
        return size;
    }

    public void setSize (String size)
    {
        this.size = size;
    }

    public String getEan ()
    {
        return ean;
    }

    public void setEan (String ean)
    {
        this.ean = ean;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getDimension ()
    {
        return dimension;
    }

    public void setDimension (String dimension)
    {
        this.dimension = dimension;
    }

    public String getColor ()
    {
        return color;
    }

    public void setColor (String color)
    {
        this.color = color;
    }

    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    public String[] getImages ()
    {
        return images;
    }

    public void setImages (String[] images)
    {
        this.images = images;
    }

    public String getBrand ()
    {
        return brand;
    }

    public void setBrand (String brand)
    {
        this.brand = brand;
    }

    public String getLowest_recorded_price ()
    {
        return lowest_recorded_price;
    }

    public void setLowest_recorded_price (String lowest_recorded_price)
    {
        this.lowest_recorded_price = lowest_recorded_price;
    }

    public String getHighest_recorded_price ()
    {
        return highest_recorded_price;
    }

    public void setHighest_recorded_price (String highest_recorded_price)
    {
        this.highest_recorded_price = highest_recorded_price;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [model = "+model+", weight = "+weight+", asin = "+asin+", offers = "+offers+", elid = "+elid+", upc = "+upc+", size = "+size+", ean = "+ean+", title = "+title+", dimension = "+dimension+", color = "+color+", description = "+description+", images = "+images+", brand = "+brand+", lowest_recorded_price = "+lowest_recorded_price+", highest_recorded_price = "+highest_recorded_price+"]";
    }
}