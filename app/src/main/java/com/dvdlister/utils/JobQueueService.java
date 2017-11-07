package com.dvdlister.utils;

/**
 * Created by Jean-Paul on 11/1/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

import com.dvdlister.pojos.Credits;
import com.dvdlister.pojos.Keywords;
import com.dvdlister.pojos.MovieDetails;
import com.dvdlister.pojos.Results;
import com.dvdlister.pojos.TmdbSearchResponse;
import com.dvdlister.pojos.UpcResponse;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * Example implementation of a JobIntentService.
 */
public class JobQueueService extends JobIntentService {
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    private String cur_location ="";
    private String qrcode = "";
    private ArrayList<MovieMap> fresh_scans = new ArrayList<>();
    DatabaseHelper dbHelper ;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, JobQueueService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        dbHelper = new DatabaseHelper(this);
        cur_location=intent.getStringExtra("current_location");
        qrcode = intent.getStringExtra("qrcode");
        Log.i("SimpleJobIntentService", "Executing work: " + intent);
        String label = intent.getStringExtra("label");
        if (label == null) {
            label = intent.toString();
        }
        toast("Executing: " + label);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HttpRequestTitleTask get_title = new HttpRequestTitleTask();
        get_title.execute( qrcode );

        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast("All work complete");
    }

    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText( JobQueueService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
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
                Thread.sleep(1200);
                UpcResponse response = restTemplate.getForObject(url, UpcResponse.class);
                return response;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                if( e.getMessage().contains("429")){
                    try {
                        Thread.sleep(1000);
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