package com.dvdlister.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Jean-Paul on 9/29/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Offers
{
    private String shipping;

    private String title;

    private String price;

    private String condition;

    private String merchant;

    private String link;

    private String domain;

    private String updated_t;

    private String list_price;

    private String availability;

    private String currency;

    public String getShipping ()
    {
        return shipping;
    }

    public void setShipping (String shipping)
    {
        this.shipping = shipping;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getPrice ()
    {
        return price;
    }

    public void setPrice (String price)
    {
        this.price = price;
    }

    public String getCondition ()
    {
        return condition;
    }

    public void setCondition (String condition)
    {
        this.condition = condition;
    }

    public String getMerchant ()
    {
        return merchant;
    }

    public void setMerchant (String merchant)
    {
        this.merchant = merchant;
    }

    public String getLink ()
    {
        return link;
    }

    public void setLink (String link)
    {
        this.link = link;
    }

    public String getDomain ()
    {
        return domain;
    }

    public void setDomain (String domain)
    {
        this.domain = domain;
    }

    public String getUpdated_t ()
    {
        return updated_t;
    }

    public void setUpdated_t (String updated_t)
    {
        this.updated_t = updated_t;
    }

    public String getList_price ()
    {
        return list_price;
    }

    public void setList_price (String list_price)
    {
        this.list_price = list_price;
    }

    public String getAvailability ()
    {
        return availability;
    }

    public void setAvailability (String availability)
    {
        this.availability = availability;
    }

    public String getCurrency ()
    {
        return currency;
    }

    public void setCurrency (String currency)
    {
        this.currency = currency;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [shipping = "+shipping+", title = "+title+", price = "+price+", condition = "+condition+", merchant = "+merchant+", link = "+link+", domain = "+domain+", updated_t = "+updated_t+", list_price = "+list_price+", availability = "+availability+", currency = "+currency+"]";
    }
}