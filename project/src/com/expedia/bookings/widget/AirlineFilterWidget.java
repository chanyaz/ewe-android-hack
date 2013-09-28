package com.expedia.bookings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.Ui;

public class AirlineFilterWidget extends LinearLayout implements OnCheckedChangeListener {

	private CheckBox mCheckBox;
	private TextView mAirlineTv;
	private TextView mPriceTv;

	private FlightTrip mFlightTrip;
	private FlightFilter mFilter;
	private int mLegNumber;

	public AirlineFilterWidget(Context context) {
		super(context);

		LayoutInflater.from(context).inflate(R.layout.widget_airline_filter, this, true);

		mCheckBox = Ui.findView(this, R.id.section_airline_filter_checkbox);
		mCheckBox.setSaveEnabled(false); // we restore state based on FlightFilter
		mCheckBox.setOnCheckedChangeListener(this);

		mAirlineTv = Ui.findView(this, R.id.section_airline_filter_airline);
		mPriceTv = Ui.findView(this, R.id.section_airline_filter_price);
	}

	public AirlineFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		throw new IllegalArgumentException("This class does not support inflation via XML. Use Java constructor");
	}

	@SuppressLint("NewApi")
	public AirlineFilterWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		throw new IllegalArgumentException("This class does not support inflation via XML. Use Java constructor");
	}

	public void bind(FlightFilter filter, FlightTrip trip, int legNumber, boolean enabled) {
		mFilter = filter;
		mFlightTrip = trip;
		mLegNumber = legNumber;

		mCheckBox.setOnCheckedChangeListener(null);
		mCheckBox.setChecked(filter.getPreferredAirlines().contains(trip.getLeg(legNumber).getFirstAirlineCode()));
		mCheckBox.setOnCheckedChangeListener(this);

		mAirlineTv.setText(trip.getLeg(legNumber).getAirlinesFormatted());
		if (enabled) {
			mPriceTv.setText(trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
		}
		else {
			mPriceTv.setText("");
		}

		mCheckBox.setEnabled(enabled);
		mAirlineTv.setEnabled(enabled);
		mPriceTv.setEnabled(enabled);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mFilter.setPreferredAirline(mFlightTrip.getLeg(mLegNumber).getFirstAirlineCode(), isChecked);
		mFilter.notifyFilterChanged();
	}

}
