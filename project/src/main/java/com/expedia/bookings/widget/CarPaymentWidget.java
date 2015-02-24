package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarPaymentWidget extends CardView {

	public CarPaymentWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.payment_info_text)
	TextView paymentInfoText;

	@InjectView(R.id.payment_info)
	ViewGroup paymentInfoBlock;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

	}

	public void setExpanded(boolean isExpanded) {
		paymentInfoText.setVisibility(isExpanded ? GONE : VISIBLE);
		paymentInfoBlock.setVisibility(isExpanded ? VISIBLE : GONE);
	}

}
