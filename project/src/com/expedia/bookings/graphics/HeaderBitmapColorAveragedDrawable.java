package com.expedia.bookings.graphics;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.expedia.bookings.bitmaps.ColorScheme;
import com.expedia.bookings.bitmaps.DominantColorCalculator;
import com.mobiata.android.util.TimingLogger;

public class HeaderBitmapColorAveragedDrawable extends HeaderBitmapDrawable {

	public enum HeaderBitmapColorAveragedState {
		DEFAULT,
		PLACEHOLDER,
		REFRESH
	}

	private HeaderBitmapColorAveragedState mState;

	private BitmapColorAverageDoneListener mBitmapColorAverageListener;

	public HeaderBitmapColorAveragedDrawable() {
		super();
		mState = HeaderBitmapColorAveragedState.DEFAULT;
	}

	public void setState(HeaderBitmapColorAveragedState state) {
		mState = state;
	}

	public interface BitmapColorAverageDoneListener {
		public void onDominantColorCalculated(ColorScheme colorScheme);
	}

	public void setOnBitmapColorAverageListener(BitmapColorAverageDoneListener listener) {
		this.mBitmapColorAverageListener = listener;
	}

	@Override
	public void setBitmap(final Bitmap bitmap) {
		super.setBitmap(bitmap);
		Handler mainHandler = new Handler(Looper.getMainLooper());
		Runnable myRunnable = new Runnable() {
			@Override
			public void run() {
				if (bitmap != null) {
					TimingLogger startupTimer = new TimingLogger("ExpediaBookings", "BitmapColorAveraging");
					DominantColorCalculator dominantColorCalculator = new DominantColorCalculator(bitmap);
					ColorScheme colorScheme = dominantColorCalculator.getColorScheme();
					startupTimer.addSplit("Bitmap average color scheme obtained.");
					startupTimer.dumpToLog();
					if (mBitmapColorAverageListener != null) {
						mBitmapColorAverageListener.onDominantColorCalculated(colorScheme);
					}
				}
			}
		};
		// Let's average color only on refreshed state.
		if (mState == HeaderBitmapColorAveragedState.REFRESH) {
			mainHandler.post(myRunnable);
		}
	}
}