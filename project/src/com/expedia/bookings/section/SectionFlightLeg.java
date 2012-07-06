package com.expedia.bookings.section;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionFlightLeg extends LinearLayout implements ISection<FlightLeg> {

	ArrayList<SectionField<?, FlightLeg>> mFields = new ArrayList<SectionField<?, FlightLeg>>();

	FlightLeg mLeg;
	boolean mIsOutbound = false;

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
		mFields.add(this.mDisplayArrivalTime);
		mFields.add(this.mDisplayDepartureTime);
		mFields.add(this.mDisplayCarrierName);
		mFields.add(this.mDisplayFlightPrice);
		mFields.add(this.mDisplayArriveOrDepartWithDate);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		for (SectionField<?, FlightLeg> field : mFields) {
			field.bindField(this);
		}
	}

	@Override
	public void bind(FlightLeg leg) {
		//Update fields
		mLeg = leg;

		if (mLeg != null) {
			for (SectionField<?, FlightLeg> field : mFields) {
				field.bindData(mLeg);
			}
		}
	}

	public void setIsOutbound(boolean isOutbound) {
		mIsOutbound = isOutbound;
		if (mLeg != null) {
			//must rebind to adjust arrive/depart
			bind(mLeg);
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

	//////////////////////////////////////
	////// DISPLAY FIELDS
	//////////////////////////////////////

	SectionField<TextView, FlightLeg> mDisplayCarrierName = new SectionField<TextView, FlightLeg>(
			R.id.display_carrier_name) {
		@Override
		public void onHasFieldAndData(TextView field, FlightLeg data) {
			if (!TextUtils.isEmpty(data.getAirlinesFormatted())) {
				field.setText(data.getAirlinesFormatted());
			}
		}
	};

	SectionField<TextView, FlightLeg> mDisplayDepartureTime = new SectionField<TextView, FlightLeg>(
			R.id.display_departure_time) {
		@Override
		public void onHasFieldAndData(TextView field, FlightLeg data) {
			if (data.getSegments() != null && data.getSegmentCount() > 0) {
				field.setText(getFormatedRelevantWaypointTime(data.getSegment(0).mOrigin));
			}
		}
	};

	SectionField<TextView, FlightLeg> mDisplayArrivalTime = new SectionField<TextView, FlightLeg>(
			R.id.display_arrival_time) {
		@Override
		public void onHasFieldAndData(TextView field, FlightLeg data) {
			if (data.getSegments() != null && data.getSegmentCount() > 0) {
				field.setText(getFormatedRelevantWaypointTime(data.getSegment(data.getSegmentCount() - 1).mDestination));
			}
		}
	};

	SectionField<TextView, FlightLeg> mDisplayArriveOrDepartWithDate = new SectionField<TextView, FlightLeg>(
			R.id.display_arrive_or_depart_with_date) {
		@Override
		public void onHasFieldAndData(TextView field, FlightLeg data) {
			if (data.getSegments() != null && data.getSegmentCount() > 0) {
				String formatted = "";
				String formatString = "";
				Calendar cal = null;
				if (mIsOutbound) {
					cal = data.getSegment(0).mOrigin.getMostRelevantDateTime();
					formatString = getResources().getString(R.string.departs_with_date_TEMPLATE);
				}
				else {
					cal = data.getSegment(data.getSegmentCount() - 1).mDestination
							.getMostRelevantDateTime();

					formatString = getResources().getString(R.string.arrives_with_date_TEMPLATE);
				}

				String shortMonth = DateUtils.getMonthString(cal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
				String day = "" + cal.get(Calendar.DAY_OF_MONTH);
				formatted = String.format(formatString, shortMonth, day);
				field.setText(formatted);
			}
		}
	};

	SectionField<TextView, FlightLeg> mDisplayFlightPrice = new SectionField<TextView, FlightLeg>(
			R.id.display_flight_price) {
		@Override
		public void onHasFieldAndData(TextView field, FlightLeg data) {
			field.setText("$000");
		}
	};

	//It's not clear that this should even be here, as it does not take a value from the FlightLeg instance
	SectionField<ImageView, FlightLeg> mDisplayInboundOutboundArrow = new SectionField<ImageView, FlightLeg>(
			R.id.display_inbound_outbound_arrow) {
		@Override
		public void onHasFieldAndData(ImageView field, FlightLeg data) {
			if (mIsOutbound) {
				//TODO:Get good arrow drawables...
			}
			else {

			}
		}
	};

	//////////////////////////////////////
	////// EDIT FIELDS
	//////////////////////////////////////

}
