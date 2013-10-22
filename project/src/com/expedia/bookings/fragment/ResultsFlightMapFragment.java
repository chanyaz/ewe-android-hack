package com.expedia.bookings.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.mobiata.android.bitmaps.BitmapDrawable;

/**
 * ResultsFlightMapFragment: The hotel map fragment designed for tablet results 2013
 */
public class ResultsFlightMapFragment extends SvgMapFragment {

	private FrameLayout mRoot;

	public static ResultsFlightMapFragment newInstance() {
		ResultsFlightMapFragment frag = new ResultsFlightMapFragment();
		frag.setMapResource(R.raw.map_flight_details);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

		mRoot.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				mRoot.getViewTreeObserver().removeOnPreDrawListener(this);
				generateMap();
				return true;
			}
		});

		return mRoot;
	}

	private void generateMap() {
		int w = getMapImageView().getWidth();
		int h = getMapImageView().getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(Color.parseColor("#687887"));

		setHorizontalBounds(57, -140.828186, 57, 32.316284);

		// Draw scaled and translated map
		Canvas c = new Canvas(bitmap);
		c.setMatrix(getViewportMatrix());
		getMapPicture().draw(c);
		c.setMatrix(new Matrix());

		getMapImageView().setImageDrawable(new BitmapDrawable(bitmap));
	}
}
