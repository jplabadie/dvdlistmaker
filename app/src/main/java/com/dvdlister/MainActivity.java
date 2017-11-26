package com.dvdlister;

import android.Manifest;
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

import com.dvdlister.utils.DatabaseHelper;
import com.dvdlister.utils.JobQueueService;
import com.dvdlister.utils.MovieMap;
import com.dvdlister.utils.NetUtilities;
import com.dvdlister.utils.UserDataHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static DatabaseHelper dbHelper;
    private TextView mTextMessage;
    private ListView listView;
    private Button cont_scan_btn, search_btn, erase_db_btn;

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
                //HttpRequestTitleTask upcite_api = new HttpRequestTitleTask();
                //upcite_api.execute(qrcode);
                Intent intent = new Intent();
                intent.putExtra("current_location",cur_location);
                intent.putExtra("qrcode",qrcode);
                intent.setAction(Intent.ACTION_RUN);
                intent.addCategory(Intent.CATEGORY_DEFAULT);

                JobQueueService.enqueueWork(this,intent);

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