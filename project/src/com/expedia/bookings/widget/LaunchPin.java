package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.graphics.RoundBitmapDrawable;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.mobiata.android.util.ViewUtils;

public class LaunchPin extends TextView {

	public LaunchPin(Context context) {
		super(context);
		init(context, null, 0);
	}

	public LaunchPin(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public LaunchPin(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
	}

	public void bind(LaunchLocation location) {
		setPinText(location.title);
		setPinImage(location.drawableId);
	}

	/**
	 * Returns a Rect matching the global location of the circle on the screen.
	 * @return
	 */
	public Rect getGlobalOrigin() {
		Rect origin = ScreenPositionUtils.getGlobalScreenPosition(this);
		int size = getResources().getDimensionPixelSize(R.dimen.launch_pin_size);
		origin.bottom = origin.top + size;
		origin.left = (origin.left + origin.right - size) / 2;
		origin.right = origin.left + size;
		return origin;
	}

	private void setPinText(String upper) {
		TextAppearanceSpan upperSpan = new TextAppearanceSpan(getContext(), R.style.MapPinUpperTextAppearance);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(upper, upperSpan);

		setText(sb.build(), android.widget.TextView.BufferType.SPANNABLE);
	}

	private void setPinImage(int drawableId) {
		//TODO: float size = getResources().getDimensionPixelSize(R.dimen.launch_pin_size);
		Drawable d = new RoundBitmapDrawable(getContext(), R.drawable.mappin_madrid);

		setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
	}

}
