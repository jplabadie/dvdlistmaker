package com.dvdlister.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
import com.dvdlister.pojos.Results;
import com.dvdlister.pojos.TmdbSearchResponse;
import com.dvdlister.pojos.Words;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jean-Paul on 9/20/2017.
 *
 * Provides key methods for saving and retrieving dvd meta data to the SQLite database
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "qrcodes.db";

    private static final String TBL_DVD = "dvd_tbl";
    private static final String COL_DVD_QRCODE = "qrcode";
    private static final String COL_DVD_TMDB_ID ="tmdb_id";
    private static final String COL_DVD_TITLE = "title";
    private static final String COL_DVD_CORE_TITLE = "core_title";
    private static final String COL_DVD_DESCRIPTION = "description";
    private static final String COL_DVD_PLOT = "overview";
    private static final String TBL_DVD_VIEW = "dvd_view";

    private static final String TBL_CREDITS = "dvd_credits";
    private static final String COL_NAME = "name";

    private static final String TBL_KEYWORDS = "dvd_keywords";
    private static final String COL_KEYWORD = "keyword";

    private static final String TBL_GENRE = "dvd_genre";
    private static final String COL_GENRE = "genre";
    private static final String COL_DESCRIPTION = "description";

    private static final String TBL_LOCATION = "dvd_location";
    private static final String COL_LOCATION = "location";

    private static final String TBL_DVD_KEYWORDS = "dvd_to_keywords";
    private static final String TBL_DVD_CREDITS = "dvd_to_credits";
    private static final String COL_ROLE = "role";
    private static final String TBL_DVD_GENRE = "dvd_to_genre";
    private static final String TBL_DVD_LOCATION = "dvd_to_location";


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    /**
     * Initializes the database
     * @param db a reference to the database object
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Standalone tables (DVDs,Keywords,Credits,Genres,Locations)
        db.execSQL( "CREATE TABLE " + TBL_DVD +
                "(" + COL_DVD_QRCODE + " TEXT PRIMARY KEY," +
                COL_DVD_TMDB_ID + " TEXT," +
                COL_DVD_TITLE + " TEXT," +
                COL_DVD_CORE_TITLE + " TEXT," +
                COL_DVD_DESCRIPTION + " TEXT," +
                COL_DVD_PLOT + " TEXT)" );
        db.execSQL( "CREATE TABLE " + TBL_KEYWORDS +
                "(" + COL_KEYWORD + " TEXT PRIMARY KEY)" );
        db.execSQL( "CREATE TABLE " + TBL_CREDITS +
                "(" + COL_NAME + " TEXT PRIMARY KEY)" );
        db.execSQL( "CREATE TABLE " + TBL_GENRE +
                "(" + COL_GENRE + " TEXT PRIMARY KEY," +
                COL_DESCRIPTION + " TEXT)" );
        db.execSQL( "CREATE TABLE " + TBL_LOCATION +
                "(" + COL_LOCATION + " TEXT PRIMARY KEY)" );

        //Bridge Tables linking standalone tables
        db.execSQL( "CREATE TABLE " + TBL_DVD_KEYWORDS +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_DVD_QRCODE + " TEXT," +
                COL_KEYWORD + " TEXT," +
                "FOREIGN KEY(" + COL_DVD_QRCODE + ") REFERENCES " + TBL_DVD+"("+COL_DVD_QRCODE+"),"+
                "FOREIGN KEY("+COL_KEYWORD+") REFERENCES " + TBL_KEYWORDS+"("+COL_KEYWORD+")"+ ")" );

        db.execSQL( "CREATE TABLE " + TBL_DVD_CREDITS +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_DVD_QRCODE+" TEXT," +
                COL_NAME+" TEXT," +
                COL_ROLE+" TEXT," +
                "FOREIGN KEY(" + COL_DVD_QRCODE+") REFERENCES " + TBL_DVD + "("+COL_DVD_QRCODE+"),"+
                "FOREIGN KEY(" + COL_NAME + ") REFERENCES " + TBL_CREDITS + "("+COL_NAME+")"+ ")");

        db.execSQL( "CREATE TABLE " + TBL_DVD_GENRE +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_DVD_QRCODE + " TEXT," +
                COL_GENRE+" TEXT,"+
                "FOREIGN KEY("+COL_DVD_QRCODE+") REFERENCES " + TBL_DVD+"("+COL_DVD_QRCODE+"),"+
                "FOREIGN KEY("+COL_GENRE+") REFERENCES " + TBL_GENRE+ "("+COL_GENRE+")"+ ")");

        db.execSQL( "CREATE TABLE " + TBL_DVD_LOCATION +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT," + COL_DVD_QRCODE +
                " TEXT,"+COL_LOCATION+" TEXT,"+
                "FOREIGN KEY("+COL_DVD_QRCODE+") REFERENCES " + TBL_DVD+"("+COL_DVD_QRCODE+"),"+
                "FOREIGN KEY("+COL_LOCATION+") REFERENCES "+TBL_LOCATION+"("+COL_LOCATION+")"+")");

        db.execSQL( "CREATE VIEW " + TBL_DVD_VIEW + " AS " +
                "SELECT " + TBL_DVD +"."+COL_DVD_QRCODE + " AS " + COL_DVD_QRCODE + "," +
                TBL_DVD +"."+COL_DVD_CORE_TITLE + " AS " + COL_DVD_CORE_TITLE + "," +
                TBL_DVD_LOCATION +"."+COL_LOCATION + " AS " + COL_LOCATION +"," +
                TBL_DVD+"."+COL_DVD_QRCODE + " AS _ID "+
                "FROM " +TBL_DVD + " LEFT JOIN "+ TBL_DVD_LOCATION+
                    " ON "+TBL_DVD+"."+COL_DVD_QRCODE +"="+TBL_DVD_LOCATION+"."+COL_DVD_QRCODE );
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
        db.execSQL( "DROP VIEW IF EXISTS " + TBL_DVD_VIEW);
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
    public boolean addDvd(String qrcode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put( COL_DVD_QRCODE,qrcode );

        long result = db.insertWithOnConflict(TBL_DVD, null, cv,SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    /**
     * Provides a cursor package of data from the DVD Table View
     * @return the cursor data of the DVD Table in the db
     */
    public Cursor getPrimaryData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query( TBL_DVD_VIEW,null,null,null,null,null,null ); //pull main table
    }

    /**
     * Provides the key elements of the database as a CSV
     * Does not include description text.
     * @return the filepath of the output csv
     */
    public String exportData() {
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
            Cursor loc = db.rawQuery("SELECT * FROM "+ TBL_DVD_LOCATION,null);
            Cursor curCSV = db.rawQuery("SELECT "+COL_DVD_QRCODE+","+COL_DVD_TITLE+
                    " FROM "+ TBL_DVD,null);
            String[] cols = new String[curCSV.getColumnCount()+1];
            String[] new_cols = Arrays.copyOf(curCSV.getColumnNames(),cols.length+1);
            new_cols[cols.length] = TBL_DVD_LOCATION;
            csvWrite.writeNext(new_cols);

            while(curCSV.moveToNext())
            {
                //Which column you want to export
                String qrcode = curCSV.getString(0);
                String title = curCSV.getString(1);
                String location = loc.getString(0);
                String arrStr[] ={qrcode,title, location};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            loc.close();
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
    public void updateDvd(Items response) {
        if(response == null) {
            Log.e("MainActivity","Items for updateDvd was null! Update failed.");
            return;
        }
        String core_title = response.getTitle();
        core_title =core_title.replaceAll("\\(+.*\\)","");
        core_title = core_title.replace("Deluxe Edition", "");
        core_title = core_title.replace("Special Edition","");
        core_title = core_title.replace("Anniversary Edition","");
        core_title = core_title.replace("Extended Cut","");
        core_title = core_title.replace("Director's Cut","");
        core_title = core_title.replace("Final Cut","");
        core_title = core_title.replace("Extended Edition","");
        core_title = core_title.replace("Collector's Edition","");
        core_title = core_title.replace(": Deluxe Edition", "");
        core_title = core_title.replace(": Special Edition","");
        core_title = core_title.replace(": Anniversary Edition","");
        core_title = core_title.replace(": Extended Cut","");
        core_title = core_title.replace(": Director's Cut","");
        core_title = core_title.replace(": Final Cut","");
        core_title = core_title.replace(": Extended Edition","");
        core_title = core_title.replace(": Collector's Edition","");
        core_title = core_title.replace("Trilogy","");
        core_title = core_title.replace("Box Set","");
        core_title = core_title.replace(": 10th","");
        core_title = core_title.replace("10th","");
        core_title = core_title.replace(": 15th","");
        core_title = core_title.replace("15th","");
        core_title = core_title.replace(": 20th","");
        core_title = core_title.replace("20th","");
        core_title = core_title.replace(": 25th","");
        core_title = core_title.replace("25th","");
        core_title = core_title.replace(": 30th","");
        core_title = core_title.replace("30th","");
        core_title = core_title.replace(": 35th","");
        core_title = core_title.replace("35th","");
        core_title = core_title.replace(": 40th","");
        core_title = core_title.replace("40th","");
        core_title = core_title.replace(": 45th","");
        core_title = core_title.replace("45th","");
        core_title = core_title.replace(": 50th","");
        core_title = core_title.replace("50th","");
        core_title = core_title.replace(": 55th","");
        core_title = core_title.replace("55th","");
        core_title = core_title.replace(": 60th","");
        core_title = core_title.replace("60th","");
        core_title = core_title.replace(": 65th","");
        core_title = core_title.replace("65th","");
        core_title = core_title.replace("70th","");
        core_title = core_title.replace("80th","");
        core_title = core_title.replace("90th","");
        core_title = core_title.replace("100th","");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DVD_QRCODE,response.getUpc());
        cv.put(COL_DVD_TITLE,response.getTitle());
        cv.put(COL_DVD_CORE_TITLE,core_title);
        cv.put(COL_DVD_DESCRIPTION,response.getDescription());
        db.update(TBL_DVD,cv,COL_DVD_QRCODE+" IS ?", new String[]{response.getUpc()});
    }

    /**
     *
     * @param qrcode
     * @param credits
     */
    public void updateDvd(String qrcode, Credits credits) {
        if( credits == null) {
            Log.e("MainActivity","Credits for upc "+qrcode+" was null! Ignoring.");
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv_creds = new ContentValues();
        ContentValues cv_bridge = new ContentValues();

        for (Cast cast : credits.getCast()){
            cv_creds.put(COL_NAME,cast.getName());
            cv_bridge.put(COL_DVD_QRCODE,qrcode);
            cv_bridge.put(COL_NAME,cast.getName());
            cv_bridge.put(COL_ROLE,cast.getCharacter());
            db.insertWithOnConflict(TBL_CREDITS,null,cv_creds,SQLiteDatabase.CONFLICT_IGNORE);
            db.insert(TBL_DVD_CREDITS,null,cv_bridge);
            cv_creds.clear();
            cv_bridge.clear();
        }
    }

    public String getTmdbId(String qrcode){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query( TBL_DVD,new String[]{COL_DVD_TMDB_ID},
                COL_DVD_QRCODE+" IS ? ",new String[]{qrcode},null,null,null );
        if(cur.getCount()==1) {
            cur.moveToFirst();
            String out = cur.getString(0);
            cur.close();
            return out;
        }
        return null;
    }

    /**
     *
     * @param qrcode
     * @param movie_details
     */
    public void updateDvd(String qrcode, MovieDetails movie_details) {
        if(movie_details == null) {
            System.out.println("Movie Details were NUL!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Log.e("MainActivity","MovieDetails for upc "+qrcode+" was null! Ignoring.");
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv_genre = new ContentValues();
        ContentValues cv_bridge = new ContentValues();

        for (Genres genre : movie_details.getGenres()){
            cv_genre.put(COL_GENRE,genre.getName());
            cv_bridge.put(COL_DVD_QRCODE,qrcode);
            cv_bridge.put(COL_GENRE,genre.getName());
            db.insertWithOnConflict(TBL_GENRE,null,cv_genre,SQLiteDatabase.CONFLICT_IGNORE);
            db.insertWithOnConflict(TBL_DVD_GENRE,null,cv_bridge,SQLiteDatabase.CONFLICT_IGNORE);
            cv_genre.clear();
            cv_bridge.clear();
        }
    }

    /**
     *
     * @param qrcode
     * @param keywords
     */
    public void updateDvd(String qrcode, Keywords keywords) {

        if(keywords == null || keywords.getKeywords() == null) {
            Log.e("MainActivity","Keywords for upc "+qrcode+" was null! Ignoring.");
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv_keywords = new ContentValues();
        ContentValues cv_bridge = new ContentValues();

        for (Words word : keywords.getKeywords()){
            cv_keywords.put(COL_KEYWORD,word.getName());
            cv_bridge.put(COL_DVD_QRCODE,qrcode);
            cv_bridge.put(COL_KEYWORD,word.getName());
            db.insertWithOnConflict(TBL_KEYWORDS,null,cv_keywords,SQLiteDatabase.CONFLICT_IGNORE);
            db.insertWithOnConflict(TBL_DVD_KEYWORDS,null,cv_bridge,SQLiteDatabase.CONFLICT_IGNORE);
            cv_keywords.clear();
            cv_bridge.clear();
        }
    }

    /**
     *
     * @param qrcode
     * @param location
     */
    public void updateDvdLocation(String qrcode, String location) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cur = db.query(TBL_DVD_LOCATION,new String[]{COL_DVD_QRCODE,COL_LOCATION},COL_DVD_QRCODE+ " IS ?",
                new String[]{qrcode},null,null,null);
        if(cur.getCount()>0){
            db.delete(TBL_DVD_LOCATION,COL_DVD_QRCODE + " IS ?",new String[]{qrcode});
        }
        cur.close();

        ContentValues cv_location = new ContentValues();
        ContentValues cv_bridge = new ContentValues();

        cv_location.put(COL_LOCATION,location);
        cv_bridge.put(COL_DVD_QRCODE,qrcode);
        cv_bridge.put(COL_LOCATION,location);

        //TODO: This is wasteful, we should check to see if the location exists
        // and then fork on an if-else instead of trying and catching

        db.insertWithOnConflict(TBL_LOCATION,null,cv_location,SQLiteDatabase.CONFLICT_IGNORE);
        db.insertWithOnConflict(TBL_DVD_LOCATION,null,cv_bridge,SQLiteDatabase.CONFLICT_REPLACE);

    }

    public String getTitleByUPC(String upc) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT "+COL_DVD_TITLE+" FROM "+TBL_DVD+" WHERE "+ COL_DVD_QRCODE+ " IS "
                +upc,null);
        cur.moveToNext();
        cur.close();
        return cur.getString(0);
    }

    public String getCoreTitleByUPC(String upc) {
        SQLiteDatabase db = this.getReadableDatabase();
        System.out.println("requesting Core Title on: "+ upc);
        Cursor cur = db.query(TBL_DVD_VIEW,new String[]{COL_DVD_CORE_TITLE},
                COL_DVD_QRCODE+" IS ? ",new String[]{upc},null,null,null);
        if(cur.getCount()==1) {
            cur.moveToFirst();
            return cur.getString(0);
        }
        cur.close();
        return "null";
    }

    public ArrayList<String> getTitleAndLocationAsList(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> all = new ArrayList<>();
        Cursor cur = db.query(TBL_DVD_VIEW,new String[]{"*"},
                null,null,null,null,null);
        if(cur.getCount() > 0 ){
            while(cur.moveToNext()){
                all.add(cur.getString(1)+ " " + cur.getString(2));
            }
        }
        cur.close();
        return all;
    }

    public ArrayList<String> getTitleAndLocationAsList( String genre ){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> genre_titles = new ArrayList<>();
        ArrayList<String> genre_codes = new ArrayList<>();
        Cursor cur_genre = db.query(TBL_DVD_GENRE,new String[]{COL_DVD_QRCODE},
                COL_GENRE+" IS ?",new String[]{genre},null,null,null);
        Cursor cur = db.query(TBL_DVD_VIEW,new String[]{"*"},
                null,null,null,null,null);

        if(cur_genre.getCount() > 0 ){
            while(cur_genre.moveToNext()){
                genre_codes.add(cur.getString(0));
            }
        }
        if(cur.getCount() > 0 ){
            while(cur.moveToNext()){
                String qrcode = cur.getString(0);
                if(genre_codes.contains(qrcode))
                    genre_titles.add(cur.getString(1)+ " " + cur.getString(2));
            }
        }
        cur.close();
        return genre_titles;
    }

    public void updateDvd(String upc, TmdbSearchResponse response) {
        if(response == null) {
            Log.e("MainActivity","ImdbSearchResponse for upc "+upc+" was null! Ignoring.");
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Results res = response.getResults()[0];
        String tmdbid = res.getId();
        cv.put(COL_DVD_PLOT,res.getOverview());
        cv.put(COL_DVD_TMDB_ID,tmdbid);
        db.update(TBL_DVD,cv,COL_DVD_QRCODE+" IS ? ",new String[]{upc});
    }

    public void eraseDb() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL( "DROP VIEW IF EXISTS " + TBL_DVD_VIEW);
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

    public ArrayList<String> getGenres() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query( TBL_GENRE,new String[]{COL_GENRE},null,null,null,null,null );
        ArrayList<String> genres = new ArrayList<>();

        if(cur.getCount() > 0 ){
            while(cur.moveToNext()){
                genres.add(cur.getString(0));
            }
        }
        cur.close();
        return genres;
    }
}