package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.R2;

import com.expedia.bookings.R2;


import butterknife.ButterKnife;
import butterknife.InjectView;

public class ShowMoreWithCountWidget extends android.widget.LinearLayout {
	public ShowMoreWithCountWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.show_more_with_count_layout, this);
	}

	@InjectView(R2.id.more_offer_count)
	android.widget.TextView moreOfferCount;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void setCount(String count) {
		moreOfferCount.setText(count);
	}
}
