package com.jjindustries.prototype.rfindr;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.jjindustries.prototype.rfindr.data.ReminderContract;
import com.jjindustries.prototype.rfindr.databinding.ActivityCreateReminderBinding;
import com.jjindustries.prototype.rfindr.utilities.DateUtilities;
import com.jjindustries.prototype.rfindr.utilities.LocationUtilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateReminderActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Spinner mRadiusSpinner;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;
    private Location mSelectedLocation = null;

    public ActivityCreateReminderBinding mBinding;

    private final int PLACE_PICKER_REQUEST = 49;

    private boolean mCreateMode = true;

    public final static String CREATE_REMINDER_ACTION = "create-reminder-action";
    public final static String UPDATE_REMINDER_ACTION = "update-reminder-action";

    // extra keys (for update)
    public final static String REMINDER_NAME_EXTRA_KEY = "reminder-name-extra-key";
    public final static String REMINDER_DESCRIPTION_EXTRA_KEY = "reminder-description-extra-key";
    public final static String REMINDER_LOCATION_EXTRA_KEY = "reminder-location-extra-key";
    public final static String REMINDER_RADIUS_EXTRA_KEY = "reminder-radius-extra-key";

    private GoogleMap mGoogleMap;
    private long mReminderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_reminder);

        // populate spinner with options from array list
        mRadiusSpinner = (Spinner) findViewById(R.id.radius_spinner);
        mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.radius_options, android.R.layout.simple_spinner_item);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRadiusSpinner.setAdapter(mSpinnerAdapter);
/*
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.create_reminder_map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);
        */

        Intent intent = getIntent();
        String currentAction = intent.getAction();

        mCreateMode = currentAction.equals(CREATE_REMINDER_ACTION);

        if (!mCreateMode) {
            setTitle(getString(R.string.title_update_reminder));
            String radiusString = intent.getStringExtra(REMINDER_RADIUS_EXTRA_KEY);
            String locationString = intent.getStringExtra(REMINDER_LOCATION_EXTRA_KEY);
            mBinding.nameEt.setText(intent.getStringExtra(REMINDER_NAME_EXTRA_KEY));
            mBinding.descriptionEt.setText(intent.getStringExtra(REMINDER_DESCRIPTION_EXTRA_KEY));
            mRadiusSpinner.setSelection(mSpinnerAdapter.getPosition(radiusString));

            mReminderId = intent.getLongExtra(MainActivity.REMINDER_ID_EXTRA_KEY, -1);
            mSelectedLocation = new Location("");
            String[] partsOfLocationString = locationString.split(",");
            double latitude = Double.valueOf(partsOfLocationString[0]);
            double longitude = Double.valueOf(partsOfLocationString[1]);
            mSelectedLocation.setLatitude(latitude);
            mSelectedLocation.setLongitude(longitude);

            mBinding.selectedPlaceTv.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
/*
            String[] selectedCoordinates = intent.getStringExtra(REMINDER_LOCATION_EXTRA_KEY).split(",");
            double latitude = Double.valueOf(selectedCoordinates[0]);
            double longitude = Double.valueOf(selectedCoordinates[1]);

            LatLng selectedMapLocation = new LatLng(latitude, longitude);
            mGoogleMap.addMarker(new MarkerOptions().position(selectedMapLocation).title("Selected Location"));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(selectedMapLocation));

            String[] partsOfRadiusString = radiusString.split("\\s+");
            double radius = Double.valueOf(partsOfRadiusString[0]);

            if (partsOfRadiusString[1].equals("km")) {
                radius = radius * 1000;
            }

            Circle circle = mGoogleMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(Double.valueOf(radius))
                    .strokeWidth(2)
                    .fillColor(Color.argb(100, 66, 217, 244)));
            mapFragment.getView().setVisibility(View.VISIBLE);
*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.create, menu);
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

        if (id == R.id.action_save) {
            createReminder();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void pickPlace(View view) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(this, data);
                LatLng latitudeAndLongitude = selectedPlace.getLatLng();
                mSelectedLocation = new Location("");
                mSelectedLocation.setLatitude(latitudeAndLongitude.latitude);
                mSelectedLocation.setLongitude(latitudeAndLongitude.longitude);
                mBinding.selectedPlaceTv.setText("Latitude: " + latitudeAndLongitude.latitude + "\nLongitude: " + latitudeAndLongitude.longitude);
            }
        }
    }

    public void updateReminder() {
        //TODO fillout this method
    }

    //TODO SEPARATE INTO CREATEREMINDER AND UPDATEREMINDER
    //TODO CHANGE VALIDATION CODE TO OWN METHOD
    //TODO EXTRACT CODE TO CREATE CONTENTVALUES TO OWN METHOD
    //TODO ADD CHECK FOR CHARACTER LENGTH OF DESCRIPTION AND NAME
    public void createReminder() {
        String nameString = mBinding.nameEt.getText().toString();
        String descriptionString = mBinding.descriptionEt.getText().toString();

        String radiusString = mRadiusSpinner.getSelectedItem().toString();

        if (nameString == null || nameString.isEmpty()) {
            String missingNameToastText = getResources().getString(R.string.missing_reminder_name_toast_text);
            Toast.makeText(this, missingNameToastText, Toast.LENGTH_LONG).show();
            return;
        }

        if (mSelectedLocation == null) {
            String missingNameToastText = getString(R.string.missing_location_name_toast_text);
            Toast.makeText(this, missingNameToastText, Toast.LENGTH_LONG).show();
            return;
        }

        // format the selected radius to the appropriate amount of meters (for db)
        String[] partsOfSelectedRadius = radiusString.split("\\s+");
        String radiusStringInMeters;

        if (partsOfSelectedRadius[1].equals(this.getString(R.string.kilometer_unit))) {
            radiusStringInMeters = String.valueOf(Integer.parseInt(partsOfSelectedRadius[0]) * 1000);
        } else {
            radiusStringInMeters = partsOfSelectedRadius[0];
        }

        // Save reminder to database
        ContentValues values = new ContentValues();
        values.put(ReminderContract.ReminderEntry.COLUMN_NAME, nameString);
        values.put(ReminderContract.ReminderEntry.COLUMN_DESCRIPTION, descriptionString);
        // TODO ADD METHOD TO HANDLE THIS TO LOCATION UTILS
        values.put(ReminderContract.ReminderEntry.COLUMN_LOCATION, mSelectedLocation.getLatitude() + "," + mSelectedLocation.getLongitude());
        values.put(ReminderContract.ReminderEntry.COLUMN_RADIUS, radiusStringInMeters);
        values.put(ReminderContract.ReminderEntry.COLUMN_STATUS, this.getString(R.string.not_completed_status));
        values.put(ReminderContract.ReminderEntry.COLUMN_DATE_CREATED, DateUtilities.getCurrentTimeStamp());
        values.put(ReminderContract.ReminderEntry.COLUMN_SNOOZE_UNTIL, DateUtilities.getCurrentTimeStamp());

        Uri uri = ReminderContract.ReminderEntry.CONTENT_URI;

        if (mCreateMode) {
            AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {};
            queryHandler.startInsert(1, null, uri, values);
        } else {
            String stringId = Long.toString(mReminderId);
            uri = uri.buildUpon().appendPath(stringId).build();
            AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {};
            queryHandler.startUpdate(1, null, uri, values, null, null);
        }


        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMinZoomPreference(12.0f);
        mGoogleMap.setMaxZoomPreference(20.0f);
    }
}
