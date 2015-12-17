package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class ExpirationPicker extends LinearLayout {

	private static final String TAG_MAX_MONTH = "TAG_MAX_MONTH";
	private static final String TAG_MIN_MONTH = "TAG_MIN_MONTH";
	private static final String TAG_MAX_YEAR = "TAG_MAX_YEAR";
	private static final String TAG_MIN_YEAR = "TAG_MIN_YEAR";
	private static final String TAG_CURRENT_MONTH = "TAG_CURRENT_MONTH";
	private static final String TAG_CURRENT_YEAR = "TAG_CURRENT_YEAR";

	private static final String sMonthFormatString = "%02d";
	private static final String sYearFormatString = "%04d";

	private int mMinMonth = 1;
	private int mMaxMonth = 12;
	private int mMinYear = 2012;
	private int mMaxYear = mMinYear + 25;

	private int mCurrentMonth = 1;
	private int mCurrentYear = 2012;

	private IExpirationListener mListener;

	View mMonthUp;
	View mMonthDown;
	View mYearUp;
	View mYearDown;
	TextView mMonthTv;
	TextView mYearTv;

	public ExpirationPicker(Context context) {
		super(context);
		init(context);
	}

	public ExpirationPicker(Context context, AttributeSet attr) {
		super(context, attr);
		init(context);
	}

	public ExpirationPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);

	}

	private void init(Context context) {
		View view = Ui.inflate(context, R.layout.widget_expiration_picker, this);
		mMonthUp = Ui.findView(view, R.id.month_up);
		mMonthDown = Ui.findView(view, R.id.month_down);
		mYearUp = Ui.findView(view, R.id.year_up);
		mYearDown = Ui.findView(view, R.id.year_down);
		mMonthTv = Ui.findView(view, R.id.month_text);
		mYearTv = Ui.findView(view, R.id.year_text);

		mMonthUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				monthUp();
			}
		});
		mMonthDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				monthDown();
			}
		});
		mYearUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				yearUp();
			}
		});
		mYearDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				yearDown();
			}
		});

		updateText();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		super.onSaveInstanceState();
		Bundle bundle = new Bundle();
		bundle.putInt(TAG_CURRENT_MONTH, mCurrentMonth);
		bundle.putInt(TAG_CURRENT_YEAR, mCurrentYear);
		bundle.putInt(TAG_MAX_MONTH, mMaxMonth);
		bundle.putInt(TAG_MIN_MONTH, mMinMonth);
		bundle.putInt(TAG_MAX_YEAR, mMaxYear);
		bundle.putInt(TAG_MIN_YEAR, mMinYear);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {

		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			mCurrentMonth = bundle.getInt(TAG_CURRENT_MONTH);
			mCurrentYear = bundle.getInt(TAG_CURRENT_YEAR);
			mMaxMonth = bundle.getInt(TAG_MAX_MONTH, 12);
			mMinMonth = bundle.getInt(TAG_MIN_MONTH, 1);
			mMinYear = bundle.getInt(TAG_MIN_YEAR, 2012);
			mMaxYear = bundle.getInt(TAG_MAX_YEAR, mMinYear + 25);
			return;
		}

		super.onRestoreInstanceState(state);
	}

	public void setListener(IExpirationListener listener) {
		mListener = listener;
	}

	public void setMonth(int month) {
		if (month >= mMinMonth && month <= mMaxMonth) {
			mCurrentMonth = month;
		}
		else {
			throw new RuntimeException("Can't set the month outside of the min and max range");
		}
		updateMonth();
	}

	public void setMaxMonth(int maxMonth) {
		if (maxMonth < mCurrentMonth) {
			mCurrentMonth = maxMonth;
			updateMonth();
		}
		mMaxMonth = maxMonth;
	}

	public void setMinMonth(int minMonth) {
		if (minMonth > mCurrentMonth) {
			mCurrentMonth = minMonth;
			updateMonth();
		}
		mMinMonth = minMonth;
	}

	public void setMaxYear(int maxYear) {
		if (maxYear < mCurrentYear) {
			mCurrentYear = maxYear;
			updateYear();
		}
		mMaxYear = maxYear;
	}

	public void setMinYear(int minYear) {
		if (minYear > mCurrentYear) {
			mCurrentYear = minYear;
			updateYear();
		}
		mMinYear = minYear;
	}

	public void setYear(int year) {
		if (year >= mMinYear && year <= mMaxYear) {
			mCurrentYear = year;
		}
		else {
			throw new RuntimeException("Can't set the year outside of the min and max range");
		}
		updateYear();
	}

	public void monthUp() {
		if (mCurrentMonth == mMaxMonth) {
			mCurrentMonth = mMinMonth;
		}
		else {
			mCurrentMonth++;
		}
		updateMonth();
	}

	public void monthDown() {
		if (mCurrentMonth == mMinMonth) {
			mCurrentMonth = mMaxMonth;
		}
		else {
			mCurrentMonth--;
		}
		updateMonth();
	}

	public void yearUp() {
		if (mCurrentYear == mMaxYear) {
			mCurrentYear = mMinYear;
		}
		else {
			mCurrentYear++;
		}
		updateYear();
	}

	public void yearDown() {
		if (mCurrentYear == mMinYear) {
			mCurrentYear = mMaxYear;
		}
		else {
			mCurrentYear--;
		}
		updateYear();
	}

	private void updateMonth() {
		mMonthTv.setText(String.format(sMonthFormatString, mCurrentMonth));
		if (mListener != null) {
			mListener.onMonthChange(mCurrentMonth);
		}
	}

	private void updateYear() {
		mYearTv.setText(String.format(sYearFormatString, mCurrentYear));
		if (mListener != null) {
			mListener.onYearChange(mCurrentYear);
		}
	}

	public void updateText() {
		updateMonth();
		updateYear();
	}

	public int getMonth() {
		return this.mCurrentMonth;
	}

	public int getYear() {
		return this.mCurrentYear;
	}

	public interface IExpirationListener {
		void onMonthChange(int month);

		void onYearChange(int year);
	}
}
