package com.rishabh.github.instagrabber.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBhelper extends SQLiteOpenHelper {

    //Table Name
    public static final String TABLE_NAME = "instaimage";
    //Column Names
    public static final String COL_IMG_ID = "_id";
    public static final String COL_IMG_NAME = "_name";
    public static final String COL_IMG_INSTA_URL= "_instaImageUrl";
    public static final String COL_IMG_PHONE_URL= "_phoneImageUrl";
    public static final String COL_IMG_CAPTION= "_caption";
    static final String[] columns = new String[]{DBhelper.COL_IMG_ID,
            DBhelper.COL_IMG_NAME, DBhelper.COL_IMG_INSTA_URL,
            DBhelper.COL_IMG_PHONE_URL, DBhelper.COL_IMG_CAPTION};
    //Database Information
    private static final String DATABASE_NAME = "instaimage.db";
    private static final int DATABASE_VERSION = 1;

    // creation SQLite statement
    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "(" + COL_IMG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_IMG_NAME + " TEXT NOT NULL, " + COL_IMG_INSTA_URL+ "  TEXT, " + COL_IMG_PHONE_URL+ " TEXT NOT NULL, " +COL_IMG_CAPTION+" TEXT );";

    public DBhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        System.out.println("DB Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        System.out.println("Table Created");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        System.out.println("DB Updated");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}