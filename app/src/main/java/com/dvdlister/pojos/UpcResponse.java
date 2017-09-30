package com.dvdlister.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Jean-Paul on 9/20/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpcResponse {

    private String total;

    private Items[] items;

    private String code;

    private String offset;

    public String getTotal ()
    {
        return total;
    }

    public void setTotal (String total)
    {
        this.total = total;
    }

    public Items[] getItems ()
    {
        return items;
    }

    public void setItems (Items[] items)
    {
        this.items = items;
    }

    public String getCode ()
    {
        return code;
    }

    public void setCode (String code)
    {
        this.code = code;
    }

    public String getOffset ()
    {
        return offset;
    }

    public void setOffset (String offset)
    {
        this.offset = offset;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [total = "+total+", items = "+items+", code = "+code+", offset = "+offset+"]";
    }
}
