package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class LaunchHeaderView extends RelativeLayout implements OnClickListener {
	private int mOffset;

	private OnClickListener mHotelOnClickListener;
	private OnClickListener mFlightOnClickListener;

	public LaunchHeaderView(Context context) {
		this(context, null);
	}

	public LaunchHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		Ui.findView(this, R.id.hotels_button).setOnClickListener(this);
		Ui.findView(this, R.id.flights_button).setOnClickListener(this);
	}

	public void setHotelOnClickListener(OnClickListener onClickListener) {
		mHotelOnClickListener = onClickListener;
	}

	public void setFlightOnClickListener(OnClickListener onClickListener) {
		mFlightOnClickListener = onClickListener;
	}

	public void show() {
		ObjectAnimator.ofFloat(this, "translationY", 0).start();
	}

	public void setOffset() {
		setOffset(mOffset, true);
	}

	public void setOffset(int offset) {
		setOffset(offset, false);
	}

	public void setOffset(int offset, boolean animate) {
		if (offset < -getHeight()) {
			offset = -getHeight();
		}

		mOffset = offset;

		if (animate) {
			ObjectAnimator.ofFloat(this, "translationY", offset).start();
		}
		else {
			ViewHelper.setTranslationY(this, offset);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.hotels_button: {
			if (mHotelOnClickListener != null) {
				mHotelOnClickListener.onClick(v);
			}
			break;
		}
		case R.id.flights_button: {
			if (mFlightOnClickListener != null) {
				mFlightOnClickListener.onClick(v);
			}
			break;
		}
		}
	}
}