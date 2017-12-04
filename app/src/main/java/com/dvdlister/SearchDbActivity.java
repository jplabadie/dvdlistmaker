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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dvdlister.utils.DatabaseHelper;
import com.dvdlister.utils.SearchArrayAdapter;
import com.dvdlister.utils.UserDataHelper;
import com.nex3z.flowlayout.FlowLayout;

import java.util.ArrayList;

/**
 * Created by Jean-Paul on 10/23/2017.
 */

public class SearchDbActivity extends Activity {
    private static DatabaseHelper dbHelper;
    private ListView lv;
    private RelativeLayout sv;
    private FlowLayout genre_buttons;
    private EditText search_text;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent home = new Intent( SearchDbActivity.this,MainActivity.class);
                    startActivity(home);
                    finish();
                    return true;
                case R.id.navigation_scan:
                    Intent main = new Intent( SearchDbActivity.this,MainActivity.class);
                    main.putExtra("start","scan");
                    startActivity(main);
                    finish();
                    return true;
                case R.id.navigation_search:
                    return true;
                case R.id.navigation_send:
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

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_db);

        lv = (ListView) findViewById(R.id.search_results);
        sv = (RelativeLayout) findViewById(R.id.search_db_lin);
        genre_buttons = (FlowLayout) findViewById(R.id.genre_layout);
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
        navigation.setSelectedItemId(R.id.navigation_search);


        final SearchArrayAdapter list_adapter = new SearchArrayAdapter(this,
                dbHelper.getTitleAndLocationAsList(),sv);
        lv.setAdapter(list_adapter);
        updateGenreButtons();
        search_text = new EditText(this);
        lv.requestFocus();
    }

    protected void updateGenreButtons(){
        ArrayList<String> genres = dbHelper.getGenres();
        Button all = new Button(this);
        all.setText((String)"any");
        all.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view){
                   displayByGenre("any");
               }
        });
        genre_buttons.addView(all);
        for(String g: genres){
            final Button button = new Button(this);
            button.setText(g);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                  displayByGenre((String) button.getText());
                }
            });
            genre_buttons.addView(button);
        }
    }

    protected void displayByGenre( String genre ){
       SearchArrayAdapter list_adapter;
        if( genre.equalsIgnoreCase("any")){
            list_adapter = new SearchArrayAdapter(this,
            dbHelper.getTitleAndLocationAsList(),sv);
        }
        else{
            list_adapter = new SearchArrayAdapter(this,
            dbHelper.getTitleAndLocationAsList(genre),sv);
        }
        lv.setAdapter(list_adapter);
    }
}
