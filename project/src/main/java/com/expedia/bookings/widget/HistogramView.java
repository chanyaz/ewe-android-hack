package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightHistogram;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.WeeklyFlightHistogram;

/**
 * This View draws a horizontal bar graph (or histogram). It's somewhat specialized for
 * the GDE fragment in Tablet 2014.
 * <p/>
 * Created by dmelton on 7/15/14.
 */
public class HistogramView extends View {

	private Paint mPaint;
	private float mGapPx = 1;

	private float[] mData;

	public HistogramView(Context context) {
		super(context);
		init();
	}

	public HistogramView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HistogramView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		Paint paint = new Paint();
		paint.setColor(getResources().getColor(R.color.tablet_results_search_blue));
		setPaint(paint);
		setData(new float[0]);
	}

	/**
	 * Sets the paint used for each bar in the histogram.
	 *
	 * @param paint
	 */
	public void setPaint(Paint paint) {
		mPaint = paint;
		invalidate();
	}

	/**
	 * Sets the data for the bars displayed on this histogram.
	 * Values are expected to be between [0f, 1f].
	 *
	 * @param data
	 */
	public void setData(float[] data) {
		mData = data;
		invalidate();
	}

	/**
	 * Special setter specifically designed for WeeklyFlightHistogram objects.
	 *
	 * @param min     Global price minimum (across all dates)
	 * @param max     Global price maximum (across all dates)
	 * @param flights
	 */
	public void setData(Money min, Money max, WeeklyFlightHistogram flights) {
		float fMin = min == null ? 0f : min.getAmount().floatValue();
		float fMax = max == null ? 0f : max.getAmount().floatValue();

		float[] data = new float[7];
		for (int i = 0; i < 7; i++) {
			FlightHistogram gram = flights.get(i);
			if (gram == null) {
				data[i] = 0f;
			}
			else {
				float fGram = gram.getMinPrice().getAmount().floatValue();
				data[i] = fMax == fMin ? 1f : (fGram - fMin) / (fMax - fMin);

				// Return trips are sometimes outside the range (shrug)
				data[i] = Math.max(0f, Math.min(1f, data[i]));

				// Adjust the range from 20% - 100% to look better
				data[i] = 0.8f * data[i] + 0.2f;
			}
		}
		setData(data);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mData == null || mData.length == 0) {
			return;
		}
		int bars = mData.length;
		float paddingTopPx = getPaddingTop();
		float paddingBottomPx = getPaddingBottom();
		float paddingLeftPx = getPaddingLeft();
		float paddingRightPx = getPaddingRight();
		float totalHeightPx = canvas.getHeight() - paddingTopPx - paddingBottomPx;
		float totalWidthPx = canvas.getWidth() - paddingLeftPx - paddingRightPx;
		float barHeightPx = (totalHeightPx - mGapPx * (bars - 1)) / bars;

		for (int i = 0; i < bars; i++) {
			if (mData[i] > 0) {
				float width = mData[i] * totalWidthPx;
				float y1 = paddingTopPx + i * (barHeightPx + mGapPx);
				float y2 = y1 + barHeightPx;
				float x1 = paddingLeftPx;
				float x2 = x1 + width;
				canvas.drawRect(x1, y1, x2, y2, mPaint);
			}
		}

	}
}
