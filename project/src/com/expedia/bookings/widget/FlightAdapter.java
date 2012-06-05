package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.mobiata.android.util.Ui;

public class FlightAdapter extends BaseAdapter {

	private static final int SEATS_REMAINING_CUTOFF = 5;

	private enum RowType {
		NORMAL,
		EXPANDED
	}

	private Context mContext;

	private LayoutInflater mInflater;

	private FlightAdapterListener mListener;

	private List<FlightTrip> mFlightTrips;

	private Calendar mMinTime;
	private Calendar mMaxTime;

	private int mLegPosition;

	private int mExpandedLeg = -1;

	public FlightAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setListener(FlightAdapterListener listener) {
		mListener = listener;
	}

	public void setFlights(List<FlightTrip> flightTrips) {
		if (flightTrips != mFlightTrips) {
			mFlightTrips = flightTrips;

			// Calculate the min/max time
			FlightTrip trip = mFlightTrips.get(0);
			FlightLeg leg = trip.getLeg(mLegPosition);
			mMinTime = leg.getSegment(0).mOrigin.getMostRelevantDateTime();
			mMaxTime = leg.getSegment(leg.getSegmentCount() - 1).mDestination.getMostRelevantDateTime();

			for (int a = 1; a < mFlightTrips.size(); a++) {
				trip = mFlightTrips.get(a);
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

	public void setExpandedLegPosition(int expandedLegPosition) {
		mExpandedLeg = expandedLegPosition;
	}

	public int getExpandedLegPosition() {
		return mExpandedLeg;
	}

	@Override
	public int getCount() {
		if (mFlightTrips == null) {
			return 0;
		}

		return mFlightTrips.size();
	}

	@Override
	public FlightTrip getItem(int position) {
		return mFlightTrips.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == mExpandedLeg) {
			return RowType.EXPANDED.ordinal();
		}
		else {
			return RowType.NORMAL.ordinal();
		}
	}

	@Override
	public int getViewTypeCount() {
		return RowType.values().length;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RowType rowType = RowType.values()[getItemViewType(position)];

		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_flight, parent, false);

			holder = new ViewHolder();
			holder.mDetailsContainer = Ui.findView(convertView, R.id.details_container);
			holder.mAirlineTextView = Ui.findView(convertView, R.id.airline_text_view);
			holder.mPriceTextView = Ui.findView(convertView, R.id.price_text_view);
			holder.mDepartureTimeTextView = Ui.findView(convertView, R.id.departure_time_text_view);
			holder.mArrivalTimeTextView = Ui.findView(convertView, R.id.arrival_time_text_view);
			holder.mFlightTripView = Ui.findView(convertView, R.id.flight_trip_view);

			convertView.setTag(holder);

			holder.mDetailsContainer.setOnClickListener(mDetailsClickListener);

			if (rowType == RowType.EXPANDED) {
				ViewGroup v = Ui.findView(convertView, R.id.expanded_details_container);
				v.setVisibility(View.VISIBLE);
				holder.mSeatsLeftTextView = Ui.findView(v, R.id.seats_left_text_view);
				holder.mDetailsButton = Ui.findView(v, R.id.details_button);
				holder.mSelectButton = Ui.findView(v, R.id.select_button);

				holder.mDetailsButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onExpandClick(v, ClickMode.DETAILS);
					}
				});

				holder.mSelectButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onExpandClick(v, ClickMode.SELECT);
					}
				});
			}
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		FlightTrip trip = getItem(position);
		FlightLeg leg = trip.getLeg(mLegPosition);

		holder.mDetailsContainer.setTag(position);

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

		// Extra configuration for expanded row types
		if (rowType == RowType.EXPANDED) {
			int seatsRemaining = trip.getSeatsRemaining();
			if (seatsRemaining <= SEATS_REMAINING_CUTOFF) {
				holder.mSeatsLeftTextView.setVisibility(View.VISIBLE);
				holder.mSeatsLeftTextView.setText(Html.fromHtml(mContext.getResources().getQuantityString(
						R.plurals.seats_left, seatsRemaining, seatsRemaining)));
			}
			else {
				holder.mSeatsLeftTextView.setVisibility(View.INVISIBLE);
			}

			setTags(holder.mDetailsButton, trip, leg, position);
			setTags(holder.mSelectButton, trip, leg, position);
		}

		return convertView;
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(mContext);
		return df.format(new Date(cal.getTimeInMillis()));
	}

	private static class ViewHolder {

		private ViewGroup mDetailsContainer;
		private TextView mAirlineTextView;
		private TextView mPriceTextView;
		private TextView mDepartureTimeTextView;
		private TextView mArrivalTimeTextView;
		private FlightTripView mFlightTripView;

		private TextView mSeatsLeftTextView;
		private View mDetailsButton;
		private View mSelectButton;
	}

	//////////////////////////////////////////////////////////////////////////
	// View.OnClickListener
	//
	// We implement a single one here so we don't need to create a bunch
	// of new OnClickListener objects every time a new row is shown.

	private OnClickListener mDetailsClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();

			if (mExpandedLeg == position) {
				mExpandedLeg = -1;
			}
			else {
				mExpandedLeg = position;
			}

			notifyDataSetChanged();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Adapter listener

	private enum ClickMode {
		DETAILS,
		SELECT
	}

	private void setTags(View v, FlightTrip trip, FlightLeg leg, int position) {
		v.setTag(R.id.tag_flight_trip, trip);
		v.setTag(R.id.tag_flight_leg, leg);
		v.setTag(R.id.tag_flight_trip_position, position);
	}

	private void onExpandClick(View v, ClickMode mode) {
		if (mListener != null) {
			FlightTrip trip = (FlightTrip) v.getTag(R.id.tag_flight_trip);
			FlightLeg leg = (FlightLeg) v.getTag(R.id.tag_flight_leg);
			int position = (Integer) v.getTag(R.id.tag_flight_trip_position);

			if (mode == ClickMode.DETAILS) {
				mListener.onDetailsClick(trip, leg, position);
			}
			else {
				mListener.onSelectClick(trip, leg, position);
			}
		}
	}

	public interface FlightAdapterListener {
		public void onDetailsClick(FlightTrip trip, FlightLeg leg, int position);

		public void onSelectClick(FlightTrip trip, FlightLeg leg, int position);
	}
}
