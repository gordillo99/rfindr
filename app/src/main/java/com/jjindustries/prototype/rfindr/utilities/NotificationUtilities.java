package com.jjindustries.prototype.rfindr.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.jjindustries.prototype.rfindr.MainActivity;
import com.jjindustries.prototype.rfindr.R;
import com.jjindustries.prototype.rfindr.sync.BackgroundLocationIntentService;
import com.jjindustries.prototype.rfindr.sync.BackgroundLocationTasks;


public class NotificationUtilities {

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 1138 is in no way significant.
     */
    private static final int TASK_CLOSE_NOTIFICATION_ID = 1138;
    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private static final int CLOSE_TO_TASK_PENDING_INTENT_ID = 3417;
    private static final int ACTION_SEE_DETAILS_PENDING_INTENT_ID = 1;
    private static final int ACTION_SNOOZE_PENDING_INTENT_ID = 14;
    private static final int ACTION_DELETE_REMINDER_PENDING_INTENT_ID = 24;

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void remindUserBecauseCloseToTask(Context context, String taskName, long reminderId) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_stat_reminder_location_marker)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.close_to_location_notification_title))
                .setContentText(context.getString(R.string.close_to_location_notification_body) + " " + taskName)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.close_to_location_notification_body) + " " + taskName))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .addAction(seeReminderDetailsAction(context, reminderId))
                .addAction(snoozeReminderAction(context, reminderId))
                .addAction(deleteReminderAction(context, reminderId))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        notificationManager.notify((int) reminderId, notificationBuilder.build());
    }

    private static NotificationCompat.Action snoozeReminderAction(Context context, long reminderId) {
        Intent snoozeReminderIntent = new Intent(context, BackgroundLocationIntentService.class);
        snoozeReminderIntent.setAction(BackgroundLocationTasks.ACTION_SNOOZE_NOTIFICATION);
        snoozeReminderIntent.putExtra(BackgroundLocationIntentService.REMINDER_ID_KEY, reminderId);
        PendingIntent snoozeReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_SNOOZE_PENDING_INTENT_ID,
                snoozeReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action snoozeReminderAction = new NotificationCompat.Action(R.drawable.places_ic_search,
                context.getString(R.string.snooze_reminder_notification_action),
                snoozeReminderPendingIntent);
        return snoozeReminderAction;
    }

    private static NotificationCompat.Action seeReminderDetailsAction(Context context, long reminderId) {
        Intent seeReminderDetailsIntent = new Intent(context, BackgroundLocationIntentService.class);
        seeReminderDetailsIntent.putExtra(BackgroundLocationIntentService.REMINDER_ID_KEY, reminderId);
        seeReminderDetailsIntent.setAction(BackgroundLocationTasks.ACTION_SEE_REMINDER_DETAILS);
        PendingIntent seeReminderDetailsPendingIntent = PendingIntent.getService(
                context,
                ACTION_SEE_DETAILS_PENDING_INTENT_ID,
                seeReminderDetailsIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action seeReminderDetailsAction = new NotificationCompat.Action(R.drawable.places_ic_search,
                context.getString(R.string.see_details_reminder_notification_action),
                seeReminderDetailsPendingIntent);
        return seeReminderDetailsAction;
    }

    private static NotificationCompat.Action deleteReminderAction(Context context, long reminderId) {
        Intent deleteReminderIntent = new Intent(context, BackgroundLocationIntentService.class);
        deleteReminderIntent.putExtra(BackgroundLocationIntentService.REMINDER_ID_KEY, reminderId);
        deleteReminderIntent.setAction(BackgroundLocationTasks.ACTION_DELETE_REMINDER);
        PendingIntent deleteReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_DELETE_REMINDER_PENDING_INTENT_ID,
                deleteReminderIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action deleteReminderAction = new NotificationCompat.Action(R.drawable.places_ic_search,
                context.getString(R.string.delete_reminder_notification_action),
                deleteReminderPendingIntent);
        return deleteReminderAction;
    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                CLOSE_TO_TASK_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // TODO set good image for this
    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_local_drink_black_24px);
        return largeIcon;
    }
}
