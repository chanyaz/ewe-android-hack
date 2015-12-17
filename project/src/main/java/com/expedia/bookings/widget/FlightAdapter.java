package com.expedia.bookings.widget;

import java.util.List;

import org.joda.time.DateTime;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;

public class FlightAdapter extends BaseAdapter {

	private static final int ROW_TYPE_FIRST = 0;
	private static final int ROW_TYPE_OTHER = 1;

	private FlightTripQuery mFlightTripQuery;

	private DateTime mMinTime;
	private DateTime mMaxTime;

	protected int mLegPosition;

	public void setFlightTripQuery(FlightTripQuery query) {
		setFlightTripQuery(query, null, null);
	}

	public void setFlightTripQuery(FlightTripQuery query, DateTime minTime, DateTime maxTime) {
		if (query != mFlightTripQuery) {
			if (mFlightTripQuery != null) {
				mFlightTripQuery.unregisterDataSetObserver(mDataSetObserver);
			}

			mFlightTripQuery = query;

			if (mFlightTripQuery != null) {
				mFlightTripQuery.registerDataSetObserver(mDataSetObserver);
			}

			if (mFlightTripQuery != null && mFlightTripQuery.getCount() > 0) {
				mMinTime = new DateTime(mFlightTripQuery.getMinTime());
				mMaxTime = new DateTime(mFlightTripQuery.getMaxTime());

				// F1306: Make current timelines relative to old timelines, if the old
				// timeline has a longer span.
				if (minTime != null && minTime != null) {
					long duration = mMaxTime.getMillis() - mMinTime.getMillis();
					long lastDuration = maxTime.getMillis() - minTime.getMillis();
					if (duration < lastDuration) {
						int toAdd = (int) ((lastDuration - duration) / 2);
						mMinTime.plusMillis(-toAdd);
						mMaxTime.plusMillis(toAdd);
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
		List<FlightTrip> trips = mFlightTripQuery.getTrips();
		if (position < trips.size()) {
			return trips.get(position);
		}
		return null;
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
			convertView = Ui.inflate(R.layout.section_flight_leg_summary, parent, false);

			// Set a custom, interactive bg
			if (getItemViewType(position) == ROW_TYPE_FIRST) {
				LayoutUtils.setBackgroundResource(convertView, R.drawable.bg_flight_row_first);
			}
			else {
				LayoutUtils.setBackgroundResource(convertView, R.drawable.bg_flight_row);
			}
		}

		FlightLegSummarySection section = Ui.findView(convertView, R.id.flight_card_container);
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
