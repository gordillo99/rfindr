package com.jjindustries.prototype.rfindr;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjindustries.prototype.rfindr.data.ReminderContract;
import com.jjindustries.prototype.rfindr.sync.CurrentLocationUpdateService;
import com.jjindustries.prototype.rfindr.utilities.LocationUtilities;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ReminderAdapter.ReminderAdapterOnClickHandler{
    private ReminderAdapter mReminderAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private ProgressBar mLoadingIndicator;
    private TextView mNoRemindersCreatedTv;
    private LinearLayoutManager mLinearLayoutManager;
    private BroadcastReceiver mLocationChangedBroadCastReceiver;

    private static final int ID_REMINDER_LOADER = 44;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String LOCATION_CHANGED_ACTION = "location-changed-action";

    public static final String REMINDER_ID_EXTRA_KEY = "reminder-id";

    public ArrayList<Integer> mCurrentDistancesState = new ArrayList<>();
    public static final String CURRENT_DISTANCES_STATE_KEY = "current-distance-state-key";

    // reminder columns to be accessed in the main activity
    public static final String[] MAIN_REMINDER_PROJECTION = {
        ReminderContract.ReminderEntry._ID,
        ReminderContract.ReminderEntry.COLUMN_NAME,
        ReminderContract.ReminderEntry.COLUMN_LOCATION,
        ReminderContract.ReminderEntry.COLUMN_RADIUS,
        ReminderContract.ReminderEntry.COLUMN_ENABLED,
        ReminderContract.ReminderEntry.COLUMN_SNOOZE_UNTIL
    };

    // indexes used to access reminder cursors
    public static final int INDEX_ID = 0;
    public static final int INDEX_NAME = 1;
    public static final int INDEX_LOCATION = 2;
    public static final int INDEX_RADIUS = 3;
    public static final int INDEX_ENABLED = 4;
    public static final int INDEX_SNOOZE_UNTIL = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState != null) {
            mCurrentDistancesState = savedInstanceState.getIntegerArrayList(CURRENT_DISTANCES_STATE_KEY);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_reminder);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mNoRemindersCreatedTv = (TextView) findViewById(R.id.no_reminders_label_tv);

        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mReminderAdapter = new ReminderAdapter(this, this, mCurrentDistancesState);
        mRecyclerView.setAdapter(mReminderAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        IntentFilter locationChangedIntentFilter = new IntentFilter();
        locationChangedIntentFilter.addAction(LOCATION_CHANGED_ACTION);

        mLocationChangedBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            //do something based on the intent's action
            updateRecyclerViewWithNewDistances(intent.getDoubleExtra(CurrentLocationUpdateService.mLocationLatitudeKey, -1), intent.getDoubleExtra(CurrentLocationUpdateService.mLocationLongitudeKey, -1));
            }
        };

        registerReceiver(mLocationChangedBroadCastReceiver, locationChangedIntentFilter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateReminderActivity.class);
                intent.setAction(CreateReminderActivity.CREATE_REMINDER_ACTION);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(ID_REMINDER_LOADER, null, this);

        showLoading();

        Intent backgroundLocationUpdateService = new Intent(this, CurrentLocationUpdateService.class);
        startService(backgroundLocationUpdateService);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(CURRENT_DISTANCES_STATE_KEY, mCurrentDistancesState);
    }

    @Override
    protected void onDestroy() {
        if (mLocationChangedBroadCastReceiver != null) {
            unregisterReceiver(mLocationChangedBroadCastReceiver);
            mLocationChangedBroadCastReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        }
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
    public void onClick(long reminderId) {
        Intent reminderDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
        Uri uriForReminderClicked = ReminderContract.ReminderEntry.buildReminderUriWithId(reminderId);
        reminderDetailIntent.setData(uriForReminderClicked);
        reminderDetailIntent.putExtra(REMINDER_ID_EXTRA_KEY, reminderId);
        startActivity(reminderDetailIntent);
    }

    private void updateRecyclerViewWithNewDistances(double currentLatitude, double currentLongitude) {
        int numOfElementsInRecyclerView = mRecyclerView.getAdapter().getItemCount();

        Location[] reminderLocations = new Location[numOfElementsInRecyclerView];
        Location currentLocation = new Location("");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);

        for (int i = 0; i < numOfElementsInRecyclerView; i++) {
            ReminderAdapter.ReminderAdapterViewHolder holder = (ReminderAdapter.ReminderAdapterViewHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
            if (holder == null) return;

            String locationString = (String) holder.distanceView.getTag();

            double[] coordinates = LocationUtilities.getLatAndLongFromString(locationString);
            reminderLocations[i] = new Location("");
            reminderLocations[i].setLatitude(coordinates[0]);
            reminderLocations[i].setLongitude(coordinates[1]);

            int distanceBetweenPoints = (int) currentLocation.distanceTo(reminderLocations[i]);

            TextView distanceTV = (TextView) holder.distanceView;
            distanceTV.setText(LocationUtilities.formatDistance(this, distanceBetweenPoints));
            holder.currentDistance = distanceBetweenPoints;
        }
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
        //mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) {
            showReminderDataView();
        } else {
            mNoRemindersCreatedTv.setVisibility(View.VISIBLE);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
        }
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
        /* hide the no reminders label */
        mNoRemindersCreatedTv.setVisibility(View.INVISIBLE);
    }

    private void showLoading() {
        /* hide the reminder data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
        /* hide the no reminders label */
        mNoRemindersCreatedTv.setVisibility(View.INVISIBLE);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }
}
