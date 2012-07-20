package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightAdapter extends BaseAdapter {

	private static final DecimalFormat mDaySpanFormatter = new DecimalFormat("#");

	static {
		// TODO: Should this be localized in some way?
		mDaySpanFormatter.setPositivePrefix("+");
	}

	private Context mContext;
	private Resources mResources;

	private LayoutInflater mInflater;

	private FlightTripQuery mFlightTripQuery;

	private Calendar mMinTime;
	private Calendar mMaxTime;

	private int mLegPosition;

	public FlightAdapter(Context context, Bundle savedInstanceState) {
		mContext = context;
		mResources = context.getResources();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setFlightTripQuery(FlightTripQuery query) {
		if (query != mFlightTripQuery) {
			if (mFlightTripQuery != null) {
				mFlightTripQuery.unregisterDataSetObserver(mDataSetObserver);
			}

			mFlightTripQuery = query;
			mFlightTripQuery.registerDataSetObserver(mDataSetObserver);

			// Calculate the min/max time
			List<FlightTrip> trips = mFlightTripQuery.getTrips();
			FlightTrip trip = trips.get(0);
			FlightLeg leg = trip.getLeg(mLegPosition);
			mMinTime = leg.getSegment(0).mOrigin.getMostRelevantDateTime();
			mMaxTime = leg.getSegment(leg.getSegmentCount() - 1).mDestination.getMostRelevantDateTime();

			for (int a = 1; a < trips.size(); a++) {
				trip = trips.get(a);
				leg = trip.getLeg(mLegPosition);

				Calendar minTime = leg.getSegment(0).mOrigin.getMostRelevantDateTime();
				Calendar maxTime = leg.getSegment(leg.getSegmentCount() - 1).mDestination.getMostRelevantDateTime();

				if (minTime.before(mMinTime)) {
					mMinTime = minTime;
				}
				if (maxTime.after(mMaxTime)) {
					mMaxTime = maxTime;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_flight, parent, false);

			holder = new ViewHolder();
			holder.mAirlineTextView = Ui.findView(convertView, R.id.airline_text_view);
			holder.mPriceTextView = Ui.findView(convertView, R.id.price_text_view);
			holder.mDepartureTimeTextView = Ui.findView(convertView, R.id.departure_time_text_view);
			holder.mArrivalTimeTextView = Ui.findView(convertView, R.id.arrival_time_text_view);
			holder.mMultiDayTextView = Ui.findView(convertView, R.id.multi_day_text_view);
			holder.mFlightTripView = Ui.findView(convertView, R.id.flight_trip_view);

			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		FlightTrip trip = getItem(position);
		FlightLeg leg = trip.getLeg(mLegPosition);

		holder.mAirlineTextView.setText(leg.getAirlinesFormatted());
		holder.mDepartureTimeTextView.setText(formatTime(leg.getSegment(0).mOrigin.getMostRelevantDateTime()));
		holder.mArrivalTimeTextView.setText(formatTime(leg.getSegment(leg.getSegmentCount() - 1).mDestination
				.getMostRelevantDateTime()));

		if (trip.hasPricing()) {
			holder.mPriceTextView.setText(trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
		}
		else {
			holder.mPriceTextView.setText(null);
		}

		int daySpan = leg.getDaySpan();
		if (daySpan != 0) {
			holder.mMultiDayTextView.setVisibility(View.VISIBLE);
			String daySpanStr = mDaySpanFormatter.format(daySpan);
			holder.mMultiDayTextView.setText(mResources.getQuantityString(R.plurals.day_span, daySpan, daySpanStr));
		}
		else {
			holder.mMultiDayTextView.setVisibility(View.INVISIBLE);
		}

		holder.mFlightTripView.setUp(leg, mMinTime, mMaxTime);

		return convertView;
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(mContext);
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}

	private static class ViewHolder {

		private TextView mAirlineTextView;
		private TextView mPriceTextView;
		private TextView mDepartureTimeTextView;
		private TextView mArrivalTimeTextView;
		private TextView mMultiDayTextView;
		private FlightTripView mFlightTripView;

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
