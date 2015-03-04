package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;

public class CarDriverCheckoutStatusLeftImageView extends ImageView {

	private static final int[] STATE_DEFAULT = { R.attr.state_default };
	private static final int[] STATE_COMPLETE = { R.attr.state_complete };
	private static final int[] STATE_INCOMPLETE = { R.attr.state_incomplete };

	private CarDriverWidget.DriverCheckoutStatus mStatus = CarDriverWidget.DriverCheckoutStatus.DEFAULT;

	public CarDriverCheckoutStatusLeftImageView(Context context) {
		super(context, null);
	}

	public CarDriverCheckoutStatusLeftImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CarDriverCheckoutStatusLeftImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwipeOutLayout, 0, 0);

		if (ta.hasValue(R.styleable.DriverInfo_state)) {
			CarDriverWidget.DriverCheckoutStatus checkoutStatus = CarDriverWidget.DriverCheckoutStatus.values()[ta
				.getInt(R.styleable.DriverInfo_state,
					CarDriverWidget.DriverCheckoutStatus.DEFAULT.ordinal())];
			mStatus = checkoutStatus;
		}

		setStatus(mStatus);
		setImageDrawable(new CheckoutInfoStatusDrawable());
	}

	public CarDriverWidget.DriverCheckoutStatus getStatus() {
		return mStatus;
	}

	public int[] getState() {
		if (mStatus == CarDriverWidget.DriverCheckoutStatus.DEFAULT) {
			return STATE_DEFAULT;
		}
		else if (mStatus == CarDriverWidget.DriverCheckoutStatus.COMPLETE) {
			return STATE_COMPLETE;
		}
		else {
			return STATE_INCOMPLETE;
		}
	}

	public void setStatus(CarDriverWidget.DriverCheckoutStatus status) {
		if (mStatus != status) {
			mStatus = status;
			refreshDrawableState();
		}
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
		mergeDrawableStates(drawableState, getState());
		return drawableState;
	}

	private class CheckoutInfoStatusDrawable extends StateListDrawable {
		public CheckoutInfoStatusDrawable() {
			super();
			Context context = getContext();
			Resources res = context.getResources();

			addState(STATE_DEFAULT, null);
			addState(STATE_COMPLETE, res.getDrawable(R.drawable.checkmark));
			addState(STATE_INCOMPLETE, res.getDrawable(R.drawable.incomplete));
		}
	}

}
