package com.expedia.bookings.widget;

import java.util.Locale;

import org.joda.time.LocalDate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;

import com.expedia.bookings.data.WeeklyFlightHistogram;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.TypefaceSpan;

/**
 * A custom view for displaying a date range. Created for the GDE histogram. But
 * could be re-used. Output looks basically like:
 * <p/>
 *  M A R
 * 15 - 21
 * <p/>
 * Created by dmelton on 7/15/14.
 */
public class StackedDateRangeTextView extends TextView {
	public StackedDateRangeTextView(Context context) {
		super(context);
	}

	public StackedDateRangeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StackedDateRangeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setDates(WeeklyFlightHistogram week) {
		setDates(week.getWeekStart(), week.getWeekEnd());
	}

	// We want to use special spans for this. The month is all caps
	// and has font tracking = 200. The date range is condensed bold.
	public void setDates(LocalDate from, LocalDate to) {

		String month = from.toString("MMM").toUpperCase(Locale.getDefault());
		String fromDay = from.toString("d");
		String toDay = to.toString("d");

		SpannableStringBuilder builder = new SpannableStringBuilder(month);
		builder.append("\n");
		int datePos = builder.length();
		builder.append(fromDay);
		builder.append("-");
		builder.append(toDay);

		// Apply kerning/tracking to the month
		TrackingSpan monthSpan = new TrackingSpan(getTextSize() / 4f);
		builder.setSpan(monthSpan, 0, month.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Apply a Typeface to the dates
		TypefaceSpan datesSpan = FontCache.getSpan(FontCache.Font.ROBOTO_CONDENSED_BOLD);
		builder.setSpan(datesSpan, datePos, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		setText(builder);
	}

	private static class TrackingSpan extends ReplacementSpan {
		private float mTrackingPx;

		public TrackingSpan(float tracking) {
			mTrackingPx = tracking;
		}

		@Override
		public int getSize(Paint paint, CharSequence text,
						   int start, int end, Paint.FontMetricsInt fm) {
			return (int) (paint.measureText(text, start, end) + mTrackingPx * (end - start - 1));
		}

		@Override
		public void draw(Canvas canvas, CharSequence text, int start, int end,
						 float x, int top, int y, int bottom, Paint paint) {
			float dx = x;
			for (int i = start; i < end; i++) {
				canvas.drawText(text, i, i + 1, dx, y, paint);
				dx += paint.measureText(text, i, i + 1) + mTrackingPx;
			}
		}
	}
}
