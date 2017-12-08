package com.jjindustries.prototype.rfindr.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jjindustries.prototype.rfindr.data.ReminderContract.ReminderEntry;

public class ReminderDbHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "reminder.db";
    private static final int DATABASE_VERSION = 7;

    public ReminderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_REMINDER_TABLE =
                "CREATE TABLE " + ReminderEntry.TABLE_NAME + " (" +
                        ReminderEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ReminderEntry.COLUMN_NAME       + " TEXT NOT NULL, "                 +
                        ReminderEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL,"                  +
                        ReminderEntry.COLUMN_LOCATION   + " TEXT NOT NULL, "                    +
                        ReminderEntry.COLUMN_RADIUS   + " INTEGER NOT NULL, "                    +
                        ReminderEntry.COLUMN_STATUS   + " TEXT NOT NULL, "                    +
                        ReminderEntry.COLUMN_SNOOZE_UNTIL   + " TEXT, "                    +
                        ReminderEntry.COLUMN_ENABLED + " INTEGER NOT NULL DEFAULT 1, " +
                        ReminderEntry.COLUMN_DATE_CREATED   + " TEXT NOT NULL);";

        // creates the database
        sqLiteDatabase.execSQL(SQL_CREATE_REMINDER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReminderContract.ReminderEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
