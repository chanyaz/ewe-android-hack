package com.expedia.bookings.data;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

/**
 * 
 * This class represents an navigation item via an image, text, and an onclick action
 *
 */
public class NavItem {
	private Drawable mDrawable;
	private String mText;
	private OnClickListener mClickListener;

	public NavItem(Drawable drawable, String text, OnClickListener clickListener) {
		mDrawable = drawable;
		mText = text;
		mClickListener = clickListener;
	}

	public String getText() {
		return mText;
	}

	public Drawable getDrawable() {
		return mDrawable;
	}

	public OnClickListener getOnClickListener() {
		return mClickListener;
	}
}
