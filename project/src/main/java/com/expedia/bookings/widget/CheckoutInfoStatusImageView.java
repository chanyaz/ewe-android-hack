package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.TravelerIconUtils;

/**
 *
 */
public class CheckoutInfoStatusImageView extends ImageView {

	private static final int[] STATE_CHECKOUT_INFO_COMPLETE = { R.attr.state_info_complete };

	private boolean mIsStatusComplete;

	public CheckoutInfoStatusImageView(Context context) {
		super(context, null);
	}

	public CheckoutInfoStatusImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
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

		setTraveler(null);
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

	public void setTraveler(Traveler traveler) {
		String fullName = traveler == null ? null : traveler.getFullName();
		setImageDrawable(new CheckoutInfoStatusDrawable(fullName));
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isStatusComplete()) {
			mergeDrawableStates(drawableState, STATE_CHECKOUT_INFO_COMPLETE);
		}
		return drawableState;
	}

	private class CheckoutInfoStatusDrawable extends StateListDrawable {
		public CheckoutInfoStatusDrawable(String travelerName) {
			super();
			Context context = getContext();
			Resources res = context.getResources();

			// Error state: state_info_complete=false
			addState(new int[] { -R.attr.state_info_complete },
				res.getDrawable(R.drawable.ic_checkout_error_state));

			// Default state: either the user's icon, or a generic icon
			Drawable drawable = Strings.isEmpty(travelerName)
				? res.getDrawable(R.drawable.ic_checkout_generic_traveler)
				: new BitmapDrawable(res, TravelerIconUtils.generateInitialIcon(context,
				travelerName, Color.parseColor("#FF373F4A"), false, true, 0));

			addState(new int[] { }, drawable);
		}
	}

}
