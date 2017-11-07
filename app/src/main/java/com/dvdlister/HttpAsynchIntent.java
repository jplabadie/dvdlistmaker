package com.dvdlister;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.dvdlister.pojos.Credits;
import com.dvdlister.pojos.Keywords;
import com.dvdlister.pojos.MovieDetails;
import com.dvdlister.pojos.Results;
import com.dvdlister.pojos.TmdbSearchResponse;
import com.dvdlister.pojos.UpcResponse;
import com.dvdlister.utils.MovieMap;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * Created by Jean-Paul on 11/7/2017.
 */

public class HttpAsynchIntent extends Intent {

    private String cur_location ="";
    private ArrayList<MovieMap> fresh_scans = new ArrayList<>();
    DatabaseHelper dbHelper;

    HttpAsynchIntent( String current_location, String qrcode, Context context ){
        cur_location = current_location;
        dbHelper = new DatabaseHelper(context);

        HttpRequestTitleTask get_title = new HttpRequestTitleTask();
        get_title.execute( qrcode );
    }

    private class HttpRequestTitleTask extends AsyncTask<String, Void, UpcResponse> {
        private String upc;
        @Override
        protected UpcResponse doInBackground(String... strings) {
            upc = strings[0];
            final String url = "https://api.upcitemdb.com/prod/trial/lookup?upc="+upc;
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            try {
                Thread.sleep(1500);
                UpcResponse response = restTemplate.getForObject(url, UpcResponse.class);
                return response;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                if( e.getMessage().contains("429")){
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    return restTemplate.getForObject(url, UpcResponse.class);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(UpcResponse response) {
            try {
                dbHelper.addDvd(upc);
                dbHelper.updateDvdLocation(upc,cur_location);
                dbHelper.updateDvd(response.getItems()[0]);
                String core_title = dbHelper.getCoreTitleByUPC(upc);

                HttpRequestSearchTask search = new HttpRequestSearchTask();
                search.execute(upc, core_title);// call to themoviedb API using core id for genres
            }
            catch (NullPointerException e)
            {
                Log.e("Error",e.getMessage());
                dbHelper.updateDvdLocation(upc,cur_location);
            }
        }
    }

    private class HttpRequestSearchTask extends AsyncTask<String, Void, TmdbSearchResponse> {
        private String title = "";
        private String upc = "";
        @Override
        protected TmdbSearchResponse doInBackground(String... strings) {
            try {
                upc = strings[0];
                title = strings[1];

                final String url = "https://api.themoviedb.org/3/search/movie" +
                        "?api_key=3a18eb07897280fb9c416fe02b7ddac8&language=en-US" +
                        "&query="+ title +
                        "&page=1" +
                        "&include_adult=true";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.getForObject(url, TmdbSearchResponse.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(TmdbSearchResponse response) {
            try{Results res = response.getResults()[0];
                String id = res.getId();
                dbHelper.updateDvd(upc, response );

                HttpRequestCreditsTask moviedb_credits_api = new HttpRequestCreditsTask();
                HttpRequestKeywordsTask moviedb_keywords_api = new HttpRequestKeywordsTask();
                HttpRequestMovieDetailsTask moviedb_details_api = new HttpRequestMovieDetailsTask();
                moviedb_keywords_api.execute(upc,id); // call to themoviedb API using core id for keywords
                moviedb_credits_api.execute(upc,id); // call to themoviedb API using core id for credits
                moviedb_details_api.execute(upc,id);
            }
            catch (ArrayIndexOutOfBoundsException e){
                Log.e("Error",e.getMessage());
            }
        }
    }
    private class HttpRequestCreditsTask extends AsyncTask<String, Void, Credits> {
        private String upc = "";
        private String title ="";
        @Override
        protected Credits doInBackground(String... strings) {
            try {
                upc = strings[0];
                title = strings[1];

                final String url = "https://api.themoviedb.org/3/movie/" + title +
                        "/credits?api_key=3a18eb07897280fb9c416fe02b7ddac8";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.getForObject(url, Credits.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Credits credits) {
            dbHelper.updateDvd( upc,credits );
        }
    }

    private class HttpRequestMovieDetailsTask extends AsyncTask<String, Void, MovieDetails> {
        private String upc = "";
        private String id ="";
        @Override
        protected MovieDetails doInBackground(String... strings) {
            try {
                upc = strings[0];
                id = strings[1];

                final String details_url = "https://api.themoviedb.org/3/movie/" + id +
                        "?api_key=3a18eb07897280fb9c416fe02b7ddac8&language=en-US";
                RestTemplate rest_temp_details = new RestTemplate();
                rest_temp_details.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return rest_temp_details.getForObject(details_url, MovieDetails.class);

            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(MovieDetails movie_details) {
            dbHelper.updateDvd( upc,movie_details );
        }
    }

    private class HttpRequestKeywordsTask extends AsyncTask<String, Void, Keywords> {
        private String upc = "";
        private String id ="";

        @Override
        protected Keywords doInBackground(String... strings) {
            try {
                upc = strings[0];
                id = dbHelper.getTmdbId(upc);

                MovieMap fs = new MovieMap(upc, dbHelper.getCoreTitleByUPC(upc));
                System.out.println("ADD SCAN: " + fs.toString());
                fresh_scans.add(fs);

                final String url = "https://api.themoviedb.org/3/movie/" + id +
                        "/keywords?api_key=3a18eb07897280fb9c416fe02b7ddac8";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.getForObject(url,Keywords.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Keywords keywords) {
            dbHelper.updateDvd( upc,keywords);
        }
    }
}
