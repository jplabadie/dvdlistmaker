package com.dvdlister;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dvdlister.utils.DatabaseHelper;
import com.dvdlister.utils.UserDataHelper;

/**
 * Created by Jean-Paul on 10/23/2017.
 */

public class SearchDbActivity extends Activity {

    private static DatabaseHelper dbHelper;
    private ListView lv;

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
                        Toast.makeText(SearchDbActivity.this, "There are no email clients " +
                                "installed.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
            return false;
        }

    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_db);

        lv = (ListView) findViewById(R.id.search_results);
        dbHelper = new DatabaseHelper(this);

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,
                dbHelper.getTitleAndLocationAsList());
        lv.setAdapter(adapter);
    }
}
