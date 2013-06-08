package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;

/**
 * This is a version of TextView that adds a checkmark when it is
 * clicked on.
 *
 * Used on the Confirmation screen.
 */
public class ConfirmationActionTextView extends TextView {

	private static final String INSTANCE_CLICKED = "INSTANCE_CLICKED";

	private OnClickListener mOnClickListener;

	private boolean mClicked;

	public ConfirmationActionTextView(Context context) {
		super(context);
		init();
	}

	public ConfirmationActionTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ConfirmationActionTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		super.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mClicked = true;
				updateState();

				if (mOnClickListener != null) {
					mOnClickListener.onClick(v);
				}
			}
		});

		updateState();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("superState", super.onSaveInstanceState());
		bundle.putBoolean(INSTANCE_CLICKED, mClicked);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		super.onRestoreInstanceState(bundle.getParcelable("superState"));
		mClicked = bundle.getBoolean(INSTANCE_CLICKED);

		updateState();
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnClickListener = l;
	}

	private void updateState() {
		Drawable[] drawables = getCompoundDrawables();

		if (mClicked) {
			drawables[2] = getResources().getDrawable(R.drawable.ic_confirmation_checkmark);
		}

		setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
	}
}
