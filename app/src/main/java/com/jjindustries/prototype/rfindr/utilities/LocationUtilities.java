package com.jjindustries.prototype.rfindr.utilities;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.jjindustries.prototype.rfindr.R;

public class LocationUtilities {

    @NonNull
    public static String changeLocationFormatForDb(String placeString) {
        // format of argument: lat/lang(0,0)
        String formattedStringCoordinates = placeString.split("\\(")[1];
        return formattedStringCoordinates.substring(0, formattedStringCoordinates.length() - 1);
    }

    @Nullable
    public static double[] getLatAndLongFromString(String placeString) {
        String[] coordinateValues = placeString.split(",");
        double latitude;
        double longitude;

        try {
            latitude = Double.valueOf(coordinateValues[0]);
            longitude = Double.valueOf(coordinateValues[1]);
            return new double[] {latitude, longitude};
        }catch (NumberFormatException | NullPointerException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static LatLng stringToLatLang(String placeString) {
        double[] coordinates = getLatAndLongFromString(placeString);
        return new LatLng(coordinates[0], coordinates[1]);
    }

    // TODO add support for other unit types
    public static String formatDistance(Context context, int distance) {
        if (distance < 1000) {
            return String.valueOf(distance) + " " + context.getString(R.string.meter_unit);
        } else {
            return String.valueOf(distance/1000) + " " + context.getString(R.string.kilometer_unit);
        }
    }

    public static String formatDistance(Context context, String distance) {
        int intDistance = Integer.valueOf(distance);
        if (intDistance < 1000) {
            return String.valueOf(distance) + " " + context.getString(R.string.meter_unit);
        } else {
            return String.valueOf(intDistance/1000) + " " + context.getString(R.string.kilometer_unit);
        }
    }
}
