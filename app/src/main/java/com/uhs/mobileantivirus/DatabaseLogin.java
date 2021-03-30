package com.uhs.mobileantivirus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseLogin extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseLogin";
    private static final String PK = "ID";
    private static final String TABLE_NAME = "TB_USERID";
    private static final String USERID = "USERID";
    private static final String LOGIN_STATUS = "LOGIN_STATUS";

    public DatabaseLogin(Context context){
        super(context, TABLE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + USERID + " TEXT, " + LOGIN_STATUS + " TEXT)";
        db.execSQL(createTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public boolean addData(String item1, String item2){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERID, item1);
        contentValues.put(LOGIN_STATUS, item2);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1){
            return false;
        } else {
            return true;
        }
    }


    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + PK + " DESC LIMIT 1";
        Cursor data = db.rawQuery(query,null);
        return data;
    }

}
