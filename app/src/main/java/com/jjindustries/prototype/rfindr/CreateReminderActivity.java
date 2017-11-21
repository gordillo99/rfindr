package com.jjindustries.prototype.rfindr;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjindustries.prototype.rfindr.data.ReminderContract;
import com.jjindustries.prototype.rfindr.databinding.ActivityCreateReminderBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateReminderActivity extends AppCompatActivity {

    private Spinner mRadiusSpinner;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;

    public ActivityCreateReminderBinding mBinding;

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
    }

    public String getTodayDateString() {
        Date myDate = new Date();
        SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dmyFormat.format(myDate);
    }

    public void createReminder(View view) {
        String nameString = mBinding.nameEt.getText().toString();
        String descriptionString = mBinding.descriptionEt.getText().toString();

        if (nameString == null || nameString.isEmpty()) {
            String missingNameToastText = getResources().getString(R.string.missing_reminder_name_toast_text);
            Toast.makeText(this, missingNameToastText, Toast.LENGTH_LONG).show();
            return;
        }

        //TODO put right values here
        ContentValues values = new ContentValues();
        values.put(ReminderContract.ReminderEntry.COLUMN_NAME, nameString);
        values.put(ReminderContract.ReminderEntry.COLUMN_DESCRIPTION, descriptionString);
        values.put(ReminderContract.ReminderEntry.COLUMN_LOCATION, "placeholder");
        values.put(ReminderContract.ReminderEntry.COLUMN_RADIUS, 10);
        values.put(ReminderContract.ReminderEntry.COLUMN_STATUS, "placeholder");
        values.put(ReminderContract.ReminderEntry.COLUMN_DATE_CREATED, getTodayDateString());

        Uri uri = ReminderContract.ReminderEntry.CONTENT_URI;

        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {};
        queryHandler.startInsert(1, null, uri, values);
        finish();
    }
}
