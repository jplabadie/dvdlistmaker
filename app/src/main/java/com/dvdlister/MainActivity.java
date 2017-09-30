package com.dvdlister;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dvdlister.pojos.Items;
import com.dvdlister.pojos.UpcResponse;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView mTextMessage;
    private ListView listView;
    private Button cont_scan_btn, view_data, email_data_btn;
    Hashtable<String,String> titles = new Hashtable<>();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_review:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_send:
                    mTextMessage.setText(R.string.title_notifications);

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
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);

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

        cont_scan_btn = (Button) findViewById(R.id.cont_scan_btn);
        cont_scan_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                IntentIntegrator zxing_itegrator = new IntentIntegrator(activity);
                zxing_itegrator.setDesiredBarcodeFormats( IntentIntegrator.ALL_CODE_TYPES );
                zxing_itegrator.setPrompt("Scan");
                zxing_itegrator.setCameraId(0);
                zxing_itegrator.setBeepEnabled(true);
                zxing_itegrator.setBarcodeImageEnabled(false);
                zxing_itegrator.initiateScan();
            }
        });

        //listView = (ListView) findViewById(R.id.listView);

        view_data = (Button) findViewById(R.id.view_data_btn);
        view_data.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Cursor res = dbHelper.getData();
                if( res.getCount() != 0 ){
                    StringBuffer buffer = new StringBuffer();

                    while(res.moveToNext()){
                        buffer.append("QrCode: " + res.getString(0)+"\n");
                        buffer.append("Title: " + res.getString(1)+"\n");
                        buffer.append("Description: " + res.getString(2)+"\n\n");
                    }

                    showMessage( "Data",buffer.toString() );
                }
            }
        });
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

    /**
     * A placeholder fragment containing a simple view.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private class HttpRequestTask extends AsyncTask<String, Void, UpcResponse> {
        @Override
        protected UpcResponse doInBackground(String... strings) {
            try {
                final String url = "https://api.upcitemdb.com/prod/trial/lookup?upc="+strings[0];
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                UpcResponse response = restTemplate.getForObject(url, UpcResponse.class);
                return response;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(UpcResponse response) {
            Items item = response.getItems()[0];
            dbHelper.updateDvd( item );

            TextView title = (TextView) findViewById(R.id.id_label);
            TextView description = (TextView) findViewById(R.id.content_value);
            TextView upc = (TextView) findViewById(R.id.id_value);

            title.setText(item.getTitle());
            upc.setText(item.getUpc());
            description.setText(item.getDescription());
        }
    }

    public void showMessage( String title, String message ){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if( result.getContents()==null ){

                Toast.makeText(this,"Scanning Complete",Toast.LENGTH_LONG).show();
            }
            else{
                String res = result.getContents();
                HttpRequestTask htt = new HttpRequestTask();
                htt.execute(res);

                dbHelper.addDvd(res);
                Toast.makeText(this,res,Toast.LENGTH_LONG).show();
                final Activity activity = this;
                IntentIntegrator zxing_itegrator = new IntentIntegrator(activity);
                zxing_itegrator.setDesiredBarcodeFormats( IntentIntegrator.ALL_CODE_TYPES );
                zxing_itegrator.setPrompt("Scan");
                zxing_itegrator.setCameraId(0);
                zxing_itegrator.setBeepEnabled(true);
                zxing_itegrator.setBarcodeImageEnabled(false);
                zxing_itegrator.initiateScan();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
