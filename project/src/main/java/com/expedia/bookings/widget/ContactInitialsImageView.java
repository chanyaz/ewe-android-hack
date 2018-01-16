package com.expedia.bookings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.TravelerIconUtils;
import com.expedia.bookings.utils.Ui;

@SuppressLint("CustomViewStyleable")
public class ContactInitialsImageView extends ImageView {

	private static final int[] STATE_DEFAULT_INCOMPLETE = {R.attr.state_default, R.attr.state_incomplete};
	private static final int[] STATE_COMPLETE = {R.attr.state_complete};

	private ContactDetailsCompletenessStatus mStatus = ContactDetailsCompletenessStatus.DEFAULT;

	public ContactInitialsImageView(Context context) {
		super(context, null);
	}

	public ContactInitialsImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ContactInitialsImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ContactDetailsCompleteness, 0, 0);

		if (ta.hasValue(R.styleable.ContactDetailsCompleteness_state)) {
			ContactDetailsCompletenessStatus direction = ContactDetailsCompletenessStatus.values()[ta.getInt(R.styleable.ContactDetailsCompleteness_state,
				ContactDetailsCompletenessStatus.DEFAULT.ordinal())];
			mStatus = direction;
		}

		setStatus(mStatus);
		setTraveler(null);
	}

	public ContactDetailsCompletenessStatus getStatus() {
		return mStatus;
	}

	public int[] getState() {
		if (mStatus == ContactDetailsCompletenessStatus.DEFAULT || mStatus == ContactDetailsCompletenessStatus.INCOMPLETE) {
			return STATE_DEFAULT_INCOMPLETE;
		}
		else {
			return STATE_COMPLETE;
		}
	}

	public void setStatus(ContactDetailsCompletenessStatus status) {
		if (mStatus != status) {
			mStatus = status;
			refreshDrawableState();
		}
	}

	public void setTraveler(Traveler traveler) {
		String fullName = traveler == null ? null : traveler.getFullName();
		setImageDrawable(new CheckoutInfoStatusDrawable(fullName));
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
		mergeDrawableStates(drawableState, getState());
		return drawableState;
	}

	private class CheckoutInfoStatusDrawable extends StateListDrawable {
		public CheckoutInfoStatusDrawable(String travelerName) {
			super();
			Context context = getContext();
			Resources res = context.getResources();

			addState(STATE_DEFAULT_INCOMPLETE, res.getDrawable(R.drawable.driver_large));
			Drawable drawable = Strings.isEmpty(travelerName)
				? res.getDrawable(R.drawable.driver_large)
				: new BitmapDrawable(res, TravelerIconUtils.generateInitialIcon(context,
					travelerName, Ui.obtainThemeColor(getContext(), R.attr.primary_color), true, false, 0));
			addState(STATE_COMPLETE, drawable );
		}
	}

}
