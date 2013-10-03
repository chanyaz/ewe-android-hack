package com.expedia.bookings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;

public class CheckBoxFilterWidget extends LinearLayout {

	private CheckBox mCheckBox;
	private android.widget.TextView mPriceTextView;

	private OnCheckedChangeListener mOnCheckedChangeListener;

	public CheckBoxFilterWidget(Context context) {
		super(context);

		setOrientation(HORIZONTAL);

		LayoutInflater.from(context).inflate(R.layout.row_filter_refinement, this, true);
		mCheckBox = Ui.findView(this, R.id.filter_refinement_checkbox);
		mPriceTextView = Ui.findView(this, R.id.filter_refinement_textview);

		// the bind methods take care of restoring the enabled/disabled state
		mCheckBox.setSaveEnabled(false);
	}

	public CheckBoxFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		throw new UnsupportedOperationException("This class does not support inflation via XML. Use Java constructor");
	}

	@SuppressLint("NewApi")
	public CheckBoxFilterWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		throw new UnsupportedOperationException("This class does not support inflation via XML. Use Java constructor");
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mCheckBox.setOnCheckedChangeListener(listener);
		mOnCheckedChangeListener = listener;
	}

	/**
	 * Set some data to attach to this view. Call getTag() on the view returned in your listener's onCheckedChange()
	 * to retrieve this data.
	 *
	 * We override the default behavior to pass the tag down to the checkbox. This means we can leverage the system
	 * OnCheckedChange interface and the listener implementations can easily retrieve the tag.
	 * @param tag
	 */
	@Override
	public void setTag(Object tag) {
		mCheckBox.setTag(tag);
	}

	public void bindFlight(FlightFilter filter, FlightTrip trip, int legNumber, boolean enabled) {
		mCheckBox.setOnCheckedChangeListener(null);
		mCheckBox.setChecked(filter.getPreferredAirlines().contains(trip.getLeg(legNumber).getFirstAirlineCode()));
		mCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);

		mCheckBox.setText(trip.getLeg(legNumber).getAirlinesFormatted());

		bindPrice(trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));

		bindEnabled(enabled);
	}

	public void bindHotel(Property property) {
		String description = property.getLocation().getDescription();
		String price = StrUtils.formatHotelPrice(property.getLowestRate().getDisplayPrice());

		mCheckBox.setOnCheckedChangeListener(null);
		mCheckBox.setChecked(true); // TODO fix this
		mCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCheckBox.setText(description);
		bindPrice(price);
	}

	private void bindEnabled(boolean isEnabled) {
		mCheckBox.setEnabled(isEnabled);
		mPriceTextView.setEnabled(isEnabled);
	}

	private void bindPrice(String price) {
		// Build and colorize "From $200" string
		ForegroundColorSpan graySpan = new ForegroundColorSpan(Color.rgb(0x05, 0x58, 0xc4)); // TODO get color from design and cache this span?
		String priceString = getResources().getString(R.string.From_x_TEMPLATE, price);
		SpannableStringBuilder builder = new SpannableStringBuilder(priceString);
		int start = priceString.indexOf(price);
		int end = start + price.length();
		builder.setSpan(graySpan, start, end, 0);
		mPriceTextView.setText(builder);
	}

}
