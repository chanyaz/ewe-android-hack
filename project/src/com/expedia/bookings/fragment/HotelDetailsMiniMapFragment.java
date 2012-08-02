package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;

public class HotelDetailsMiniMapFragment extends Fragment {
	ImageView mStaticMapImageView;
	String mStaticMapUri;

	private HotelMiniMapFragmentListener mListener;

	public static HotelDetailsMiniMapFragment newInstance() {
		return new HotelDetailsMiniMapFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelMiniMapFragmentListener)) {
			throw new RuntimeException(
					"HotelDetailsMiniMapFragment Activity must implement HotelMiniMapFragmentListener!");
		}

		mListener = (HotelMiniMapFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mStaticMapImageView = new MapImageView(getActivity());
		mStaticMapImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onMiniMapClicked();
			}
		});

		return mStaticMapImageView;
	}

	public void populateViews() {
		Property searchProperty = Db.getSelectedProperty();
		double latitude = searchProperty.getLocation().getLatitude();
		double longitude = searchProperty.getLocation().getLongitude();

		int width = mStaticMapImageView.getWidth();
		int height = mStaticMapImageView.getHeight();

		// High DPI screens should utilize scale=2 for this API
		// https://developers.google.com/maps/documentation/staticmaps/
		if (getResources().getDisplayMetrics().density > 1.5) {
			mStaticMapUri = GoogleServices.getStaticMapUrl(width / 2, height / 2, 12, MapType.ROADMAP, latitude,
					longitude) + "&scale=2";
		}
		else {
			mStaticMapUri = GoogleServices.getStaticMapUrl(width, height, 12, MapType.ROADMAP, latitude, longitude);
		}

		ImageCache.loadImage(mStaticMapUri, mStaticMapImageView);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (getActivity().isFinishing()) {
			Log.d("Clearing out map image.");

			ImageCache.removeImage(mStaticMapUri, true);
		}
	}

	private class MapImageView extends ImageView {
		private int mCircleRadius;

		public MapImageView(Context context) {
			super(context);

			Resources res = getResources();

			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = res.getDimensionPixelSize(R.dimen.hotel_details_map_visible_size) * 2;
			mCircleRadius = res.getDimensionPixelSize(R.dimen.mini_map_circle_radius);

			setScaleType(ScaleType.CENTER_CROP);
			setLayoutParams(new ViewGroup.LayoutParams(width, height));
			setImageResource(R.drawable.bg_gallery);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			super.onLayout(changed, left, top, right, bottom);

			if (changed && bottom - top > 0 && right - left > 0) {
				populateViews();
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			// Draw the hotel marker
			int x = getWidth() / 2;
			int y = getHeight() / 2;
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setARGB(0xC0, 0x47, 0x71, 0x99);
			canvas.drawCircle(x, y, mCircleRadius, paint);

			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(2);
			paint.setColor(Color.BLACK);
			canvas.drawCircle(x, y, mCircleRadius, paint);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelMiniMapFragmentListener {
		public void onMiniMapClicked();
	}
}
