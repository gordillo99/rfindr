package com.jjindustries.prototype.rfindr;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.jjindustries.prototype.rfindr.data.ReminderContract;
import com.jjindustries.prototype.rfindr.utilities.LocationUtilities;

import java.util.ArrayList;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderAdapterViewHolder> {
    private final Context mContext;
    private Cursor mCursor;
    final private ReminderAdapterOnClickHandler mClickHandler;
    private ArrayList<Integer> mCurrentDistancesState;

    public ReminderAdapter(@NonNull Context context, ReminderAdapterOnClickHandler clickHandler, ArrayList<Integer> currentDistances) {
        mContext = context;
        mClickHandler = clickHandler;
        mCurrentDistancesState = currentDistances;
    }

    @Override
    public ReminderAdapter.ReminderAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId = R.layout.reminder_list_item;
        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
        view.setFocusable(true);

        return new ReminderAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReminderAdapterViewHolder reminderAdapterViewHolder, int position) {
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null

        mCursor.moveToPosition(position);

        // gets the name from the cursor and set it
        String reminderName = mCursor.getString(MainActivity.INDEX_NAME);
        reminderAdapterViewHolder.nameView.setText(reminderName);

        reminderAdapterViewHolder.itemView.setTag(R.id.distance_tv, mCursor.getString(MainActivity.INDEX_LOCATION));
        String radiusString = mCursor.getString(MainActivity.INDEX_RADIUS);
        reminderAdapterViewHolder.itemView.setTag(R.id.radius_tv, radiusString);
        reminderAdapterViewHolder.radiusView.setText(LocationUtilities.formatDistance(mContext, radiusString));

        reminderAdapterViewHolder.radius = Integer.valueOf(mCursor.getString(MainActivity.INDEX_RADIUS));

        int enabled = mCursor.getInt(MainActivity.INDEX_ENABLED);
        reminderAdapterViewHolder.enabledSwitch.setChecked(enabled == 1);

        long id = mCursor.getLong(mCursor.getColumnIndex(ReminderContract.ReminderEntry._ID));
        reminderAdapterViewHolder.itemView.setTag(id);
        reminderAdapterViewHolder.enabledSwitch.setTag(reminderAdapterViewHolder);

        String locationString = mCursor.getString(MainActivity.INDEX_LOCATION);
        reminderAdapterViewHolder.distanceView.setTag(locationString);

        if (reminderAdapterViewHolder.currentDistance > 0) {
            reminderAdapterViewHolder.distanceView.setText(LocationUtilities.formatDistance(mContext, reminderAdapterViewHolder.currentDistance));
        }

        reminderAdapterViewHolder.enabledSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                Uri uri = ReminderContract.ReminderEntry.CONTENT_URI;

                ReminderAdapterViewHolder holder = (ReminderAdapterViewHolder) view.getTag();
                int position = holder.getAdapterPosition();
                Log.e("", position + " adapter position");
                String stringId = String.valueOf(holder.itemView.getTag());
                uri = uri.buildUpon().appendPath(stringId).build();
                AsyncQueryHandler queryHandler = new AsyncQueryHandler(mContext.getContentResolver()) {};

                Switch enabledSwitch = (Switch) view;
                if (enabledSwitch.isChecked()) {
                    values.put(ReminderContract.ReminderEntry.COLUMN_ENABLED, 1);
                    Toast.makeText(mContext,"Reminder is enabled.", Toast.LENGTH_SHORT).show();
                } else {
                    values.put(ReminderContract.ReminderEntry.COLUMN_ENABLED, 0);
                    Toast.makeText(mContext,"Reminder is disabled.", Toast.LENGTH_SHORT).show();
                }
                queryHandler.startUpdate(1, null, uri, values, null, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public interface ReminderAdapterOnClickHandler {
        void onClick(long reminderId);
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    class ReminderAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView nameView;
        final TextView distanceView;
        final TextView radiusView;
        final Switch enabledSwitch;
        int currentDistance = -1;
        int radius = -1;

        ReminderAdapterViewHolder(final View view) {
            super(view);
            nameView = (TextView) view.findViewById(R.id.name_tv);
            distanceView = (TextView) view.findViewById(R.id.distance_tv);
            radiusView = (TextView) view.findViewById(R.id.radius_tv);
            enabledSwitch = (Switch) view.findViewById(R.id.enabled_switch);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long reminderId = mCursor.getLong(mCursor.getColumnIndex(ReminderContract.ReminderEntry._ID));
            mClickHandler.onClick(reminderId);
        }
    }
}
