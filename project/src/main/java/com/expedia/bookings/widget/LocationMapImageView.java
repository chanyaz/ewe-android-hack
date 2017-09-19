package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.data.cars.LatLong;
import com.mobiata.android.Log;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;
import com.squareup.picasso.Picasso;

public class LocationMapImageView extends ImageView {

	private static int sZoom = 13; // We want this to be different depending on dpi too
	private static int sDensityScaleFactor = 1; // This has to be calculated at runtime

	private String mStaticMapUri;
	private LatLong mLocation;

	public LocationMapImageView(Context context) {
		super(context);
		initMapView();
	}

	public LocationMapImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LocationMapImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMapView();
	}

	private void initMapView() {
		Resources res = getResources();

		// High DPI screens should utilize scale=2 for this API
		// https://developers.google.com/maps/documentation/staticmaps/
		if (res.getDisplayMetrics().density > 1.5) {
			sDensityScaleFactor = 2;
			sZoom = 12;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w > 0 && h > 0) {
			regenerateUri();
		}
	}

	public void setZoom(int zoom) {
		sZoom = zoom;
	}

	public void setLocation(LatLong location ) {
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

		mStaticMapUri = GoogleServices.getStaticMapUrl(width / sDensityScaleFactor, height / sDensityScaleFactor,
				sZoom, MapType.ROADMAP, mLocation.getLatitude(), mLocation.getLongitude(),
				getMarkerString(mLocation, 0x126299));

		Log.d("ITIN: mapUrl:" + mStaticMapUri);

		if (!mStaticMapUri.equals(oldUri)) {
			new PicassoHelper.Builder(this)
				.setTarget(callback)
				.build()
				.load(mStaticMapUri);
		}
	}

	private PicassoTarget callback = new PicassoTarget() {
		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			super.onBitmapLoaded(bitmap, from);
			setImageBitmap(bitmap);
			LocationMapImageView.this.setBackgroundDrawable(null);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			super.onBitmapFailed(errorDrawable);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			super.onPrepareLoad(placeHolderDrawable);
		}
	};

	private String colorToHexString(int color) {
		return String.format("0x%06X", (0xFFFFFF & color));
	}

	private String getMarkerString(LatLong location, int color) {
		return "color:" + colorToHexString(color) + "|" + location.getLatitude() + "," + location.getLongitude();
	}
}
