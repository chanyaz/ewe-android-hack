package com.expedia.bookings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;

@SuppressLint("CustomViewStyleable")
public class ContactDetailsCompletenessStatusImageView extends ImageView {

	private static final int[] STATE_DEFAULT = { R.attr.state_default };
	private static final int[] STATE_COMPLETE = { R.attr.state_complete };
	private static final int[] STATE_INCOMPLETE = { R.attr.state_incomplete };

	private ContactDetailsCompletenessStatus mStatus = ContactDetailsCompletenessStatus.DEFAULT;

	public ContactDetailsCompletenessStatusImageView(Context context) {
		super(context, null);
	}

	public ContactDetailsCompletenessStatusImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ContactDetailsCompletenessStatusImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ContactDetailsCompleteness, 0, 0);

		if (ta.hasValue(R.styleable.ContactDetailsCompleteness_state)) {
			ContactDetailsCompletenessStatus checkoutStatus = ContactDetailsCompletenessStatus.values()[ta
				.getInt(R.styleable.ContactDetailsCompleteness_state,
					ContactDetailsCompletenessStatus.DEFAULT.ordinal())];
			mStatus = checkoutStatus;
		}

		setStatus(mStatus);
		setImageDrawable(new CheckoutInfoStatusDrawable());
	}

	public ContactDetailsCompletenessStatus getStatus() {
		return mStatus;
	}

	public int[] getState() {
		if (mStatus == ContactDetailsCompletenessStatus.DEFAULT) {
			return STATE_DEFAULT;
		}
		else if (mStatus == ContactDetailsCompletenessStatus.COMPLETE) {
			return STATE_COMPLETE;
		}
		else {
			return STATE_INCOMPLETE;
		}
	}

	public void setStatus(ContactDetailsCompletenessStatus status) {
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
			addState(STATE_COMPLETE, res.getDrawable(R.drawable.validated));
			addState(STATE_INCOMPLETE, res.getDrawable(R.drawable.invalid));
		}
	}

}
