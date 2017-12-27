package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.FilterRangeSeekBar;

@RunWith(RobolectricRunner.class)
public class FilterRangeSeekBarTest {

	private static Context context = RuntimeEnvironment.application;

	@Test
	public void rangeSeekBarValueUpdate() {
		FilterRangeSeekBar seekBar = new FilterRangeSeekBar(context, null);
		Assert.assertEquals(0, seekBar.getMinValue());
		Assert.assertEquals(1, seekBar.getMaxValue());

		seekBar = new FilterRangeSeekBar(context, null);
		seekBar.setUpperLimit(10);
		seekBar.setMaxValue(5);
		Assert.assertEquals(0, seekBar.getMinValue());
		Assert.assertEquals(5, seekBar.getMaxValue());

		seekBar = new FilterRangeSeekBar(context, null);
		seekBar.setUpperLimit(10);
		seekBar.setMinValue(5);
		seekBar.setMaxValue(5);
		Assert.assertEquals(5, seekBar.getMinValue());
		Assert.assertEquals(6, seekBar.getMaxValue());
	}

	@Test
	public void rangeSeekBarHandleDrag() {
		Drawable thumb = ContextCompat.getDrawable(RuntimeEnvironment.application, R.drawable.rsb__seek_thumb_pressed);
		int defaultWidth = thumb.getIntrinsicWidth();
		int defaultHeight = thumb.getIntrinsicHeight();

		Bitmap emptyBitmap = Bitmap.createBitmap(defaultHeight, 1000, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(emptyBitmap);

		Activity activity = Robolectric.buildActivity(Activity.class).create().start().get();
		FilterRangeSeekBar bar = new FilterRangeSeekBar(activity, null);
		bar.setUpperLimit(2);

		for (int testWidth = defaultWidth * 3; testWidth <= 1000; testWidth++) {
			int halfWidth = (int) (testWidth / 2.0);

			bar.setUpperLimit(2);

			bar.measure(View.MeasureSpec.makeMeasureSpec(testWidth, View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(defaultHeight, View.MeasureSpec.EXACTLY));
			bar.layout(0, 0, testWidth, defaultHeight);

			Assert.assertEquals(testWidth, bar.getWidth());
			Assert.assertEquals(defaultHeight, bar.getHeight());

			// Just touch the min thumb
			Assert.assertEquals(0, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
			Assert.assertEquals(0, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 0, 0));
			Assert.assertEquals(0, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
			Assert.assertEquals(0, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());

			// Try to drag min to middle
			Assert.assertEquals(0, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, 0, 0));
			Assert.assertEquals(0, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, halfWidth, 0, 0));
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, halfWidth, 0, 0));
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());

			// Try to drag max on top of min
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, testWidth, 0, 0));
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, halfWidth, 0, 0));
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, halfWidth, 0, 0));
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());

			// Try to drag max past min
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, testWidth, 0, 0));
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 0, 0));
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
			Assert.assertEquals(1, bar.getMinValue());
			Assert.assertEquals(2, bar.getMaxValue());

			bar.onDraw(canvas);
		}
	}

	@Test
	public void accessibilityTextWithNullName() {
		FilterRangeSeekBar seekBar = new FilterRangeSeekBar(context, null);
		seekBar.currentA11yStartValue = "startValue";
		seekBar.currentA11yEndValue = "endValue";
		FilterRangeSeekBar.Thumb thumb = FilterRangeSeekBar.Thumb.MIN;
		String accessibilityText = seekBar.getAccessibilityText(thumb);

		Assert.assertEquals(" Range Slider startValue, use back-and-forth gesture or local context menu to adjust", accessibilityText);

		thumb = FilterRangeSeekBar.Thumb.MAX;
		accessibilityText = seekBar.getAccessibilityText(thumb);

		Assert.assertEquals(" Range Slider endValue, use back-and-forth gesture or local context menu to adjust", accessibilityText);
	}

	@Test
	public void accessibilityTextWithNullValue() {
		FilterRangeSeekBar seekBar = new FilterRangeSeekBar(context, null);
		seekBar.a11yStartName = "startName";
		seekBar.a11yEndName = "endName";
		FilterRangeSeekBar.Thumb thumb = FilterRangeSeekBar.Thumb.MIN;
		String accessibilityText = seekBar.getAccessibilityText(thumb);

		Assert.assertEquals("startName Range Slider , use back-and-forth gesture or local context menu to adjust", accessibilityText);

		thumb = FilterRangeSeekBar.Thumb.MAX;
		accessibilityText = seekBar.getAccessibilityText(thumb);

		Assert.assertEquals("endName Range Slider , use back-and-forth gesture or local context menu to adjust", accessibilityText);
	}

	@Test
	public void accessibilityTextWithNullNameAndValue() {
		FilterRangeSeekBar seekBar = new FilterRangeSeekBar(context, null);
		FilterRangeSeekBar.Thumb thumb = FilterRangeSeekBar.Thumb.MIN;
		String accessibilityText = seekBar.getAccessibilityText(thumb);

		Assert.assertEquals(" Range Slider , use back-and-forth gesture or local context menu to adjust", accessibilityText);

		thumb = FilterRangeSeekBar.Thumb.MAX;
		accessibilityText = seekBar.getAccessibilityText(thumb);

		Assert.assertEquals(" Range Slider , use back-and-forth gesture or local context menu to adjust", accessibilityText);
	}
}
