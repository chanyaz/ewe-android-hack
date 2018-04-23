package com.mobiata.android.time.widget;

import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.LocalDate.Property;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.mobiata.android.time.R;
import com.mobiata.android.time.util.JodaUtils;

/**
 * Shows the days of the week in a row: MON TUE WED etc.
 *
 * This View is part of CalendarPicker and should not be used outside
 * of it.  It does not save its own state; it depends on CalendarPicker
 * for that.
 *
 * It splits itself into seven equal parts, then sizes the text based
 * on the largest size where all text fits (with reasonable padding
 * between each day).
 *
 * This class should be modified in conjunction with MonthView, which
 * should measure itself in a similar manner.
 *
 * This View assumes it has a width of match_parent and a height of
 * wrap_content (for ease of measurement).  It scales everything else.
 */
public class DaysOfWeekView extends View {

	private static final int NUM_DAYS = 7;

	// The minimum percent of the width should be taken up by padding between text
	private static final float PADDING_PERCENT = .10f;

	// The "step" size when increasing/decreasing text size to match
	private static final float TEXT_SIZE_STEP = 1;

	private Property[] mDaysOfWeek;
	private String[] mDaysOfWeekStr;

	private TextPaint mTextPaint;
	private float mMaxTextSize;

	// Cached, used for measuring
	private Rect mTextBounds;

	// Cached for faster draws
	private int mHeight;
	private float mColWidth;
	private float mHalfColWidth;
	private float mDrawY;

	// Interface for custom day-of-week formatting
	public interface DayOfWeekRenderer {
		public static final DayOfWeekRenderer DEFAULT = new DayOfWeekRenderer() {
			@Override
			public String renderDayOfWeek(Property dayOfWeek) {
				return dayOfWeek.getAsShortText().toUpperCase(Locale.getDefault());
			}
		};
		public String renderDayOfWeek(Property dayOfWeek);
	}

	// Accessibility
	private DaysOfWeekTouchHelper mTouchHelper;

	public DaysOfWeekView(Context context) {
		this(context, null);
	}

	public DaysOfWeekView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DaysOfWeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextAlign(Align.CENTER);
		mMaxTextSize = mTextPaint.getTextSize();

		mTextBounds = new Rect();

		// Pre-load the days of the week in the current locale
		mDaysOfWeek = new Property[NUM_DAYS];
		mDaysOfWeekStr = new String[NUM_DAYS];
		LocalDate firstDayOfWeek = LocalDate.now().withDayOfWeek(JodaUtils.getFirstDayOfWeek());
		for (int a = 0; a < NUM_DAYS; a++) {
			mDaysOfWeek[a] = firstDayOfWeek.plusDays(a).dayOfWeek();
		}
		setDayOfWeekRenderer(DayOfWeekRenderer.DEFAULT);

		// Setup accessibility
		mTouchHelper = new DaysOfWeekTouchHelper(this);
		ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
	}

	public void setDayOfWeekRenderer(DayOfWeekRenderer d) {
		for (int a = 0; a < NUM_DAYS; a++) {
			mDaysOfWeekStr[a] = d.renderDayOfWeek(mDaysOfWeek[a]);
		}
		invalidate();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean dispatchHoverEvent(MotionEvent event) {
		// Always attempt to dispatch hover events to accessibility first.
		if (mTouchHelper.dispatchHoverEvent(event)) {
			return true;
		}

		return super.dispatchHoverEvent(event);
	}

	public void setTextColor(int color) {
		mTextPaint.setColor(color);
	}

	public void setMaxTextSize(float textSize) {
		mMaxTextSize = textSize;
	}

	public void setTypeface(Typeface typeface) {
		mTextPaint.setTypeface(typeface);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Do a normal measurement first
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Keep measuring with smaller textSizes until we're small enough to fit
		int measuredWidth = getMeasuredWidth();
		float colWidth = (float) measuredWidth / NUM_DAYS;
		float maxTextWidth = colWidth * (1 - PADDING_PERCENT);

		mTextPaint.setTextSize(mMaxTextSize);
		boolean tooBig;
		do {
			tooBig = false;
			for (String dayOfWeek : mDaysOfWeekStr) {
				mTextBounds.setEmpty();
				mTextPaint.getTextBounds(dayOfWeek, 0, dayOfWeek.length(), mTextBounds);

				if (mTextBounds.width() > maxTextWidth) {
					tooBig = true;
				}
			}

			if (tooBig) {
				mTextPaint.setTextSize(mTextPaint.getTextSize() - TEXT_SIZE_STEP);
			}
		}
		while (tooBig);

		// Use text paint's total height to determine measured height
		int measuredHeight = (int) Math.ceil(mTextPaint.descent() - mTextPaint.ascent());
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mHeight = h;
		mColWidth = (float) w / NUM_DAYS;
		mHalfColWidth = mColWidth / 2.0f;
		mDrawY = h - mTextPaint.descent();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		for (int a = 0; a < NUM_DAYS; a++) {
			float centerOfCol = (mColWidth * a) + mHalfColWidth;
			canvas.drawText(mDaysOfWeekStr[a], centerOfCol, mDrawY, mTextPaint);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Accessibility

	private final class DaysOfWeekTouchHelper extends ExploreByTouchHelper {

		public DaysOfWeekTouchHelper(View forView) {
			super(forView);
		}

		private String getDescriptionForVirtualViewId(int virtualViewId) {
			return getContext().getString(R.string.cd_day_of_week_TEMPLATE, String.valueOf(virtualViewId + 1),
					mDaysOfWeek[virtualViewId].getAsText());
		}

		@Override
		protected int getVirtualViewAt(float x, float y) {
			return (int) Math.floor(x / mColWidth);
		}

		@Override
		protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
			for (int a = 0; a < NUM_DAYS; a++) {
				virtualViewIds.add(a);
			}
		}

		@Override
		protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
			node.setContentDescription(getDescriptionForVirtualViewId(virtualViewId));
			node.setBoundsInParent(new Rect((int) mColWidth * virtualViewId, 0, (int) mColWidth * (virtualViewId + 1),
					mHeight));
		}

		@Override
		protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
			event.setContentDescription(getDescriptionForVirtualViewId(virtualViewId));
		}

		@Override
		protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
			// There are no actions to perform on this View
			return false;
		}

	}

}
