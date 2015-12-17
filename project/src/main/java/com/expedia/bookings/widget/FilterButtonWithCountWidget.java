package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FilterButtonWithCountWidget extends LinearLayout {

	public FilterButtonWithCountWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		inflate(context, R.layout.widget_filter_button_with_count, this);
	}

	@InjectView(R.id.filter_number_text)
	TextView filterNumber;

	@InjectView(R.id.filter_icon)
	View filterIcon;

	@InjectView(R.id.filter_text)
	TextView filterText;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		filterNumber.setVisibility(GONE);
	}

	public void showNumberOfFilters(int number) {
		filterNumber.setText(String.valueOf(number));
		boolean hasCheckedFilters = number > 0;
		filterNumber.setVisibility(hasCheckedFilters ? VISIBLE : GONE);
		filterIcon.setVisibility(hasCheckedFilters ? GONE : VISIBLE);
	}

	public void setFilterText(String text) {
		filterText.setText(text);
	}
}
