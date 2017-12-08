package com.jjindustries.prototype.rfindr.sync;

import android.app.IntentService;
import android.content.Intent;

public class BackgroundLocationIntentService extends IntentService {
    public static final String REMINDER_ID_KEY = "reminder-id-key";

    public BackgroundLocationIntentService() {
        super("BackgroundLocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        long reminderId = intent.getLongExtra(REMINDER_ID_KEY, -1);
        BackgroundLocationTasks.executeTask(this, action, reminderId);
    }
}
