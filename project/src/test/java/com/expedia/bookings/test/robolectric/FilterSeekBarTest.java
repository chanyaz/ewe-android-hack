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
import com.expedia.bookings.widget.FilterSeekBar;

@RunWith(RobolectricRunner.class)
public class FilterSeekBarTest {

	private static final Context context = RuntimeEnvironment.application;

	@Test
	public void seekBarValueUpdate() {
		FilterSeekBar seekBar = new FilterSeekBar(context, null);
		Assert.assertEquals(1, seekBar.getMaxValue());

		seekBar = new FilterSeekBar(context, null);
		seekBar.setUpperLimit(10);
		seekBar.setMaxValue(5);
		Assert.assertEquals(5, seekBar.getMaxValue());

		seekBar = new FilterSeekBar(context, null);
		seekBar.setUpperLimit(10);
		seekBar.setMaxValue(0);
		Assert.assertEquals(1, seekBar.getMaxValue());
	}

	@Test
	public void seekBarHandleDrag() {
		Drawable thumb = ContextCompat.getDrawable(RuntimeEnvironment.application, R.drawable.rsb__seek_thumb_pressed);
		int defaultWidth = thumb.getIntrinsicWidth();
		int defaultHeight = thumb.getIntrinsicHeight();

		Bitmap emptyBitmap = Bitmap.createBitmap(defaultHeight, 1000, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(emptyBitmap);

		Activity activity = Robolectric.buildActivity(Activity.class).create().start().get();
		FilterSeekBar bar = new FilterSeekBar(activity, null);
		bar.setUpperLimit(2);

		for (int testWidth = defaultWidth * 3; testWidth <= 1000; testWidth++) {
			int halfWidth = (int) (testWidth / 2.0);

			bar.setUpperLimit(2);

			bar.measure(View.MeasureSpec.makeMeasureSpec(testWidth, View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(defaultHeight, View.MeasureSpec.EXACTLY));
			bar.layout(0, 0, testWidth, defaultHeight);

			Assert.assertEquals(testWidth, bar.getWidth());
			Assert.assertEquals(defaultHeight, bar.getHeight());

			// Try to drag max to middle
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, testWidth, 0, 0));
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, halfWidth, 0, 0));
			Assert.assertEquals(1, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, halfWidth, 0, 0));
			Assert.assertEquals(1, bar.getMaxValue());

			// Try to drag max on top of min
			Assert.assertEquals(1, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, testWidth, 0, 0));
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, halfWidth, 0, 0));
			Assert.assertEquals(1, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, halfWidth, 0, 0));
			Assert.assertEquals(1, bar.getMaxValue());

			// Try to drag max past min
			Assert.assertEquals(1, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, testWidth, 0, 0));
			Assert.assertEquals(2, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, 0, 0));
			Assert.assertEquals(1, bar.getMaxValue());
			bar.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
			Assert.assertEquals(1, bar.getMaxValue());

			bar.onDraw(canvas);
		}
	}
}
