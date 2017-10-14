package com.dvdlister;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Jean-Paul on 9/20/2017.
 */

public class ReviewActivity extends AppCompatActivity{

    private ListView elv;
    private AutoCompleteTextView et;
    private Button et_button;
    private ArrayList<String> result_list = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private TextView mTextMessage;

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
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        et = (AutoCompleteTextView) findViewById(R.id.editLocationText);
        et_button = (Button) findViewById(R.id.et_button);
        dbHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_review);

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        elv = (ListView) findViewById(R.id.item_list);
        final ArrayList<MovieTuple> scans =
                (ArrayList<MovieTuple>) getIntent().getExtras().get("FreshScans");

        ListAdapter list_adapter =  new ArrayAdapter<MovieTuple>
                (this,android.R.layout.simple_list_item_1,scans);
        elv.setAdapter(list_adapter);

        et_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et.setEnabled(false);
                String location = et.getText().toString();
                for(MovieTuple mt : scans){
                    dbHelper.updateDvdLocation(mt.getUpc(),location);
                }
                Toast.makeText(ReviewActivity.this, "Location Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
