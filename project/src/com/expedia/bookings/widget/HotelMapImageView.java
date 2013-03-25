package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.data.Location;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache.OnImageLoaded;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;

public class HotelMapImageView extends ImageView {

	private static int ZOOM = 13; // We want this to be different depending on dpi too
	private static int DENSITY_SCALE_FACTOR = 1; // This has to be calculated at runtime

	private String mStaticMapUri;
	private Location mLocation;

	public HotelMapImageView(Context context) {
		super(context);
		initMapView();
	}

	public HotelMapImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HotelMapImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMapView();
	}

	private void initMapView() {
		Resources res = getResources();

		// High DPI screens should utilize scale=2 for this API
		// https://developers.google.com/maps/documentation/staticmaps/
		if (res.getDisplayMetrics().density > 1.5) {
			DENSITY_SCALE_FACTOR = 2;
			ZOOM = 12;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w > 0 && h > 0) {
			regenerateUri();
		}
	}

	public void setLocation(Location location) {
		mLocation = location;
		regenerateUri();
	}

	private void regenerateUri() {
		if (mLocation == null) {
			return;
		}

		int width = getWidth();
		int height = getHeight();
		if (width == 0 || height == 0) {
			// It would be a useless image anyways
			return;
		}

		String oldUri = mStaticMapUri;

		mStaticMapUri = GoogleServices.getStaticMapUrl(width / DENSITY_SCALE_FACTOR, height / DENSITY_SCALE_FACTOR,
				ZOOM, MapType.ROADMAP, mLocation.getLatitude(), mLocation.getLongitude(),
				getMarkerString(mLocation, 0x126299)) + "&scale=" + DENSITY_SCALE_FACTOR;

		Log.d("ITIN: mapUrl:" + mStaticMapUri);

		if (!mStaticMapUri.equals(oldUri)) {
			UrlBitmapDrawable drawable = UrlBitmapDrawable.loadImageView(mStaticMapUri, this);
			drawable.setOnImageLoadedCallback(new OnImageLoaded() {
				@Override
				public void onImageLoaded(String url, Bitmap bitmap) {
					HotelMapImageView.this.setBackgroundDrawable(null);
				}

				@Override
				public void onImageLoadFailed(String url) {
					// nothing
				}
			});
		}
	}

	private String colorToHexString(int color) {
		return String.format("0x%06X", (0xFFFFFF & color));
	}

	private String getMarkerString(Location location, int color) {
		return "color:" + colorToHexString(color) + "|" + location.getLatitude() + "," + location.getLongitude();
	}
}
