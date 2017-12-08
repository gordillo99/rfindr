package com.jjindustries.prototype.rfindr.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class ReminderContract {

    public static final String CONTENT_AUTHORITY = "com.jjindustries.prototype.rfindr";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_REMINDER = "reminder";

    public static final class ReminderEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REMINDER)
                .build();

        public static final String TABLE_NAME = "reminder";

        // reminder table columns
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_RADIUS = "radius";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_DATE_CREATED = "date_created";
        public static final String COLUMN_ENABLED = "enabled"; // 1 = true, 0 = false
        public static final String COLUMN_SNOOZE_UNTIL = "snooze_until";

        public static Uri buildReminderUriWithId(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .build();
        }
    }

}
