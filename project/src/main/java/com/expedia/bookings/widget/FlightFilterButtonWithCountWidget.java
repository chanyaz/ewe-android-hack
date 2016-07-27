package com.expedia.bookings.widget;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;

public class FlightFilterButtonWithCountWidget extends FilterButtonWithCountWidget {

	public FlightFilterButtonWithCountWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setTextAndFilterIconColor(@ColorRes int color) {
		int textAndFilterIconColor = ContextCompat.getColor(getContext(), color);
		filterText.setTextColor(textAndFilterIconColor);
		int filterNumberColor = ContextCompat.getColor(getContext(), R.color.filter_number_text_color);
		filterNumber.setBackgroundResource(R.drawable.filter_number_bg_white);
		filterNumber.setTextColor(filterNumberColor);
		((ImageView) filterIcon).setColorFilter(textAndFilterIconColor);
	}
}
