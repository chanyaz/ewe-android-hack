package com.expedia.bookings.widget.itin;

import android.view.View;
import android.view.View.OnClickListener;

public class SummaryButton {
	private int mIconResId;
	private String mText;
	private String mContentDescription;
	private boolean mShouldShowPopup;
	private OnClickListener mOnClickListener;
	private View mPopupContentView;
	private OnClickListener mPopupOnClickListener;

	public SummaryButton(int iconResId, String text, OnClickListener onClickListener) {
		this(iconResId, text, null, onClickListener, null, null);
	}

	public SummaryButton(int iconResId, String text, String contDesc, OnClickListener onClickListener) {
		this(iconResId, text, contDesc, onClickListener, null, null);
	}

	public SummaryButton(int iconResId, String text, OnClickListener onClickListener, View popupContentView,
		OnClickListener popupOnClickListener) {
		this(iconResId, text, null, onClickListener, popupContentView, popupOnClickListener);
	}

	public SummaryButton(int iconResId, String text, String contDesc, OnClickListener onClickListener,
		View popupContentView,
		OnClickListener popupOnClickListener) {

		mIconResId = iconResId;
		mText = text;
		if (contDesc == null) {
			mContentDescription = mText;
		}
		else {
			mContentDescription = contDesc;
		}
		mOnClickListener = onClickListener;
		mPopupContentView = popupContentView;
		mShouldShowPopup = (mPopupContentView != null);
		mPopupOnClickListener = popupOnClickListener;
	}

	public int getIconResId() {
		return mIconResId;
	}

	public String getText() {
		return mText;
	}

	public String getContentDescription() {
		return mContentDescription;
	}

	public OnClickListener getOnClickListener() {
		return mOnClickListener;
	}

	public boolean getShouldShowPopup() {
		return mShouldShowPopup;
	}

	public View getPopupContentView() {
		return mPopupContentView;
	}

	public OnClickListener getPopupOnClickListener() {
		return mPopupOnClickListener;
	}
}
