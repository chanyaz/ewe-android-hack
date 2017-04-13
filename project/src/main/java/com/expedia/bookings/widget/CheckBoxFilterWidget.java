package com.expedia.bookings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;

public class CheckBoxFilterWidget extends LinearLayout implements Checkable, Comparable<CheckBoxFilterWidget> {

	private CharSequence mDescription;
	private Money mPrice;
	private boolean mUsePriceTemplate;

	private CheckBox mCheckBox;
	private android.widget.TextView mPriceTextView;

	private OnCheckedChangeListener mOnCheckedChangeListener;

	public CheckBoxFilterWidget(Context context) {
		super(context);

		setOrientation(HORIZONTAL);

		Ui.inflate(R.layout.row_filter_refinement, this, true);
		mCheckBox = Ui.findView(this, R.id.filter_refinement_checkbox);
		mPriceTextView = Ui.findView(this, R.id.filter_refinement_textview);

		mCheckBox.setOnCheckedChangeListener(mLocalCheckedChangeListener);

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
		mOnCheckedChangeListener = listener;
	}

	@Override
	public void setChecked(boolean isChecked) {
		mCheckBox.setChecked(isChecked);
	}

	@Override
	public boolean isChecked() {
		return mCheckBox.isChecked();
	}

	@Override
	public void toggle() {
		setChecked(!isChecked());
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		mCheckBox.setEnabled(isEnabled);
		mPriceTextView.setEnabled(isEnabled);
	}

	@Override
	public int compareTo(CheckBoxFilterWidget another) {
		// Use "~" here so an empty location, "Other Areas", is sorted as last
		String a = mDescription == null ? "~" : mDescription.toString();
		String b = another.mDescription == null ? "~" : another.mDescription.toString();
		return a.compareTo(b);
	}

	public void setDescription(CharSequence description) {
		mDescription = description;
		buildDescriptionString();
	}

	public void setPrice(Money money) {
		setPrice(money, true);
	}

	public void setPrice(Money money, boolean usePriceTemplate) {
		mPrice = money;
		mUsePriceTemplate = usePriceTemplate;
		if (mPrice != null) {
			buildPriceString();
		}
	}

	public void setPriceIfLower(Money money) {
		if (mPrice == null || money.compareTo(mPrice) < 0) {
			setPrice(money);
		}
	}

	public void bindFlight(FlightFilter filter, String airlineCode, FlightTrip trip) {
		boolean isChecked = filter.getPreferredAirlines().contains(airlineCode);
		SpannableBuilder sb = new SpannableBuilder();
		String airlineName = Db.getAirline(airlineCode).mAirlineName;
		sb.append(airlineName, FontCache.getSpan(FontCache.Font.ROBOTO_REGULAR));

		setChecked(isChecked);
		setDescription(sb.build());
		setPrice(trip.getAverageTotalFare());
	}

	private void buildDescriptionString() {
		if (TextUtils.isEmpty(mDescription)) {
			mCheckBox.setText(getContext().getString(R.string.Other_Areas));
		}
		else {
			mCheckBox.setText(mDescription, android.widget.TextView.BufferType.SPANNABLE);
		}
	}

	// We're using a custom span instead just another TextView here
	// to support languages where "From $234" is translated in a different order,
	// such as "234 xxxx" or "xxx 234 yyyy"
	private void buildPriceString() {
		String str = mPrice.getFormattedMoney(Money.F_NO_DECIMAL);

		// Build and colorize "From $200" string
		if (mUsePriceTemplate) {
			String priceString = getResources().getString(R.string.From_x_TEMPLATE, str);
			SpannableStringBuilder builder = new SpannableStringBuilder(priceString);
			int start = priceString.indexOf(str);
			int end = start + str.length();

			ForegroundColorSpan span = new ForegroundColorSpan(getResources()
				.getColorStateList(R.color.tablet_filter_price_text));
			builder.setSpan(span, start, end, 0);
			mPriceTextView.setText(builder);
		}
		else {
			mPriceTextView.setText(str);
			mPriceTextView.setTextColor(getResources()
				.getColorStateList(R.color.tablet_filter_price_text));

		}
	}

	private CompoundButton.OnCheckedChangeListener mLocalCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		private boolean mBroadcasting = false;

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// Avoid infinite recursions if setChecked() is called from a listener
			if (mBroadcasting) {
				return;
			}

			mBroadcasting = true;
			if (mOnCheckedChangeListener != null) {
				mOnCheckedChangeListener.onCheckedChanged(CheckBoxFilterWidget.this, isChecked);
			}

			mBroadcasting = false;
		}
	};

	public interface OnCheckedChangeListener {
		void onCheckedChanged(CheckBoxFilterWidget view, boolean isChecked);
	}

	public class ForegroundColorSpan extends CharacterStyle implements UpdateAppearance {

		private final ColorStateList mColor;

		public ForegroundColorSpan(ColorStateList color) {
			mColor = color;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			int[] drawState = CheckBoxFilterWidget.this.getDrawableState();
			int color = mColor.getColorForState(drawState, 0);
			ds.setColor(color);
		}
	}
}
