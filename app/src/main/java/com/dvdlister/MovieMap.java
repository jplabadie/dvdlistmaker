package com.dvdlister;

/**
 * Created by Jean-Paul on 10/14/2017.
 */

public class MovieTuple {
    private String upc="";
    private String title="";

    MovieTuple (String upc, String title){
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
