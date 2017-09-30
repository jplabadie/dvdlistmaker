package com.dvdlister;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.dvdlister.pojos.Items;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Jean-Paul on 9/20/2017.
 *
 * Provides key methods for saving and retrieving dvd meta data to the SQLite database
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "qrcodes.db";
    public static final String TBL_DVD_NAME = "dvd_tbl";
    public static final String COL_DVD_ID = "id";
    public static final String COL_DVD_QRCODE = "qrcode";
    public static final String COL_DVD_TITLE = "title";
    public static final String COL_DVD_DESCRIPTION = "description";
    public static final String COL_DVD_LOCATION = "location";


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "CREATE TABLE " + TBL_DVD_NAME + "(QRCODE TEXT PRIMARY KEY," +
                "TITLE TEXT,DESCRIPTION TEXT,LOCATION TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL( "DROP TABLE IF EXISTS " + TBL_DVD_NAME );
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
        cv.put(COL_DVD_QRCODE,qrcode);

        try {
            long result = db.insertOrThrow(TBL_DVD_NAME, null, cv);
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
        Cursor result = db.rawQuery("SELECT * FROM "+TBL_DVD_NAME,null);
        return result;
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
            Cursor curCSV = db.rawQuery("SELECT "+COL_DVD_QRCODE+","+COL_DVD_TITLE+","+
                    COL_DVD_LOCATION + " FROM "+TBL_DVD_NAME,null);
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
    public void updateDvd(Items response) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("qrcode",response.getUpc());
        cv.put("title",response.getTitle());
        cv.put("description",response.getDescription());
        db.update(TBL_DVD_NAME,cv,"qrcode IS ?", new String[]{response.getUpc()});
    }
}