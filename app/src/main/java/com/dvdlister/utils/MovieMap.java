package com.dvdlister.utils;

import java.util.HashMap;

/**
 * Created by Jean-Paul on 10/14/2017.
 */

public class MovieMap extends HashMap<String, String> {
    private String upc="";
    private String title="";

    public MovieMap(String upc, String title){
        this.upc = upc;
        this.title = title;
    }

    String getTitle(){
        return title;
    }

    public String getUpc(){
        return upc;
    }

    @Override
    public String toString(){
        return upc+" "+title;
    }
}
