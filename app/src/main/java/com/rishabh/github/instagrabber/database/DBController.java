package com.rishabh.github.instagrabber.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class DBController {
    // Database fields
    private DBhelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DBController(Context context) {
        dbHelper = new DBhelper(context);
    }

    public void close() {
        dbHelper.close();
    }

    public void addimage(InstaImage img) {

        database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DBhelper.COL_IMG_NAME, img.get_name());
        values.put(DBhelper.COL_IMG_INSTA_URL , img.get_instaImageURL());
        values.put(DBhelper.COL_IMG_PHONE_URL, img.get_phoneImageURL());
        values.put(DBhelper.COL_IMG_CAPTION, img.get_caption());

        database.insert(DBhelper.TABLE_NAME, null, values);

        System.out.println("Record Added");
        database.close();
    }

    public InstaImage getInstaImage(int _id) {

        database = dbHelper.getReadableDatabase();

        Cursor cursor = database.query(DBhelper.TABLE_NAME, DBhelper.columns, DBhelper.COL_IMG_ID + " =?", new String[]{String.valueOf(_id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        InstaImage img = new InstaImage(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), cursor.getString(3),cursor.getString(4));

        return img;
    }

    public int getTotalImages(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<InstaImage> imageList = new ArrayList<InstaImage>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DBhelper.TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor.getCount();
    }

    // Getting All Employees
    public ArrayList<InstaImage> getAllInstaImages() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<InstaImage> imageList = new ArrayList<InstaImage>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DBhelper.TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                InstaImage img = new InstaImage();
                img.set_id(Integer.parseInt(cursor.getString(0)));
                img.set_name(cursor.getString(1));
                img.set_instaImageURL(cursor.getString(2));
                img.set_phoneImageURL(cursor.getString(3));
                img.set_caption(cursor.getString(4));
                // Adding contact to list
                imageList.add(img);
            } while (cursor.moveToNext());
        }

        // return contact list
        return imageList;
    }

    //// Updating single employee
    //public int updateEmployee(InstaImage emp) {
    //    SQLiteDatabase db = dbHelper.getWritableDatabase();
    //
    //    ContentValues values = new ContentValues();
    //
    //    values.put(DBhelper.COL_EMP_NAME, emp.get_name());
    //    values.put(DBhelper.COL_EMP_ADDRESS, emp.get_address());
    //    values.put(DBhelper.COL_EMP_PHONE, emp.get_phone());
    //
    //    // updating row
    //    return db.update(DBhelper.TABLE_NAME, values, DBhelper.COL_EMP_ID + " = ?",
    //            new String[]{String.valueOf(emp.get_id())});
    //}


    public  boolean isURLPresent(String postURL){
        boolean flag=false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DBhelper.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getString(2).equals(postURL)){
                    flag=true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        return flag;
    }

    public void clearTable()   {
        database = dbHelper.getWritableDatabase();
        database.delete(DBhelper.TABLE_NAME, null,null);
    }

    // Deleting single employee
    public void deleteInstaImage(InstaImage img) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DBhelper.TABLE_NAME, DBhelper.COL_IMG_ID + " = ?",
                new String[]{String.valueOf(img.get_id())});

        System.out.println("Record Deleted");
        db.close();
    }
}
