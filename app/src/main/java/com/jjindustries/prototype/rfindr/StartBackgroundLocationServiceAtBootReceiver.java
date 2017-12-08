package com.jjindustries.prototype.rfindr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.jjindustries.prototype.rfindr.sync.CurrentLocationUpdateService;

public class StartBackgroundLocationServiceAtBootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "starting service!", Toast.LENGTH_SHORT).show();
        Intent startServiceIntent = new Intent(context, CurrentLocationUpdateService.class);
        context.startService(startServiceIntent);
    }
}

