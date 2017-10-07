package com.dvdlister;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.dvdlister.pojos.Cast;
import com.dvdlister.pojos.Credits;
import com.dvdlister.pojos.Genres;
import com.dvdlister.pojos.Items;
import com.dvdlister.pojos.Keywords;
import com.dvdlister.pojos.MovieDetails;
import com.dvdlister.pojos.Words;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by Jean-Paul on 9/20/2017.
 *
 * Provides key methods for saving and retrieving dvd meta data to the SQLite database
 */

class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "qrcodes.db";

    private static final String TBL_DVD = "dvd_tbl";
    private static final String COL_DVD_QRCODE = "qrcode";
    private static final String COL_DVD_TITLE = "title";
    public static final String COL_DVD_DESCRIPTION = "description";
    public static final String COL_DVD_OVERVIEW = "overview";
    private static final String COL_DVD_LOCATION = "location";

    private static final String TBL_CREDITS = "dvd_credits";
    private static final String COL_NAME = "name";
    private static final String COL_ROLE = "role";

    private static final String TBL_KEYWORDS = "dvd_keywords";
    private static final String COL_KEYWORD = "keyword";

    private static final String TBL_GENRE = "dvd_genre";
    private static final String COL_GENRE = "genre";
    private static final String COL_DESCRIPTION = "description";

    private static final String TBL_LOCATION = "dvd_location";
    private static final String COL_LOCATION = "location";

    private static final String TBL_DVD_KEYWORDS = "dvd_to_genre";
    private static final String TBL_DVD_CREDITS = "dvd_to_credits";
    private static final String TBL_DVD_GENRE = "dvd_to_genre";
    private static final String TBL_DVD_LOCATION = "dvd_to_location";




    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "CREATE TABLE " + TBL_DVD + "(" + COL_DVD_QRCODE +" TEXT PRIMARY KEY," +
                COL_DVD_TITLE+" TEXT,"+COL_DVD_DESCRIPTION+" TEXT,"+COL_DVD_OVERVIEW+" TEXT)");
        db.execSQL( "CREATE TABLE " + TBL_KEYWORDS + "("+COL_KEYWORD+" TEXT PRIMARY KEY)");
        db.execSQL( "CREATE TABLE " + TBL_CREDITS + "("+COL_NAME + " TEXT PRIMARY KEY)");
        db.execSQL( "CREATE TABLE " + TBL_GENRE + "("+COL_GENRE+" TEXT PRIMARY KEY," +
                COL_DESCRIPTION+" TEXT)");
        db.execSQL( "CREATE TABLE " + TBL_LOCATION + "("+COL_LOCATION + " TEXT PRIMARY KEY)");
        //Bridge tables init
        db.execSQL( "CREATE TABLE " + TBL_DVD_KEYWORDS + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COL_DVD_QRCODE+" TEXT,"+COL_KEYWORD+" TEXT,"+
                "FOREIGN KEY("+COL_DVD_QRCODE+") REFERENCES " + TBL_DVD+"("+COL_DVD_QRCODE+"),"+
                "FOREIGN KEY("+COL_KEYWORD+") REFERENCES " + TBL_KEYWORDS+"("+COL_KEYWORD+")"+ ")");
        db.execSQL( "CREATE TABLE " + TBL_DVD_CREDITS + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_DVD_QRCODE+" TEXT,"+COL_NAME+" TEXT,"+
                "FOREIGN KEY("+COL_DVD_QRCODE+") REFERENCES " + TBL_DVD+"("+COL_DVD_QRCODE+"),"+
                "FOREIGN KEY("+COL_NAME+") REFERENCES " + TBL_CREDITS+ "("+COL_NAME+")"+ ")");
        db.execSQL( "CREATE TABLE " + TBL_DVD_GENRE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_DVD_QRCODE+" TEXT,"+COL_GENRE+" TEXT,"+
                "FOREIGN KEY("+COL_DVD_QRCODE+") REFERENCES " + TBL_DVD+"("+COL_DVD_QRCODE+"),"+
                "FOREIGN KEY("+COL_GENRE+") REFERENCES " + TBL_GENRE+ "("+COL_GENRE+")"+ ")");
        db.execSQL( "CREATE TABLE " + TBL_DVD_LOCATION + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_DVD_QRCODE+" TEXT,"+COL_LOCATION+" TEXT,"+
                "FOREIGN KEY("+COL_DVD_QRCODE+") REFERENCES " + TBL_DVD+"("+COL_DVD_QRCODE+"),"+
                "FOREIGN KEY("+COL_LOCATION+") REFERENCES "+TBL_LOCATION+"("+COL_LOCATION+")"+")");

    }

    /**
     * Called whenever the database must be upgraded, causing all tables to be dropped
     * Allowing a new database to be initialized
     * @param db the database reference to be upgraded
     * @param i optional param
     * @param i1 optional param
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_DVD);
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_CREDITS);
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_KEYWORDS);
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_LOCATION);
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_GENRE);
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_DVD_CREDITS);
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_DVD_KEYWORDS);
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_DVD_LOCATION);
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_DVD_GENRE);

        onCreate(db);
    }

    /**
     * Adds a new dvd to the dvd table using the qrcode as a unique private key
     * @param qrcode the unique qrcode for the dvd
     * @return true indicates success
     */
    boolean addDvd(String qrcode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DVD_QRCODE,qrcode);

        try {
            long result = db.insertOrThrow(TBL_DVD, null, cv);
            return result != -1;
        }
        catch (SQLiteConstraintException e){
            return false;
        }
    }

    /**
     * Provides a cursor package of data from the DVD Table
     * @return the cursor data of the DVD Table in the db
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM "+ TBL_DVD,null);
        return result;
    }

    /**
     * Provides the key elements of the database as a CSV
     * Does not include description text.
     * @return the filepath of the output csv
     */
    String exportData() {
        SQLiteDatabase db = this.getWritableDatabase();

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "db_snapshot.csv");
        try
        {
            boolean suc = file.createNewFile();
            CsvWriter csvWrite = new CsvWriter(new FileWriter(file));
            Cursor curCSV = db.rawQuery("SELECT "+COL_DVD_QRCODE+","+COL_DVD_TITLE+","+
                    COL_DVD_LOCATION + " FROM "+ TBL_DVD,null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext())
            {
                //Which column you want to exprort
                String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        }
        catch(Exception sqlEx)
        {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
        return file.getPath();
    }

    /**
     * Updates an existing DVD entry with extra details obtained via internet API call
     * @param response a pojo populated by restful api call
     */
    void updateDvd(Items response) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DVD_QRCODE,response.getUpc());
        cv.put(COL_DVD_TITLE,response.getTitle());
        cv.put(COL_DVD_DESCRIPTION,response.getDescription());
        db.update(TBL_DVD,cv,COL_DVD_QRCODE+" IS ?", new String[]{response.getUpc()});
    }

    void updateDvd(String qrcode, Credits credits) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv_creds = new ContentValues();
        ContentValues cv_bridge = new ContentValues();

        for (Cast cast : credits.getCast()){
            cv_creds.put(COL_NAME,cast.getName());
            cv_bridge.put(COL_DVD_QRCODE,qrcode);
            cv_bridge.put(COL_NAME,cast.getName());
            cv_bridge.put(COL_ROLE,cast.getCharacter());
            db.insert(TBL_CREDITS,null,cv_creds);
            db.insert(TBL_DVD_CREDITS,null,cv_bridge);
            cv_creds.clear();
            cv_bridge.clear();
        }
    }

    void updateDvd(String qrcode, MovieDetails movie_details) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv_genre = new ContentValues();
        ContentValues cv_bridge = new ContentValues();

        for (Genres genre : movie_details.getGenres()){
            cv_genre.put(COL_GENRE,genre.getName());
            cv_bridge.put(COL_DVD_QRCODE,qrcode);
            cv_bridge.put(COL_GENRE,genre.getName());
            db.insert(TBL_GENRE,null,cv_genre);
            db.insert(TBL_DVD_GENRE,null,cv_bridge);
            cv_genre.clear();
            cv_bridge.clear();
        }
    }

    void updateDvd(String qrcode, Keywords keywords) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv_keywords = new ContentValues();
        ContentValues cv_bridge = new ContentValues();

        for (Words word : keywords.getKeywords()){
            cv_keywords.put(COL_KEYWORD,word.getName());
            cv_bridge.put(COL_DVD_QRCODE,qrcode);
            cv_bridge.put(COL_KEYWORD,word.getName());
            db.insert(TBL_KEYWORDS,null,cv_keywords);
            db.insert(TBL_DVD_KEYWORDS,null,cv_bridge);
            cv_keywords.clear();
            cv_bridge.clear();
        }
    }

    void updateDvd(String qrcode, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv_location = new ContentValues();
        ContentValues cv_bridge = new ContentValues();

        cv_location.put(COL_LOCATION,location);
        cv_bridge.put(COL_DVD_QRCODE,qrcode);
        cv_bridge.put(COL_LOCATION,location);
        db.insert(TBL_LOCATION,null,cv_location);
        db.insert(TBL_DVD_LOCATION,null,cv_bridge);
        cv_location.clear();
        cv_bridge.clear();
    }
}