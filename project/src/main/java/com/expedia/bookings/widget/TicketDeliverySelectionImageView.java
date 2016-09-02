package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;

public class TicketDeliverySelectionImageView extends ImageView {

	private static final int[] STATE_DEFAULT = { R.attr.unselected_state };
	private static final int[] STATE_SELECTED = { R.attr.selected_state };

	private TicketDeliverySelectionStatus mStatus = TicketDeliverySelectionStatus.UNSELECTED;

	public TicketDeliverySelectionImageView(Context context) {
		super(context, null);
	}

	public TicketDeliverySelectionImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TicketDeliverySelectionImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TicketDeliverySelectionImageView, 0, 0);

		try {
			if (ta.hasValue(R.styleable.TicketDeliverySelectionImageView_selection)) {
				TicketDeliverySelectionStatus checkoutStatus = TicketDeliverySelectionStatus.values()[ta
					.getInt(R.styleable.TicketDeliverySelectionImageView_unselected_state,
						TicketDeliverySelectionStatus.UNSELECTED.ordinal())];
				mStatus = checkoutStatus;
			}
			setStatus(mStatus);
			setImageDrawable(new CheckoutInfoStatusDrawable());
		}
		finally {
			ta.recycle();
		}
	}

	public TicketDeliverySelectionStatus getStatus() {
		return mStatus;
	}

	public int[] getState() {
		if (mStatus == TicketDeliverySelectionStatus.SELECTED) {
			return STATE_SELECTED;
		}
		else {
			return STATE_DEFAULT;
		}
	}

	public void setStatus(TicketDeliverySelectionStatus status) {
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
			addState(STATE_DEFAULT, ContextCompat.getDrawable(context, R.drawable.rail_tdo_unselected));
			addState(STATE_SELECTED, ContextCompat.getDrawable(context, R.drawable.rail_tdo_selected));
		}
	}
}
