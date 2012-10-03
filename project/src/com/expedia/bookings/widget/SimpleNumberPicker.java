package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

import com.mobiata.android.Log;

public class SimpleNumberPicker extends LinearLayout {
	private Context mContext;
	private ImageButton mIncrementButton;
	private TextView mTextLower;
	private TextView mTextCurrent;
	private TextView mTextHigher;
	private ImageButton mDecrementButton;

	// Internal state
	private int mValue;
	private int mMaxValue;
	private int mMinValue;

	// Change listener
	private OnValueChangeListener mOnValueChangeListener;
	public interface OnValueChangeListener {
		void onValueChange(SimpleNumberPicker picker, int oldVal, int newVal);
	}

	private Formatter mFormatter;
	public interface Formatter {
		public String format(int value);
	}

	// Constructors
	public SimpleNumberPicker(Context context) {
		this(context, null);
	}

	public SimpleNumberPicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleNumberPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mContext = context;

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.simple_number_picker, this, true);

		setOrientation(LinearLayout.VERTICAL);

		// increment button
		mIncrementButton = Ui.findView(this, R.id.increment);
		mIncrementButton.setOnClickListener(mOnClickListener);

		mTextLower = Ui.findView(this, R.id.text_lower);
		mTextCurrent = Ui.findView(this, R.id.text_current);
		mTextHigher = Ui.findView(this, R.id.text_higher);

		// decrement button
		mDecrementButton = Ui.findView(this, R.id.decrement);
		mDecrementButton.setOnClickListener(mOnClickListener);

		// Fade the top and bottom TextViews for effect
		mTextLower.setTextColor(mTextLower.getTextColors().withAlpha(64));
		mTextHigher.setTextColor(mTextHigher.getTextColors().withAlpha(64));
	}

	public int getValue() {
		return mValue;
	}

	public void setValue(int value) {
		mValue = value;
	}

	public int getMinValue() {
		return mMinValue;
	}

	public void setMinValue(int minValue) {
		if (mMinValue == minValue) {
			return;
		}
		if (minValue < 0) {
			throw new IllegalArgumentException("minValue must be >= 0");
		}
		mMinValue = minValue;
		if (mMinValue > mValue) {
			mValue = mMinValue;
		}
		update();
	}

	public int getMaxValue() {
		return mMaxValue;
	}

	public void setMaxValue(int maxValue) {
		if (mMaxValue == maxValue) {
			return;
		}
		if (maxValue < 0) {
			throw new IllegalArgumentException("maxValue must be >= 0");
		}
		mMaxValue = maxValue;
		if (mMaxValue < mValue) {
			mValue = mMaxValue;
		}
		update();
	}

	public void setFormatter(Formatter formatter) {
		mFormatter = formatter;
	}

	public void setOnValueChangeListener(OnValueChangeListener listener) {
		mOnValueChangeListener = listener;
	}

	private void notifyChange(int previous, int current) {
		if (mOnValueChangeListener != null) {
			mOnValueChangeListener.onValueChange(this, previous, mValue);
		}
	}

	private void changeCurrent(int current) {
		if (mValue == current) {
			return;
		}
		int previous = mValue;
		setValue(current);
		update();
		notifyChange(previous, current);
	}

	private void changeCurrentByOne(boolean increment) {
		if (increment) {
			changeCurrent(mValue + 1);
		}
		else {
			changeCurrent(mValue - 1);
		}
	}

	private void update() {
		String text;
		boolean enabled;

		if (mValue - 1 >= mMinValue) {
			if (mFormatter != null) {
				text = mFormatter.format(mValue - 1);
			}
			else {
				text = String.valueOf(mValue - 1);
			}
			enabled = true;
		}
		else {
			text = "";
			enabled = false;
		}
		mTextLower.setText(text);
		mTextLower.setEnabled(enabled);
		mDecrementButton.setEnabled(enabled);

		if (mFormatter != null) {
			mTextCurrent.setText(mFormatter.format(mValue));
		}
		else {
			mTextCurrent.setText(String.valueOf(mValue));
		}

		if (mValue + 1 <= mMaxValue) {
			if (mFormatter != null) {
				text = mFormatter.format(mValue + 1);
			}
			else {
				text = String.valueOf(mValue + 1);
			}
			enabled = true;
		}
		else {
			text = "";
			enabled = false;
		}
		mTextHigher.setText(text);
		mTextHigher.setEnabled(enabled);
		mIncrementButton.setEnabled(enabled);

		// disable appropriate button if at end of range
		invalidate();
	}

	private final OnClickListener mOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v.getId() == R.id.increment) {
				changeCurrentByOne(true);
			}
			else {
				changeCurrentByOne(false);
			}
		}
	};
}

