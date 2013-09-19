package com.expedia.bookings.widget;

import org.joda.time.YearMonth;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.graphics.CaretDrawable;
import com.expedia.bookings.graphics.CaretDrawable.Direction;
import com.mobiata.android.util.Ui;

/**
 * All configuration happens in the style/attributes.  If you want to setup the code
 * to work with dynamic configuration be my guest.
 * 
 * TODO: Scale all views based on size of CalendarPicker itself
 */
public class CalendarPicker extends LinearLayout {

	private static final String INSTANCE_SUPER_STATE = "INSTANCE_SUPER_STATE";
	private static final String INSTANCE_DISPLAY_YEAR_MONTH = "INSTANCE_DISPLAY_YEAR_MONTH";
	private static final String INSTANCE_BASE_COLOR = "INSTANCE_BASE_COLOR";
	private static final String INSTANCE_HIGHLIGHT_COLOR = "INSTANCE_HIGHLIGHT_COLOR";

	// State
	private YearMonth mDisplayYearMonth;

	// Style
	private int mBaseColor;
	private int mHighlightColor;

	// Subviews
	private TextView mPreviousMonthTextView;
	private TextView mCurrentMonthTextView;
	private TextView mNextMonthTextView;

	public CalendarPicker(Context context) {
		this(context, null);
	}

	public CalendarPicker(Context context, AttributeSet attrs) {
		this(context, attrs, R.style.V2_Widget_CalendarPicker);
	}

	public CalendarPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Load attributes
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CalendarPicker, 0, defStyle);
		int n = ta.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = ta.getIndex(i);

			switch (attr) {
			case R.styleable.CalendarPicker_calendarBaseColor:
				mBaseColor = ta.getColor(attr, mBaseColor);
				break;

			case R.styleable.CalendarPicker_calendarHighlightColor:
				mHighlightColor = ta.getColor(attr, mHighlightColor);
				break;
			}
		}
		ta.recycle();

		// Configure some layout params (so you don't have to do it in XML)
		setOrientation(LinearLayout.VERTICAL);

		// Inflate the widget
		inflate(context, R.layout.widget_calendar_picker, this);

		// Default to showing current year/month
		mDisplayYearMonth = YearMonth.now();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mPreviousMonthTextView = Ui.findView(this, R.id.previous_month);
		mCurrentMonthTextView = Ui.findView(this, R.id.current_month);
		mNextMonthTextView = Ui.findView(this, R.id.next_month);

		mPreviousMonthTextView.setOnClickListener(mOnClickListener);
		mNextMonthTextView.setOnClickListener(mOnClickListener);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_SUPER_STATE));
		mDisplayYearMonth = YearMonth.parse(bundle.getString(INSTANCE_DISPLAY_YEAR_MONTH));
		mBaseColor = bundle.getInt(INSTANCE_BASE_COLOR);
		mHighlightColor = bundle.getInt(INSTANCE_HIGHLIGHT_COLOR);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// Wait until here to start manipulating sub-Views; that way we can
		// restore the instance state properly first.
		updateColors();
		updateHeader();

		// Measure some Views so we can properly setup Drawables next to them
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);

				// Carets' width is calculated based on the height
				int caretHeight = (int) mPreviousMonthTextView.getTextSize();
				int caretWidth = (int) Math.floor(caretHeight / 1.5);
				int caretPadding = caretHeight / 3;
				int totalHeight = mPreviousMonthTextView.getHeight();
				int topBotPadding = (totalHeight - caretHeight) / 2;

				CaretDrawable drawableLeft = new CaretDrawable(Direction.LEFT, mHighlightColor);
				drawableLeft.setBounds(0, 0, caretWidth, totalHeight - topBotPadding);
				mPreviousMonthTextView.setCompoundDrawables(drawableLeft, null, null, null);
				mPreviousMonthTextView.setCompoundDrawablePadding(caretPadding);

				CaretDrawable drawableRight = new CaretDrawable(Direction.RIGHT, mHighlightColor);
				drawableRight.setBounds(0, 0, caretWidth, totalHeight - topBotPadding);
				mNextMonthTextView.setCompoundDrawables(null, null, drawableRight, null);
				mNextMonthTextView.setCompoundDrawablePadding(caretPadding);

				return true;
			}
		});
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE_SUPER_STATE, super.onSaveInstanceState());
		bundle.putString(INSTANCE_DISPLAY_YEAR_MONTH, mDisplayYearMonth.toString());
		bundle.putInt(INSTANCE_BASE_COLOR, mBaseColor);
		bundle.putInt(INSTANCE_HIGHLIGHT_COLOR, mHighlightColor);
		return bundle;
	}

	//////////////////////////////////////////////////////////////////////////
	// Display

	private void updateColors() {
		if (mPreviousMonthTextView != null) {
			mPreviousMonthTextView.setTextColor(mHighlightColor);
		}

		if (mCurrentMonthTextView != null) {
			mCurrentMonthTextView.setTextColor(mBaseColor);
		}

		if (mNextMonthTextView != null) {
			mNextMonthTextView.setTextColor(mHighlightColor);
		}
	}

	private void updateHeader() {
		mPreviousMonthTextView.setText(mDisplayYearMonth.minusMonths(1).monthOfYear().getAsText());
		mCurrentMonthTextView.setText(mDisplayYearMonth.monthOfYear().getAsText());
		mNextMonthTextView.setText(mDisplayYearMonth.plusMonths(1).monthOfYear().getAsText());
	}

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == mPreviousMonthTextView) {
				mDisplayYearMonth = mDisplayYearMonth.minusMonths(1);
				updateHeader();
			}
			else if (v == mNextMonthTextView) {
				mDisplayYearMonth = mDisplayYearMonth.plusMonths(1);
				updateHeader();
			}
		}
	};
}
