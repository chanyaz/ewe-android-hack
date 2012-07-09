package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class FlightAdapter extends BaseAdapter {

	private static final int SEATS_REMAINING_CUTOFF = 5;

	private static final int ANIM_EXPAND_CONTRACT_DURATION = 300;

	private static final Interpolator ANIMATION_INTERPOLATOR = new AccelerateDecelerateInterpolator();

	private static final String STATE_EXPANDED_LEG = "STATE_EXPANDED_LEG";

	private static final DecimalFormat mDaySpanFormatter = new DecimalFormat("#");

	static {
		// TODO: Should this be localized in some way?
		mDaySpanFormatter.setPositivePrefix("+");
	}

	private enum RowType {
		NORMAL,
		EXPANDING,
		EXPANDED,
		CONTRACTING
	}

	private Context mContext;
	private Resources mResources;

	private LayoutInflater mInflater;

	private FlightAdapterListener mListener;

	private FlightTripQuery mFlightTripQuery;

	private Calendar mMinTime;
	private Calendar mMaxTime;

	private int mLegPosition;

	private int mExpandedLeg = -1;
	private int mExpandingLeg = -1;
	private int mContractingLeg = -1;
	private ValueAnimator mExpandAnim;
	private ValueAnimator mContractAnim;

	// We need to hold onto this value to set the correct height for the
	// final "expanded" view, once we get to it
	private int mAnimViewHeight;

	public FlightAdapter(Context context, Bundle savedInstanceState) {
		mContext = context;
		mResources = context.getResources();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		restoreInstanceState(savedInstanceState);
	}

	public void saveInstanceState(Bundle outState) {
		// Save either the expanded leg or the one that *is* expanding as the expanded leg
		outState.putInt(STATE_EXPANDED_LEG, (mExpandedLeg != -1) ? mExpandedLeg : mExpandingLeg);
	}

	public void restoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mExpandedLeg = savedInstanceState.getInt(STATE_EXPANDED_LEG, mExpandedLeg);
		}
	}

	public void setListener(FlightAdapterListener listener) {
		mListener = listener;
	}

	public void setFlightTripQuery(FlightTripQuery query) {
		if (query != mFlightTripQuery) {
			if (mFlightTripQuery != null) {
				query.unregisterDataSetObserver(mDataSetObserver);
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

	public int getExpandedLegPosition() {
		return mExpandedLeg;
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
	public int getItemViewType(int position) {
		if (position == mExpandedLeg) {
			return RowType.EXPANDED.ordinal();
		}
		else if (position == mExpandingLeg) {
			return RowType.EXPANDING.ordinal();
		}
		else if (position == mContractingLeg) {
			return RowType.CONTRACTING.ordinal();
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
	public boolean isEnabled(int position) {
		RowType rowType = RowType.values()[getItemViewType(position)];

		// Disable animating rows - don't let them get click events
		if (rowType == RowType.EXPANDING || rowType == RowType.CONTRACTING) {
			return false;
		}

		return true;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
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
			holder.mMultiDayTextView = Ui.findView(convertView, R.id.multi_day_text_view);
			holder.mFlightTripView = Ui.findView(convertView, R.id.flight_trip_view);

			convertView.setTag(holder);

			// We don't want the details container to be clickable mid-animation
			if (rowType != RowType.CONTRACTING && rowType != RowType.EXPANDING) {
				holder.mDetailsContainer.setOnClickListener(mDetailsClickListener);
			}

			if (renderExpandedDetails(rowType)) {
				// Ensure that the details container is at the front (so that the animation shows behind it)
				holder.mDetailsContainer.bringToFront();

				holder.mAnimContainer = Ui.findView(convertView, R.id.anim_container);

				ViewGroup v = holder.mExpandedDetailsContainer = Ui.findView(convertView,
						R.id.expanded_details_container);
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

		// Extra configuration for expanded row types
		if (renderExpandedDetails(rowType)) {
			int seatsRemaining = trip.getSeatsRemaining();
			if (seatsRemaining > 0 && seatsRemaining <= SEATS_REMAINING_CUTOFF) {
				holder.mSeatsLeftTextView.setVisibility(View.VISIBLE);
				holder.mSeatsLeftTextView.setText(Html.fromHtml(mContext.getResources().getQuantityString(
						R.plurals.seats_left, seatsRemaining, seatsRemaining)));
			}
			else {
				holder.mSeatsLeftTextView.setVisibility(View.INVISIBLE);
			}

			setTags(holder.mDetailsButton, trip, leg, position);
			setTags(holder.mSelectButton, trip, leg, position);

			holder.mAnimContainer.getLayoutParams().height = mAnimViewHeight;
		}

		// Do animations if this row has just started expanding or contracting
		if (rowType == RowType.EXPANDING && mExpandAnim == null) {
			holder.mExpandedDetailsContainer.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

			mAnimViewHeight = holder.mExpandedDetailsContainer.getMeasuredHeight();
			mExpandAnim = ValueAnimator.ofInt(0, holder.mExpandedDetailsContainer.getMeasuredHeight());
			mExpandAnim.addUpdateListener(new HeightUpdateListener(holder.mAnimContainer));
			mExpandAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mExpandedLeg = position;
					mExpandingLeg = -1;
					mExpandAnim = null;
					notifyDataSetChanged();
				}
			});
			mExpandAnim.setDuration(ANIM_EXPAND_CONTRACT_DURATION);
			mExpandAnim.setInterpolator(ANIMATION_INTERPOLATOR);
			mExpandAnim.start();
		}
		else if (rowType == RowType.CONTRACTING && mContractAnim == null) {
			holder.mExpandedDetailsContainer.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

			mContractAnim = ValueAnimator.ofInt(holder.mExpandedDetailsContainer.getMeasuredHeight(), 0);
			mContractAnim.addUpdateListener(new HeightUpdateListener(holder.mAnimContainer));
			mContractAnim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mContractingLeg = -1;
					mContractAnim = null;
					notifyDataSetChanged();
				}
			});
			mContractAnim.setDuration(ANIM_EXPAND_CONTRACT_DURATION);
			mContractAnim.setInterpolator(ANIMATION_INTERPOLATOR);
			mContractAnim.start();
		}

		return convertView;
	}

	private boolean renderExpandedDetails(RowType rowType) {
		return rowType == RowType.EXPANDED || rowType == RowType.EXPANDING || rowType == RowType.CONTRACTING;
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(mContext);
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}

	private static class ViewHolder {

		private ViewGroup mDetailsContainer;
		private TextView mAirlineTextView;
		private TextView mPriceTextView;
		private TextView mDepartureTimeTextView;
		private TextView mArrivalTimeTextView;
		private TextView mMultiDayTextView;
		private FlightTripView mFlightTripView;

		private View mAnimContainer;

		private ViewGroup mExpandedDetailsContainer;
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
				mContractingLeg = position;
			}
			else {
				mExpandingLeg = position;

				if (mExpandedLeg != -1) {
					mContractingLeg = mExpandedLeg;
				}
			}

			mExpandedLeg = -1;

			notifyDataSetChanged();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Animator listeners

	private class HeightUpdateListener implements AnimatorUpdateListener {
		private View mView;

		public HeightUpdateListener(View view) {
			mView = view;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animator) {
			int val = (Integer) animator.getAnimatedValue();
			mView.getLayoutParams().height = val;
			mView.requestLayout();
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
