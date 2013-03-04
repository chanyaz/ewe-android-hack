package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

public class ItineraryMapFragment extends SupportMapFragment implements OnGlobalLayoutListener {

	private static final float ZOOM_LEVEL = 13;

	private Marker mMarker;

	private float mMarkerHeight;
	private float mBottomPadding;
	private float mOffsetCenterY;

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getMap().setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				// Consume all click events so users can't interact with markers
				return true;
			}
		});

		// Create an invisible marker that we will move around the screen
		// as itineraries are shown.
		MarkerOptions opts = new MarkerOptions();
		opts.position(new LatLng(0, 0));
		opts.visible(false);
		mMarker = getMap().addMarker(opts);

		// TODO: Empirically speaking this is the correct marker height for default markers;
		// however once we implement our own markers we will want to specify the height ourselves.
		Resources res = getResources();
		mMarkerHeight = 40 * res.getDisplayMetrics().density;
		mBottomPadding = res.getDimension(R.dimen.itin_map_marker_bottom_padding);

		view.getViewTreeObserver().addOnGlobalLayoutListener(this);

		// Set the initial zoom level; otherwise all of our camera updates will be off target
		moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	public void showItinItem(ItinCardData data) {
		LatLng position = null;

		mMarker.setVisible(true);

		long now = Calendar.getInstance().getTimeInMillis();

		if (data instanceof ItinCardDataFlight) {
			ItinCardDataFlight flightData = (ItinCardDataFlight) data;

			Flight flight = flightData.getMostRelevantFlightSegment();
			Waypoint waypoint = flight.mOrigin.getMostRelevantDateTime().getTimeInMillis() > now ? flight.mOrigin
					: flight.getArrivalWaypoint();
			Airport airport = waypoint.getAirport();
			position = new LatLng(airport.getLatitude(), airport.getLongitude());
		}
		else if (data instanceof ItinCardDataHotel) {
			ItinCardDataHotel hotelData = (ItinCardDataHotel) data;

			Location loc = hotelData.getPropertyLocation();
			position = new LatLng(loc.getLatitude(), loc.getLongitude());
		}
		else if (data instanceof ItinCardDataCar) {
			ItinCardDataCar carData = (ItinCardDataCar) data;
			Location loc = carData.getPickUpDate().getMillisFromEpoch() > now ? carData.getPickUpLocation() : carData
					.getDropOffLocation();
			position = new LatLng(loc.getLatitude(), loc.getLongitude());
		}

		if (position == null) {
			position = new LatLng(0, 0);
		}

		if (position.latitude == 0 && position.longitude == 0) {
			mMarker.setVisible(false);
		}
		else {
			mMarker.setPosition(position);
		}

		// Move the camera to match the new position; but make it so that the marker is at the bottom of the screen
		Projection projection = getMap().getProjection();
		Point screenLoc = projection.toScreenLocation(position);
		screenLoc.y -= mOffsetCenterY;
		LatLng camLatLng = projection.fromScreenLocation(screenLoc);

		CameraPosition camPos = new CameraPosition(camLatLng, ZOOM_LEVEL, 0, 0);
		animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
	}

	//////////////////////////////////////////////////////////////////////////
	// OnGlobalLayoutListener
	//
	// We want to know the height of the fragment so we can determine the 
	// offset for the camera movements.

	@Override
	public void onGlobalLayout() {
		mOffsetCenterY = (getView().getHeight() / 2.0f) - mMarkerHeight - mBottomPadding;
	}

}
