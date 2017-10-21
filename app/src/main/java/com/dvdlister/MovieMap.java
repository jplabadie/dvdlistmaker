package com.dvdlister;

import java.util.HashMap;

/**
 * Created by Jean-Paul on 10/14/2017.
 */

public class MovieMap extends HashMap<String, String> {
    private String upc="";
    private String title="";

    MovieMap(String upc, String title){
        this.upc = upc;
        this.title = title;
    }

    String getTitle(){
        return title;
    }

    String getUpc(){
        return upc;
    }

    @Override
    public String toString(){
        return upc+" "+title;
    }
}
