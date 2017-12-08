package com.jjindustries.prototype.rfindr.sync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.jjindustries.prototype.rfindr.MainActivity;
import com.jjindustries.prototype.rfindr.data.ReminderContract;
import com.jjindustries.prototype.rfindr.utilities.DateUtilities;
import com.jjindustries.prototype.rfindr.utilities.LocationUtilities;
import com.jjindustries.prototype.rfindr.utilities.NotificationUtilities;

import java.util.Date;

public class CurrentLocationUpdateService extends Service{
    private static final String TAG = "MyLocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000  * 1; //TODO add the right settings to handle this....
    private static final float LOCATION_DISTANCE = 10f;
    public static final String mLocationLatitudeKey = "location-latitude";
    public static final String mLocationLongitudeKey = "location-longitude";

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.v(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.v(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            // broadcast to main to update UI
            //broadcastToMainActivity(location);
            // notify user if close to a task
            notifyUserIfCloseToTask(location);

            //TODO ADD VIBRATE IF SHOULD BE DONE IN SETTINGS
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.v(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate");

        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: "+ LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void broadcastToMainActivity(Location location) {
        // broadcast to main activity to update distance to UI values
        Intent intent = new Intent();
        intent.setAction(MainActivity.LOCATION_CHANGED_ACTION);
        intent.putExtra(mLocationLatitudeKey, location.getLatitude());
        intent.putExtra(mLocationLongitudeKey, location.getLongitude());
        sendBroadcast(intent);
    }

    private void notifyUserIfCloseToTask(Location location) {
        // check to see if notification is required
        Cursor results = getContentResolver().query(ReminderContract.ReminderEntry.CONTENT_URI, MainActivity.MAIN_REMINDER_PROJECTION, null, null, null);

        int numOfElementsInDatabase = results.getCount();

        Location currentLocation = new Location("");
        currentLocation.setLatitude(location.getLatitude());
        currentLocation.setLongitude(location.getLongitude());

        for (int i = 0; i < numOfElementsInDatabase; i++) {
            results.moveToPosition(i);

            String reminderName = results.getString(MainActivity.INDEX_NAME);
            String locationString = results.getString(MainActivity.INDEX_LOCATION);
            long reminderId = results.getLong(MainActivity.INDEX_ID);
            int radius = results.getInt(MainActivity.INDEX_RADIUS);
            int isReminderEnabled = results.getInt(MainActivity.INDEX_ENABLED);

            String snoozeUntilDateString = results.getString(MainActivity.INDEX_SNOOZE_UNTIL);
            Date snoozeUntilDate = DateUtilities.convertStringToDate(snoozeUntilDateString);
            Date now = new Date();

            Location locationFromDb = new Location("");
            double[] coordinates = LocationUtilities.getLatAndLongFromString(locationString);
            locationFromDb.setLatitude(coordinates[0]);
            locationFromDb.setLongitude(coordinates[1]);

            int distanceBetweenPoints = (int) currentLocation.distanceTo(locationFromDb);

            if (distanceBetweenPoints < radius && isReminderEnabled == 1 && now.compareTo(snoozeUntilDate) > 0) {
                NotificationUtilities.remindUserBecauseCloseToTask(CurrentLocationUpdateService.this, reminderName, reminderId);
            }
        }
    }
}

