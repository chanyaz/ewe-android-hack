package com.expedia.bookings.section;

import java.text.DateFormat;
import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionDisplayFlightLeg extends LinearLayout implements ISection<FlightLeg> {

	TextView mCarrierName;
	TextView mDepartureTime;
	TextView mArrivalTime;
	TextView mFlightPrice;
	TextView mArriveDepartWithDate;
	ImageView mInBoundOutboundArrow;

	FlightLeg mLeg;
	boolean mIsOutbound = false;

	Context mContext;

	public SectionDisplayFlightLeg(Context context) {
		super(context);
		init(context);
	}

	public SectionDisplayFlightLeg(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionDisplayFlightLeg(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mCarrierName = Ui.findView(this, R.id.carrier_name);
		mDepartureTime = Ui.findView(this, R.id.departure_time);
		mArrivalTime = Ui.findView(this, R.id.arrival_time);
		mFlightPrice = Ui.findView(this, R.id.flight_price);
		mArriveDepartWithDate = Ui.findView(this, R.id.arrive_or_depart_with_date);
		mInBoundOutboundArrow = Ui.findView(this, R.id.inbound_outbound_arrow);
	}

	@Override
	public void bind(FlightLeg leg) {
		mLeg = leg;

		if (mLeg != null) {

			if (mCarrierName != null && mLeg.getAirlinesFormatted() != null) {
				mCarrierName.setText(mLeg.getAirlinesFormatted());
			}

			if (mLeg.getSegmentCount() > 0) {

				if (mDepartureTime != null) {
					mDepartureTime.setText(getFormatedRelevantWaypointTime(mLeg.getSegment(0).mOrigin));
				}
				if (mArrivalTime != null) {
					mArrivalTime
							.setText(getFormatedRelevantWaypointTime(mLeg.getSegment(mLeg.getSegmentCount() - 1).mDestination));
				}

				//TODO:Set the imageView to have the correct arrow and add more error checking
				if (mArriveDepartWithDate != null) {
					if (mIsOutbound) {
						Calendar cal = mLeg.getSegment(0).mOrigin.getMostRelevantDateTime();
						String shortMonth = DateUtils.getMonthString(cal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
						String day = "" + cal.get(Calendar.DAY_OF_MONTH);

						String formatted = String.format(getResources().getString(R.string.departs_with_date_TEMPLATE),
								shortMonth, day);
						mArriveDepartWithDate.setText(formatted);
					}
					else {
						Calendar cal = mLeg.getSegment(mLeg.getSegmentCount() - 1).mDestination
								.getMostRelevantDateTime();
						String shortMonth = DateUtils.getMonthString(cal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
						String day = "" + cal.get(Calendar.DAY_OF_MONTH);

						String formatted = String.format(getResources().getString(R.string.arrives_with_date_TEMPLATE),
								shortMonth, day);
						mArriveDepartWithDate.setText(formatted);
					}
				}
			}

			//TODO:Individual data...
			if (mFlightPrice != null) {
				mFlightPrice.setText("$000");
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

}
