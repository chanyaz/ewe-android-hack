package com.expedia.bookings.widget;

import java.util.Calendar;

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

public class FlightAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	private FlightTripQuery mFlightTripQuery;

	private Calendar mMinTime;
	private Calendar mMaxTime;

	private int mLegPosition;

	public FlightAdapter(Context context, Bundle savedInstanceState) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setFlightTripQuery(FlightTripQuery query) {
		if (query != mFlightTripQuery) {
			if (mFlightTripQuery != null) {
				mFlightTripQuery.unregisterDataSetObserver(mDataSetObserver);
			}

			mFlightTripQuery = query;

			if (mFlightTripQuery != null) {
				mFlightTripQuery.registerDataSetObserver(mDataSetObserver);

				mMinTime = mFlightTripQuery.getMinTime();
				mMaxTime = mFlightTripQuery.getMaxTime();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.section_flight_leg_summary, parent, false);

			// Set a custom, interactive bg
			convertView.setBackgroundResource(R.drawable.bg_flight_row);
		}

		FlightLegSummarySection section = (FlightLegSummarySection) convertView;
		FlightTrip trip = getItem(position);
		FlightLeg leg = trip.getLeg(mLegPosition);
		section.bind(trip, leg, mMinTime, mMaxTime);

		return convertView;
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
