package com.jjindustries.prototype.rfindr.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.annotation.TargetApi;

import com.jjindustries.prototype.rfindr.data.ReminderContract.ReminderEntry;

public class ReminderProvider extends ContentProvider{

    public static final int CODE_REMINDER = 100;
    public static final int CODE_SPECIFIC_REMINDER = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ReminderDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ReminderContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ReminderContract.PATH_REMINDER, CODE_REMINDER);
        matcher.addURI(authority, ReminderContract.PATH_REMINDER + "/#", CODE_SPECIFIC_REMINDER);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ReminderDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch(sUriMatcher.match(uri)) {
            case CODE_REMINDER: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        ReminderEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            }
            case CODE_SPECIFIC_REMINDER:{
                String normalizedUtcDateString = uri.getLastPathSegment();

                String[] selectionArguments = new String[]{normalizedUtcDateString};

                cursor = mOpenHelper.getReadableDatabase().query(

                        ReminderEntry.TABLE_NAME,
                        projection,
                        ReminderEntry._ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("getType has not been implmented in this app.");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case CODE_REMINDER:
                long id = db.insert(ReminderEntry.TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(ReminderEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted tasks
        int tasksDeleted; // starts as 0

        // Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case CODE_SPECIFIC_REMINDER:
                // Get the task ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                tasksDeleted = db.delete(ReminderEntry.TABLE_NAME, ReminderEntry._ID + " = ? ", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (tasksDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of tasks deleted
        return tasksDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numRowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case CODE_SPECIFIC_REMINDER:
                String id = uri.getPathSegments().get(1);
                numRowsUpdated = mOpenHelper.getWritableDatabase().update(
                        ReminderEntry.TABLE_NAME,
                        values,
                        ReminderEntry._ID + " = ? ",
                        new String[]{id}
                );

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
