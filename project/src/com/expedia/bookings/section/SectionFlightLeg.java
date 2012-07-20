package com.expedia.bookings.section;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightDetailsActivity;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class SectionFlightLeg extends LinearLayout implements ISection<FlightTripLeg> {

	ArrayList<SectionField<?, FlightTripLeg>> mFields = new ArrayList<SectionField<?, FlightTripLeg>>();

	private FlightTripLeg mTripLeg;

	private boolean mDeselectButtonEnabled;

	private SectionFlightLegListener mListener;

	Context mContext;

	public SectionFlightLeg(Context context) {
		super(context);
		init(context);
	}

	public SectionFlightLeg(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionFlightLeg(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		//Display fields
		mFields.add(this.mDetailsButton);
		mFields.add(this.mDeselectButton);
		mFields.add(this.mDisplayArrivalTime);
		mFields.add(this.mDisplayDepartureTime);
		mFields.add(this.mDisplayCarrierName);
		mFields.add(this.mDisplayFlightPrice);
		mFields.add(this.mDisplayArriveOrDepartWithDate);
	}
	
	public void setListener(SectionFlightLegListener listener) {
		mListener = listener;
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, FlightTripLeg> field : mFields) {
			field.bindField(this);
		}
	}

	@Override
	public void bind(FlightTripLeg leg) {
		//Update fields
		mTripLeg = leg;

		if (mTripLeg != null) {
			for (SectionField<?, FlightTripLeg> field : mFields) {
				field.bindData(mTripLeg);
			}
		}
	}

	public String getFormatedRelevantWaypointTime(Waypoint wp) {
		if (mContext != null && wp != null && wp.getMostRelevantDateTime() != null) {
			DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(mContext);
			return timeFormat.format(DateTimeUtils.getTimeInConfiguredTimeZone(mContext, wp.getMostRelevantDateTime()));
		}
		else {
			return "fail";
		}
	}

	public void setDeselectButtonEnabled(boolean enabled) {
		mDeselectButtonEnabled = enabled;
		mDeselectButton.bindData(mTripLeg);
	}

	private int getLegPosition() {
		FlightTrip trip = mTripLeg.getFlightTrip();
		FlightLeg leg = mTripLeg.getFlightLeg();

		int legCount = trip.getLegCount();
		for (int a = 0; a < legCount; a++) {
			if (leg.equals(trip.getLeg(a))) {
				return a;
			}
		}

		// Should never get here
		return -1;
	}

	// Convenience method that works while we only support 1-2 legs.
	private boolean isOutbound() {
		return getLegPosition() == 0;
	}

	//////////////////////////////////////
	////// DISPLAY FIELDS
	//////////////////////////////////////

	SectionField<TextView, FlightTripLeg> mDetailsButton = new SectionField<TextView, FlightTripLeg>(
			R.id.details_btn) {
		@Override
		public void onHasFieldAndData(TextView field, final FlightTripLeg data) {
			field.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, FlightDetailsActivity.class);
					intent.putExtra(FlightDetailsActivity.EXTRA_TRIP_KEY, data.getFlightTrip().getProductKey());
					intent.putExtra(FlightDetailsActivity.EXTRA_LEG_POSITION, getLegPosition());
					mContext.startActivity(intent);
				}
			});
		}
	};

	SectionField<TextView, FlightTripLeg> mDeselectButton = new SectionField<TextView, FlightTripLeg>(
			R.id.deselect_btn) {
		@Override
		public void onHasFieldAndData(TextView field, final FlightTripLeg data) {
			if (!mDeselectButtonEnabled) {
				field.setVisibility(View.GONE);
			}
			else {
				field.setVisibility(View.VISIBLE);
				field.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mListener != null) {
							mListener.onDeselect();
						}
					}
				});
			}
		}
	};

	SectionField<TextView, FlightTripLeg> mDisplayCarrierName = new SectionField<TextView, FlightTripLeg>(
			R.id.display_carrier_name) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTripLeg data) {
			FlightLeg leg = data.getFlightLeg();
			if (!TextUtils.isEmpty(leg.getAirlinesFormatted())) {
				field.setText(leg.getAirlinesFormatted());
			}
		}
	};

	SectionField<TextView, FlightTripLeg> mDisplayDepartureTime = new SectionField<TextView, FlightTripLeg>(
			R.id.display_departure_time) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTripLeg data) {
			field.setText(getFormatedRelevantWaypointTime(data.getFlightLeg().getFirstWaypoint()));
		}
	};

	SectionField<TextView, FlightTripLeg> mDisplayArrivalTime = new SectionField<TextView, FlightTripLeg>(
			R.id.display_arrival_time) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTripLeg data) {
			field.setText(getFormatedRelevantWaypointTime(data.getFlightLeg().getLastWaypoint()));
		}
	};

	SectionField<TextView, FlightTripLeg> mDisplayArriveOrDepartWithDate = new SectionField<TextView, FlightTripLeg>(
			R.id.display_arrive_or_depart_with_date) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTripLeg data) {
			FlightLeg leg = data.getFlightLeg();

			String formatted = "";
			String formatString = "";
			Calendar cal = null;
			if (isOutbound()) {
				cal = leg.getFirstWaypoint().getMostRelevantDateTime();
				formatString = getResources().getString(R.string.departs_with_date_TEMPLATE);
			}
			else {
				cal = leg.getLastWaypoint().getMostRelevantDateTime();
				formatString = getResources().getString(R.string.arrives_with_date_TEMPLATE);
			}

			String shortMonth = DateUtils.getMonthString(cal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
			String day = "" + cal.get(Calendar.DAY_OF_MONTH);
			formatted = String.format(formatString, shortMonth, day);
			field.setText(formatted);
		}
	};

	SectionField<TextView, FlightTripLeg> mDisplayFlightPrice = new SectionField<TextView, FlightTripLeg>(
			R.id.display_flight_price) {
		@Override
		public void onHasFieldAndData(TextView field, FlightTripLeg data) {
			field.setText(data.getFlightTrip().getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
		}
	};

	//It's not clear that this should even be here, as it does not take a value from the SelectedFlightLeg instance
	SectionField<ImageView, FlightTripLeg> mDisplayInboundOutboundArrow = new SectionField<ImageView, FlightTripLeg>(
			R.id.display_inbound_outbound_arrow) {
		@Override
		public void onHasFieldAndData(ImageView field, FlightTripLeg data) {
			// TODO: Support arrow assets one we have them.
		}
	};

	//////////////////////////////////////
	////// Listener
	//////////////////////////////////////

	public interface SectionFlightLegListener {
		public void onDeselect();
	}
}
