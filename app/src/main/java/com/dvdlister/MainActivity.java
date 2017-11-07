package com.dvdlister;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dvdlister.pojos.Credits;
import com.dvdlister.pojos.Keywords;
import com.dvdlister.pojos.MovieDetails;
import com.dvdlister.pojos.Results;
import com.dvdlister.pojos.TmdbSearchResponse;
import com.dvdlister.pojos.UpcResponse;
import com.dvdlister.utils.JobQueueService;
import com.dvdlister.utils.MovieMap;
import com.dvdlister.utils.NetUtilities;
import com.dvdlister.utils.UserDataHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static DatabaseHelper dbHelper;
    private TextView mTextMessage;
    private ListView listView;
    private Button cont_scan_btn, search_btn, erase_db_btn;
    private JobQueueService job_service;

    static ArrayList<MovieMap> fresh_scans = new ArrayList<>();
    static String cur_location = "";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:
                   // mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_review:
                    //mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_send:
                    //mTextMessage.setText(R.string.title_notifications);

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    String user_email = UserDataHelper.getEmail(getApplicationContext());
                    i.putExtra(Intent.EXTRA_EMAIL  , new String[]{user_email});
                    i.putExtra(Intent.EXTRA_SUBJECT, "Titles Currently in DVD Database");
                    i.putExtra(Intent.EXTRA_TEXT   , "Title list attached.");
                    String filepath = dbHelper.exportData();

                    Uri uri = Uri.parse("file://"+filepath);
                    i.putExtra(Intent.EXTRA_STREAM, uri);
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(MainActivity.this, "There are no email clients " +
                                "installed.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        job_service = new JobQueueService();
        setContentView(R.layout.activity_main);


        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                1
        );
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        final Activity activity = this;

        search_btn = (Button) findViewById(R.id.search_btn) ;
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent( MainActivity.this,SearchDbActivity.class);
                startActivity(intent);
            }
        });

        cont_scan_btn = (Button) findViewById(R.id.cont_scan_btn);
        cont_scan_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                CheckNetworkTask cnt = new CheckNetworkTask();
                cnt.execute("https://www.google.com");
            }
        });

        //listView = (ListView) findViewById(R.id.listView);

        erase_db_btn = (Button) findViewById(R.id.delete_btn);
//        erase_db_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dbHelper.eraseDb();
//            }
//        });
    }

    private class CheckNetworkTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                return NetUtilities.hasActiveInternetConnection( MainActivity.this );
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if(bool) {

                AlertDialog.Builder loc = new AlertDialog.Builder(MainActivity.this);

                final EditText input = new EditText(MainActivity.this);
                loc.setView(input);
                loc.setTitle("Set DVD Location");
                loc.setMessage("Where are these DVD's stored?");
                loc.setPositiveButton("Set",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                cur_location = input.getText().toString();
                                IntentIntegrator zxing_itegrator = new IntentIntegrator(MainActivity.this);
                                zxing_itegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                                zxing_itegrator.setPrompt("Scan");
                                zxing_itegrator.setOrientationLocked(false);
                                zxing_itegrator.setCameraId(0);
                                zxing_itegrator.setBeepEnabled(true);
                                zxing_itegrator.setBarcodeImageEnabled(false);
                                zxing_itegrator.initiateScan();}
                        });
                loc.show();
            }
            else{
                Toast.makeText(MainActivity.this, "You must maintain an active internet connection" +
                        " when using this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.view_data_btn) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    /**
     *
     * @param title
     * @param message
     */
    public void showMessage( String title, String message ){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if( result.getContents()==null ){
                //Toast the user, then allow them to review the scan results

                Toast.makeText(this,"Scanning Complete",Toast.LENGTH_LONG).show();
                Toast.makeText(this,"Making Internet Data Requests...",Toast.LENGTH_LONG).show();
                super.onActivityResult(requestCode, resultCode, data);
            }
            else{
                String qrcode = result.getContents();
                HttpRequestTitleTask upcite_api = new HttpRequestTitleTask();
                upcite_api.execute(qrcode);

                Toast.makeText(this,qrcode,Toast.LENGTH_LONG).show();
                IntentIntegrator zxing_itegrator = new IntentIntegrator(this);
                zxing_itegrator.setDesiredBarcodeFormats( IntentIntegrator.ALL_CODE_TYPES );
                zxing_itegrator.setPrompt("Scan");
                zxing_itegrator.setOrientationLocked(false);
                zxing_itegrator.setCameraId(0);
                zxing_itegrator.setBeepEnabled(true);
                zxing_itegrator.setBarcodeImageEnabled(false);
                zxing_itegrator.initiateScan();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}