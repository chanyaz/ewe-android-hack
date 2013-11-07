package com.expedia.bookings.widget;

import java.util.Set;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CheckBoxFilterWidget.OnCheckedChangeListener;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class AirportFilterWidget extends TextView {

	private PopupWindow mPopup;

	private int mLegNumber;
	private Set<String> mAirportCodes;
	private FlightFilter mFilter;
	private OnCheckedChangeListener mAirportCheckChangeListener;

	public AirportFilterWidget(Context context) {
		super(context);
		init();
	}

	public AirportFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AirportFilterWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleDropdown();
			}
		});
	}

	public void bind(int legNumber, Set<String> airportCodes, FlightFilter filter, OnCheckedChangeListener listener) {
		mLegNumber = legNumber;
		mAirportCodes = airportCodes;
		mFilter = filter;
		mAirportCheckChangeListener = listener;

		setVisibility(mAirportCodes.size() < 2 ? View.GONE : View.VISIBLE);
	}

	private void toggleDropdown() {
		if (mPopup != null && mPopup.isShowing()) {
			mPopup.dismiss();
		}
		else {
			View content = LayoutInflater.from(getContext()).inflate(R.layout.snippet_flight_airport_filter, null);
			ViewGroup vg = Ui.findView(content, R.id.airport_filter_container);
			mPopup = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
					true);
			mPopup.setBackgroundDrawable(new BitmapDrawable());
			mPopup.setOutsideTouchable(true);
			mPopup.setTouchable(true);

			// Add the checkbox widgets
			for (String code : mAirportCodes) {
				Airport airport = FlightStatsDbUtils.getAirport(code);
				String text = airport.mAirportCode + " - " + airport.mName;
				CheckBoxFilterWidget widget = new CheckBoxFilterWidget(getContext());
				widget.setDescription(text);
				widget.setTag(code);
				widget.setChecked(mFilter.containsDepartureAirportForLeg(mLegNumber, airport.mAirportCode));
				widget.setOnCheckedChangeListener(mAirportCheckChangeListener);
				vg.addView(widget);
			}

			// Set a click listener for the done button
			Button button = Ui.findView(content, R.id.airport_filter_done);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mPopup.dismiss();
				}
			});

			// Show the popup
			mPopup.showAsDropDown(this);
		}
	}

}
