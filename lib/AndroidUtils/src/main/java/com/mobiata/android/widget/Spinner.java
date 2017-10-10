package com.mobiata.android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobiata.android.R;

public class Spinner extends android.widget.Spinner {
	private ColorStateList mTextColor;

	public Spinner(Context context) {
		this(context, null, android.R.style.Widget_Spinner);
	}

	public Spinner(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.style.Widget_Spinner);
	}

	public Spinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Spinner);
		mTextColor = a.getColorStateList(R.styleable.Spinner_android_textColor);
		a.recycle();
	}

	@Override
	protected boolean addViewInLayout(View child, int index, LayoutParams params) {
		if (mTextColor != null) {
			setTextColor(child);
		}

		return super.addViewInLayout(child, index, params);
	}

	private void setTextColor(View view) {
		if (view instanceof TextView) {
			((TextView) view).setTextColor(mTextColor);
		}

		if (view instanceof ViewGroup) {
			final int size = ((ViewGroup) view).getChildCount();
			for (int i = 0; i < size; i++) {
				setTextColor(((ViewGroup) view).getChildAt(i));
			}
		}
	}
}
