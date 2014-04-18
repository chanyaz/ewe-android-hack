package com.expedia.bookings.graphics;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.expedia.bookings.bitmaps.ColorAvgUtils;
import com.expedia.bookings.bitmaps.ColorScheme;
import com.expedia.bookings.bitmaps.DominantColorCalculator;
import com.expedia.bookings.enums.TripBucketItemState;
import com.mobiata.android.util.TimingLogger;

public class HeaderBitmapColorAveragedDrawable extends HeaderBitmapDrawable {

	private boolean mOverlayEnabled = false;

	public HeaderBitmapColorAveragedDrawable() {
		super();
	}

	public void enableOverlay() {
		mOverlayEnabled = true;
	}

	public void disableOverlay() {
		mOverlayEnabled = false;
	}

	@Override
	public void setBitmap(final Bitmap bitmap) {
		super.setBitmap(bitmap);
		if (mOverlayEnabled && bitmap != null) {
			new DominantColorTask().execute(bitmap);
		}
	}

	private class DominantColorTask extends AsyncTask<Bitmap, Void, ColorScheme> {
		@Override
		protected ColorScheme doInBackground(Bitmap... bitmaps) {
			TimingLogger startupTimer = new TimingLogger("ExpediaBookings", "BitmapColorAveraging");
			DominantColorCalculator dominantColorCalculator = new DominantColorCalculator(bitmaps[0]);
			startupTimer.addSplit("Bitmap average color scheme obtained.");
			startupTimer.dumpToLog();

			return dominantColorCalculator.getColorScheme();
		}

		@Override
		protected void onPostExecute(ColorScheme colorScheme) {
			int colorDarkened = ColorAvgUtils.darken(colorScheme.primaryAccent, 0.4f);
			int overLayWithAlpha = 0xCC000000 | 0x00ffffff & colorDarkened;
			setOverlayDrawable(new ColorDrawable(overLayWithAlpha));
		}
	};
}