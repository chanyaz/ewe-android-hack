package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.ToolbarListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarPaymentWidget extends CardView {

	public CarPaymentWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.payment_info_text)
	TextView paymentInfoText;

	@InjectView(R.id.payment_info)
	ViewGroup paymentInfoBlock;


	@OnClick(R.id.payment_info_card_view)
	public void onCardExpanded() {
		if (mToobarListener != null) {
			mToobarListener.onWidgetExpanded();
		}
		setExpanded(true);
	}

	private ToolbarListener mToobarListener;
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

	}

	public void setExpanded(boolean isExpanded) {
		if (isExpanded && mToobarListener != null) {
			mToobarListener.setActionBarTitle(getResources().getString(R.string.cars_payment_details_text));
		}
		paymentInfoText.setVisibility(isExpanded ? GONE : VISIBLE);
		paymentInfoBlock.setVisibility(isExpanded ? VISIBLE : GONE);
	}

	public void setToolbarListener(ToolbarListener listener) {
		mToobarListener = listener;
	}

}
