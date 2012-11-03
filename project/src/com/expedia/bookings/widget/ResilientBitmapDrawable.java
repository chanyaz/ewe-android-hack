package com.expedia.bookings.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import com.mobiata.android.Log;

/**
 * This is a version of BitmapDrawable that doesn't crash when you accidentally
 * try to render a recycled Bitmap.
 * 
 * Ideally, we'd be able to avoid a BitmapDrawable from ever rendering after
 * we've recycled its Bitmap.  However, in reality this just seems to be far
 * more difficult to achieve than I thought.
 * 
 * Ultimately we should come up with a better solution (e.g., an ImageView that
 * automatically loads a URL on demand), but for now we'll just go with this
 * to provide a bit more stability.
 * 
 * Also allows one to spit out debug messages when trying to draw recycled
 * bitmaps.
 */
public class ResilientBitmapDrawable extends BitmapDrawable {

	private boolean mWarned = true;

	private String mMessage;

	public ResilientBitmapDrawable(Resources resources, Bitmap image) {
		super(resources, image);
	}

	public ResilientBitmapDrawable(Resources resources, Bitmap image, String message) {
		super(resources, image);

		mMessage = message;
		mWarned = false;
	}

	// Allows you to spit out debug info
	public void setWarning(String message) {
		mMessage = message;
		mWarned = false;
	}

	@Override
	public void draw(Canvas canvas) {
		if (!getBitmap().isRecycled()) {
			super.draw(canvas);
		}
		else if (!mWarned) {
			Log.w("Tried to draw recycled bitmap.  Info: " + mMessage);
			mWarned = true;
		}
	}
}
