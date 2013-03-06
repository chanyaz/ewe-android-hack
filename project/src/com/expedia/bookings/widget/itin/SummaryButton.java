package com.expedia.bookings.widget.itin;

import android.view.View.OnClickListener;

public class SummaryButton {
	private int mIconResId;
	private String mText;
	private OnClickListener mOnClickListener;

	public SummaryButton(int iconResId, String text, OnClickListener onClickListener) {
		mIconResId = iconResId;
		mText = text;
		mOnClickListener = onClickListener;
	}

	public int getIconResId() {
		return mIconResId;
	}

	public String getText() {
		return mText;
	}

	public OnClickListener getOnClickListener() {
		return mOnClickListener;
	}
}
