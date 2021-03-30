package com.uhs.mobileantivirus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.ContentView;

public class DatabaseCloud extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper2";

    private static final String TABLE_NAME2 = "storage";
    private static final String PK = "ID";
    private static final String Size = "size";

    public DatabaseCloud(Context context){
        super(context, TABLE_NAME2, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable2 = "CREATE TABLE " + TABLE_NAME2 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + Size + " TEXT)";
        db.execSQL(createTable2);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME2);
        onCreate(db);
    }


    public boolean updateStorage(String item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Size, item);

        long result = db.insert(TABLE_NAME2, null, contentValues);

        if (result == -1){
            return false;
        } else {
            return true;
        }
    }


    public Cursor getStorage(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME2 + " ORDER BY " + PK + " DESC LIMIT 1";
        Cursor data = db.rawQuery(query,null);
        return data;
    }
}
