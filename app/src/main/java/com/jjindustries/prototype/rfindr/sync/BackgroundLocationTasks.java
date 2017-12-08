package com.jjindustries.prototype.rfindr.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.jjindustries.prototype.rfindr.DetailActivity;
import com.jjindustries.prototype.rfindr.MainActivity;
import com.jjindustries.prototype.rfindr.R;
import com.jjindustries.prototype.rfindr.data.ReminderContract;
import com.jjindustries.prototype.rfindr.utilities.DateUtilities;

import java.util.Calendar;
import java.util.Date;

public class BackgroundLocationTasks {

    public static final String ACTION_SEE_REMINDER_DETAILS = "see-reminder-details";
    public static final String ACTION_SNOOZE_NOTIFICATION = "snooze-notification";
    public static final String ACTION_DELETE_REMINDER = "delete-reminder";

    public static void executeTask(Context context, String action, long reminderId) {
        if (ACTION_SEE_REMINDER_DETAILS.equals(action)) {
            seeReminderDetails(context, reminderId);
        } else if (ACTION_SNOOZE_NOTIFICATION.equals(action)) {
            snoozeNotification(context, reminderId);
        } else if (ACTION_DELETE_REMINDER.equals(action)) {
            deleteReminder(context, reminderId);
        }
    }

    private static void seeReminderDetails(Context context, long reminderId) {
        Intent reminderDetailIntent = new Intent(context, DetailActivity.class);
        Uri uriForDateClicked = ReminderContract.ReminderEntry.buildReminderUriWithId(reminderId);
        reminderDetailIntent.setData(uriForDateClicked);
        reminderDetailIntent.putExtra(MainActivity.REMINDER_ID_EXTRA_KEY, reminderId);
        context.startActivity(reminderDetailIntent);
    }

    private static void snoozeNotification(Context context, long reminderId) {
        Date now =  new Date();
        Date dateInTheFuture = DateUtilities.addTimeToDate(now, Calendar.SECOND, 10); //TODO IMPORTANT: MAKE IT SO YOU CAN SNOOZE ACCORDING TO SETTINGS
        String formattedDate = DateUtilities.formatDate(dateInTheFuture);

        Uri uri = ReminderContract.ReminderEntry.CONTENT_URI;
        String stringId = Long.toString(reminderId);
        uri = uri.buildUpon().appendPath(stringId).build();

        ContentValues values = new ContentValues();
        values.put(ReminderContract.ReminderEntry.COLUMN_SNOOZE_UNTIL, formattedDate);
        context.getContentResolver().update(uri, values, null, null);
    }

    private static void deleteReminder(Context context, long reminderId) {
        Uri uri = ReminderContract.ReminderEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(String.valueOf(reminderId)).build();
        context.getContentResolver().delete(uri, null, null);
        Toast.makeText(context, context.getString(R.string.reminder_was_deleted_label), Toast.LENGTH_LONG);
    }
}
