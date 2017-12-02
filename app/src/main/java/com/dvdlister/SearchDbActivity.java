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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.dvdlister.utils.DatabaseHelper;
import com.dvdlister.utils.UserDataHelper;

import java.util.ArrayList;

/**
 * Created by Jean-Paul on 10/23/2017.
 */

public class SearchDbActivity extends Activity {
    private static DatabaseHelper dbHelper;
    private ListView lv;
    private LinearLayout genre_buttons;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent home = new Intent( SearchDbActivity.this,MainActivity.class);
                    startActivity(home);
                    return true;
                case R.id.navigation_scan:
                    MainActivity main = new MainActivity();
                    main.checkNetAndStartScan();
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_db);

        lv = (ListView) findViewById(R.id.search_results);
        genre_buttons = (LinearLayout) findViewById(R.id.genre_layout);
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

        ArrayAdapter<String> title_adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,
                dbHelper.getTitleAndLocationAsList());
        lv.setAdapter(title_adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SearchDbActivity.this, "Click!", Toast.LENGTH_SHORT).show();
                PopupMenu pm = new PopupMenu(SearchDbActivity.this, lv.getChildAt(position));
                pm.getMenuInflater().inflate(R.menu.edit_db_item, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle() == "edit") {
                            Toast.makeText(SearchDbActivity.this, "EDIT", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        return true;
                    }
                });
                pm.show();
            }
        });
        updateGenreButtons();
    }

    protected void updateGenreButtons(){
        ArrayList<String> genres = dbHelper.getGenres();
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
        ArrayAdapter<String> title_adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,
                dbHelper.getTitleAndLocationAsList(genre));
        lv.setAdapter(title_adapter);
    }
}
