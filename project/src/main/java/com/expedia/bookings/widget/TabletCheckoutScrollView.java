package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.enums.CheckoutState;

public class TabletCheckoutScrollView extends ScrollView {

	private CheckoutState mState;

	public TabletCheckoutScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TabletCheckoutScrollView(Context context) {
		super(context);
	}

	public TabletCheckoutScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setCheckoutState(CheckoutState state) {
		mState = state;
	}

	private boolean isShowingFTCResortFeeInfo() {
		return Db.getTripBucket().getHotel() != null && Db.getTripBucket().getHotel().getRate().showResortFeesMessaging();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mState == CheckoutState.READY_FOR_CHECKOUT && isShowingFTCResortFeeInfo()) {
			fullScroll(View.FOCUS_DOWN);
			smoothScrollTo(0, getBottom());
		}
	}
}
