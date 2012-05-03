package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class FlightAdapter extends BaseAdapter {

	private Context mContext;

	private LayoutInflater mInflater;

	private FlightSearchResponse mFlights;

	private Calendar mMinTime;
	private Calendar mMaxTime;

	private int mLegPosition;

	public FlightAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setFlights(FlightSearchResponse flights) {
		if (flights != mFlights) {
			mFlights = flights;

			// Calculate the min/max time
			List<FlightTrip> trips = mFlights.getTrips();
			FlightTrip trip = trips.get(0);
			FlightLeg leg = trip.getLeg(mLegPosition);
			mMinTime = leg.getSegment(0).mOrigin.getMostRelevantDateTime();
			mMaxTime = leg.getSegment(leg.getSegmentCount() - 1).mDestination.getMostRelevantDateTime();

			DateFormat df = android.text.format.DateFormat.getTimeFormat(mContext);
			DateFormat df2 = android.text.format.DateFormat.getDateFormat(mContext);
			Log.i("Start min time: " + df2.format(mMinTime.getTime()) + " " + df.format(mMinTime.getTime()));
			Log.i("Start max time: " + df2.format(mMaxTime.getTime()) + " " + df.format(mMaxTime.getTime()));

			for (int a = 1; a < trips.size(); a++) {
				trip = trips.get(a);
				leg = trip.getLeg(mLegPosition);

				Calendar minTime = leg.getSegment(0).mOrigin.getMostRelevantDateTime();
				Calendar maxTime = leg.getSegment(leg.getSegmentCount() - 1).mDestination.getMostRelevantDateTime();

				if (minTime.before(mMinTime)) {
					mMinTime = minTime;
					Log.i("NEW min time: " + df2.format(mMinTime.getTime()) + " " + df.format(mMinTime.getTime()));
				}
				if (maxTime.after(mMaxTime)) {
					mMaxTime = maxTime;
					Log.i("NEW max time: " + df2.format(mMaxTime.getTime()) + " " + df.format(mMaxTime.getTime()));
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
		if (mFlights == null) {
			return 0;
		}

		return mFlights.getTripCount();
	}

	@Override
	public FlightTrip getItem(int position) {
		return mFlights.getTrip(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_flight, parent, false);

			holder = new ViewHolder();
			holder.mAirlineTextView = Ui.findView(convertView, R.id.airline_text_view);
			holder.mPriceTextView = Ui.findView(convertView, R.id.price_text_view);
			holder.mDepartureTimeTextView = Ui.findView(convertView, R.id.departure_time_text_view);
			holder.mArrivalTimeTextView = Ui.findView(convertView, R.id.arrival_time_text_view);
			holder.mFlightTripView = Ui.findView(convertView, R.id.flight_trip_view);

			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		FlightTrip trip = getItem(position);
		FlightLeg leg = trip.getLeg(mLegPosition);

		holder.mAirlineTextView.setText(leg.getAirlineName(mContext));
		holder.mDepartureTimeTextView.setText(formatTime(leg.getSegment(0).mOrigin.getMostRelevantDateTime()));
		holder.mArrivalTimeTextView.setText(formatTime(leg.getSegment(leg.getSegmentCount() - 1).mDestination
				.getMostRelevantDateTime()));

		if (trip.hasPricing()) {
			holder.mPriceTextView.setText(trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
		}
		else {
			holder.mPriceTextView.setText(null);
		}

		holder.mFlightTripView.setUp(leg, mMinTime, mMaxTime);

		return convertView;
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(mContext);
		return df.format(new Date(cal.getTimeInMillis()));
	}

	private static class ViewHolder {

		private TextView mAirlineTextView;
		private TextView mPriceTextView;
		private TextView mDepartureTimeTextView;
		private TextView mArrivalTimeTextView;
		private FlightTripView mFlightTripView;

	}
}
