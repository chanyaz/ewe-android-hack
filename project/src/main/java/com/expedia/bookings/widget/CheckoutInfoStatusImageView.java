package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.TravelerIconUtils;
import com.mobiata.android.Log;

/**
 *
 */
public class CheckoutInfoStatusImageView extends ImageView {

	private static final int[] STATE_CHECKOUT_INFO_COMPLETE = {R.attr.state_info_complete};

	private boolean mIsStatusComplete;
	private String mTravelerName;

	public CheckoutInfoStatusImageView(Context context) {
		super(context, null);
	}

	public CheckoutInfoStatusImageView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public CheckoutInfoStatusImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a =
			context.obtainStyledAttributes(
				attrs, R.styleable.CheckoutInfoStatusIcon, defStyle, 0);
		boolean isComplete = a
			.getBoolean(R.styleable.CheckoutInfoStatusIcon_state_info_complete, false);
		setStatusComplete(isComplete);
		a.recycle();
	}

	public boolean isStatusComplete() {
		return mIsStatusComplete;
	}

	public void setStatusComplete(boolean statusComplete) {
		if (mIsStatusComplete != statusComplete) {
			mIsStatusComplete = statusComplete;
			refreshDrawableState();
 		}
	}

	public String getTravelerName() {
		return mTravelerName;
	}

	public void setTravelerName(String name) {
		mTravelerName = name;
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isStatusComplete()) {
			mergeDrawableStates(drawableState, STATE_CHECKOUT_INFO_COMPLETE);
		}
		return drawableState;
	}
}
