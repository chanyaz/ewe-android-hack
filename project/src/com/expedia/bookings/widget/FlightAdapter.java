package com.expedia.bookings.widget;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.utils.LayoutUtils;

public class FlightAdapter extends BaseAdapter {

	private static final int ROW_TYPE_FIRST = 0;
	private static final int ROW_TYPE_OTHER = 1;

	private LayoutInflater mInflater;

	private FlightTripQuery mFlightTripQuery;

	private Calendar mMinTime;
	private Calendar mMaxTime;

	private int mLegPosition;

	public FlightAdapter(Context context, Bundle savedInstanceState) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setFlightTripQuery(FlightTripQuery query) {
		setFlightTripQuery(query, null, null);
	}

	public void setFlightTripQuery(FlightTripQuery query, Calendar minTime, Calendar maxTime) {
		if (query != mFlightTripQuery) {
			if (mFlightTripQuery != null) {
				mFlightTripQuery.unregisterDataSetObserver(mDataSetObserver);
			}

			mFlightTripQuery = query;

			if (mFlightTripQuery != null) {
				mFlightTripQuery.registerDataSetObserver(mDataSetObserver);

				mMinTime = (Calendar) mFlightTripQuery.getMinTime().clone();
				mMaxTime = (Calendar) mFlightTripQuery.getMaxTime().clone();

				// F1306: Make current timelines relative to old timelines, if the old
				// timeline has a longer span.
				if (minTime != null && minTime != null) {
					long duration = mMaxTime.getTimeInMillis() - mMinTime.getTimeInMillis();
					long lastDuration = maxTime.getTimeInMillis() - minTime.getTimeInMillis();
					if (duration < lastDuration) {
						int toAdd = (int) ((lastDuration - duration) / 2);
						mMinTime.add(Calendar.MILLISECOND, -toAdd);
						mMaxTime.add(Calendar.MILLISECOND, toAdd);
					}
				}
			}

			notifyDataSetChanged();
		}
	}

	public void setLegPosition(int legPosition) {
		mLegPosition = legPosition;
	}

	@Override
	public int getCount() {
		if (mFlightTripQuery == null) {
			return 0;
		}

		return mFlightTripQuery.getCount();
	}

	@Override
	public FlightTrip getItem(int position) {
		return mFlightTripQuery.getTrips().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return ROW_TYPE_FIRST;
		}
		else {
			return ROW_TYPE_OTHER;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.section_flight_leg_summary, parent, false);

			// Set a custom, interactive bg
			if (getItemViewType(position) == ROW_TYPE_FIRST) {
				LayoutUtils.setBackgroundResource(convertView, R.drawable.bg_flight_row_first);
			}
			else {
				LayoutUtils.setBackgroundResource(convertView, R.drawable.bg_flight_row);
			}
		}

		FlightLegSummarySection section = (FlightLegSummarySection) convertView;
		FlightTrip trip = getItem(position);
		FlightLeg leg = trip.getLeg(mLegPosition);
		section.bind(trip, leg, mMinTime, mMaxTime);

		return convertView;
	}

	//////////////////////////////////////////////////////////////////////////
	// Utilities

	public int getPosition(FlightLeg leg) {
		String targetLegId = leg.getLegId();
		List<FlightTrip> trips = mFlightTripQuery.getTrips();
		int len = trips.size();
		for (int pos = 0; pos < len; pos++) {
			if (trips.get(pos).getLeg(mLegPosition).getLegId().equals(targetLegId)) {
				return pos;
			}
		}

		return -1;
	}

	//////////////////////////////////////////////////////////////////////////
	// Reset

	// This *needs* to be called before the FlightAdapter is destroyed.
	// Otherwise we keep a long-running reference as a dataset observer.
	public void destroy() {
		if (mFlightTripQuery != null) {
			mFlightTripQuery.unregisterDataSetObserver(mDataSetObserver);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Dataset observer

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			notifyDataSetChanged();
		}
	};
}
