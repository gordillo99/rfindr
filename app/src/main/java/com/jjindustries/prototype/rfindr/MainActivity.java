package com.jjindustries.prototype.rfindr;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ProgressBar;

import com.jjindustries.prototype.rfindr.data.ReminderContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ReminderAdapter.ReminderAdapterOnClickHandler{

    private ReminderAdapter mReminderAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private ProgressBar mLoadingIndicator;

    private static final int ID_REMINDER_LOADER = 44;

    // reminder columns to be accessed in the main activity
    public static final String[] MAIN_REMINDER_PROJECTION = {
        ReminderContract.ReminderEntry.COLUMN_NAME,
        ReminderContract.ReminderEntry.COLUMN_DESCRIPTION,
        ReminderContract.ReminderEntry.COLUMN_LOCATION
    };

    // indexes used to access reminder cursors
    public static final int INDEX_NAME = 0;
    public static final int INDEX_DESCRIPTION = 1;
    public static final int INDEX_LOCATION = 2;
    public static final int INDEX_RADIUS = 3;
    public static final int INDEX_STATUS = 4;
    public static final int INDEX_DATE_CREATED = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_reminder);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager =  new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mReminderAdapter = new ReminderAdapter(this, this);
        mRecyclerView.setAdapter(mReminderAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateReminderActivity.class);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(ID_REMINDER_LOADER, null, this);

        showLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(long date) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case ID_REMINDER_LOADER:
                Uri reminderListQueryUri = ReminderContract.ReminderEntry.CONTENT_URI;
                String sortOrder = ReminderContract.ReminderEntry.COLUMN_NAME + " ASC";

                return new CursorLoader(this,
                        reminderListQueryUri,
                        MAIN_REMINDER_PROJECTION,
                        null,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mReminderAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showReminderDataView();
        //TODO handle case with no reminders
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mReminderAdapter.swapCursor(null);
    }

    private void showReminderDataView() {
        /* hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* make sure the reminder data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        /* hide the reminder data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }
}
