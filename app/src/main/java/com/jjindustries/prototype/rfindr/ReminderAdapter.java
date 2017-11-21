package com.jjindustries.prototype.rfindr;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderAdapterViewHolder> {
    private final Context mContext;
    private Cursor mCursor;
    final private ReminderAdapterOnClickHandler mClickHandler;

    public ReminderAdapter(@NonNull Context context, ReminderAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
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
        mCursor.moveToPosition(position);

        String reminderName = mCursor.getString(MainActivity.INDEX_NAME);
        reminderAdapterViewHolder.nameView.setText(reminderName);

        String reminderDescription = mCursor.getString(MainActivity.INDEX_DESCRIPTION);
        reminderAdapterViewHolder.descriptionView.setText(reminderDescription);

        String reminderLocation = mCursor.getString(MainActivity.INDEX_LOCATION);
        reminderAdapterViewHolder.locationView.setText(reminderLocation);
        //TODO update this with actual distance
        reminderAdapterViewHolder.distanceView.setText("100 km");
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public interface ReminderAdapterOnClickHandler {
        void onClick(long date);
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    class ReminderAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView nameView;
        final TextView descriptionView;
        final TextView locationView;
        final TextView distanceView;

        ReminderAdapterViewHolder(View view) {
            super(view);
            nameView = (TextView) view.findViewById(R.id.name_tv);
            descriptionView = (TextView) view.findViewById(R.id.description_tv);
            locationView = (TextView) view.findViewById(R.id.location_tv);
            distanceView = (TextView) view.findViewById(R.id.distance_tv);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
