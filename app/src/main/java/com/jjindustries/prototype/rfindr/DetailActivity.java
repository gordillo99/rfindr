package com.jjindustries.prototype.rfindr;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jjindustries.prototype.rfindr.data.ReminderContract;
import com.jjindustries.prototype.rfindr.databinding.ActivityDetailBinding;
import com.jjindustries.prototype.rfindr.utilities.LocationUtilities;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OnMapReadyCallback {

    // reminder columns to be accessed in the main activity
    public static final String[] REMINDER_DETAIL_PROJECTION = {
            ReminderContract.ReminderEntry._ID,
            ReminderContract.ReminderEntry.COLUMN_NAME,
            ReminderContract.ReminderEntry.COLUMN_LOCATION,
            ReminderContract.ReminderEntry.COLUMN_RADIUS,
            ReminderContract.ReminderEntry.COLUMN_DESCRIPTION,
            ReminderContract.ReminderEntry.COLUMN_STATUS,
            ReminderContract.ReminderEntry.COLUMN_DATE_CREATED
    };

    // indexes used to access reminder cursors
    public static final int INDEX_ID = 0;
    public static final int INDEX_NAME = 1;
    public static final int INDEX_LOCATION = 2;
    public static final int INDEX_RADIUS = 3;
    public static final int INDEX_DESCRIPTION = 4;
    public static final int INDEX_STATUS = 5;
    public static final int INDEX_DATE_CREATED = 6;

    private static final int ID_DETAIL_LOADER = 353;

    /* The URI that is used to access the chosen reminder's details */
    private Uri mUri;

    private ActivityDetailBinding mDetailBinding;

    private Location mSelectedLocation;

    private DialogInterface.OnClickListener mDialogClickListener;

    private GoogleMap mGoogleMap;
    private long mReminderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.details_map);
        mapFragment.getMapAsync(this);
        mReminderId = getIntent().getLongExtra(MainActivity.REMINDER_ID_EXTRA_KEY, -1);

        mDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteReminder();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Get the ID of the clicked item */
        int id = item.getItemId();

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_edit) {
            startEditionActivity();
            return true;
        }

        if (id == R.id.action_delete) {
            showDeletionDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startEditionActivity() {
        Intent intent = new Intent(this, CreateReminderActivity.class);
        intent.setAction(CreateReminderActivity.UPDATE_REMINDER_ACTION);
        intent.putExtra(CreateReminderActivity.REMINDER_NAME_EXTRA_KEY, mDetailBinding.nameTv.getText());
        intent.putExtra(CreateReminderActivity.REMINDER_DESCRIPTION_EXTRA_KEY, mDetailBinding.descriptionContentTv.getText());
        intent.putExtra(CreateReminderActivity.REMINDER_RADIUS_EXTRA_KEY, mDetailBinding.radiusContentTv.getText());
        intent.putExtra(CreateReminderActivity.REMINDER_LOCATION_EXTRA_KEY, mSelectedLocation.getLatitude() + "," + mSelectedLocation.getLongitude());
        intent.putExtra(MainActivity.REMINDER_ID_EXTRA_KEY, mReminderId);
        startActivityForResult(intent, 0);

    }

    private void showDeletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_reminder_dialog_prompt))
                .setPositiveButton(getString(R.string.delete_reminder_dialog_positive_label), mDialogClickListener)
                .setNegativeButton(getString(R.string.delete_reminder_dialog_negative_label), mDialogClickListener)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.getContentResolver().notifyChange(ReminderContract.ReminderEntry.CONTENT_URI, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {

            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        REMINDER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            return;
        }
        String reminderName = data.getString(INDEX_NAME);
        String reminderDescription = data.getString(INDEX_DESCRIPTION);
        String reminderRadius = data.getString(INDEX_RADIUS);
        String reminderLocation = data.getString(INDEX_LOCATION);

        mDetailBinding.nameTv.setText(reminderName);

        if (reminderDescription.isEmpty()) {
            mDetailBinding.descriptionLabelTv.setVisibility(View.GONE);
            mDetailBinding.descriptionContentTv.setVisibility(View.GONE);

        } else {
            mDetailBinding.descriptionContentTv.setText(reminderDescription);
        }

        double[] selectedLatLng = LocationUtilities.getLatAndLongFromString(reminderLocation);
        mSelectedLocation = new Location("");
        mSelectedLocation.setLatitude(selectedLatLng[0]);
        mSelectedLocation.setLongitude(selectedLatLng[1]);

        LatLng selectedMapLocation = new LatLng(mSelectedLocation.getLatitude(), mSelectedLocation.getLongitude());
        mGoogleMap.clear();
        mGoogleMap.addMarker(new MarkerOptions().position(selectedMapLocation).title("Selected Location"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(selectedMapLocation));

        Circle circle = mGoogleMap.addCircle(new CircleOptions()
                .center(new LatLng(mSelectedLocation.getLatitude(), mSelectedLocation.getLongitude()))
                .radius(Double.valueOf(reminderRadius))
                .strokeWidth(2)
                .fillColor(Color.argb(100, 66, 217, 244)));

        int radius = Integer.valueOf(reminderRadius);

        mDetailBinding.radiusContentTv.setText(LocationUtilities.formatDistance(this, radius));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMinZoomPreference(12.0f);
        mGoogleMap.setMaxZoomPreference(20.0f);
    }

    private void deleteReminder() {
        Uri uri = ReminderContract.ReminderEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(String.valueOf(mReminderId)).build();
        getContentResolver().delete(uri, null, null);
        Toast.makeText(this, getString(R.string.reminder_was_deleted_label), Toast.LENGTH_LONG);
        finish();
    }
}
