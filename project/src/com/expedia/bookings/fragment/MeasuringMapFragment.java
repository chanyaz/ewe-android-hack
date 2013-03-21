package com.expedia.bookings.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MeasuringMapFragment extends SupportMapFragment {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Set invisible to avoid rendering
		view.setVisibility(View.INVISIBLE);
	}

	public CameraPosition measure(LatLng target, float zoom, float centerOffsetX, float centerOffsetY) {
		getMap().moveCamera(CameraUpdateFactory.zoomTo(zoom));
		LatLng newLatLng = offsetLatLng(getMap(), target, centerOffsetX,
				centerOffsetY);
		return new CameraPosition(newLatLng, zoom, 0, 0);
	}

	public static LatLng offsetLatLng(GoogleMap map, LatLng latLng, float offsetX, float offsetY) {
		if (offsetX == 0 && offsetY == 0) {
			return latLng;
		}

		Projection projection = map.getProjection();
		Point screenLocation = projection.toScreenLocation(latLng);
		screenLocation.x += offsetX;
		screenLocation.y += offsetY;
		return projection.fromScreenLocation(screenLocation);
	}
}
