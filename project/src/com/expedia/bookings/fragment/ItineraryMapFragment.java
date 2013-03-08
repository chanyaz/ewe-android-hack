package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

public class ItineraryMapFragment extends SupportMapFragment {

	private static final float ZOOM_LEVEL = 13;

	private Marker mMarker;

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

		Activity activity = getActivity();
		if (activity instanceof OnCameraChangeListener) {
			getMap().setOnCameraChangeListener((OnCameraChangeListener) activity);
		}

		// Create an invisible marker that we will move around the screen
		// as itineraries are shown.
		MarkerOptions opts = new MarkerOptions();
		opts.position(new LatLng(0, 0));
		opts.visible(false);
		opts.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_normal));
		mMarker = getMap().addMarker(opts);

		// Set the initial zoom level; otherwise all of our camera updates will be off target
		moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
	}

	public boolean showItinItem(ItinCardData data) {
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

		CameraPosition origPosition = getMap().getCameraPosition();

		LatLng camLatLng = position;
		if (position.latitude != 0 || position.longitude != 0) {
			// Quickly set correct zoom level so we calculate offset correctly.  It's noticeable, but
			// only does anything if we're mid-animation anyways so it doesn't really matter.
			moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));

			camLatLng = offsetLatLng(camLatLng);
		}

		CameraPosition camPos = new CameraPosition(camLatLng, ZOOM_LEVEL, 0, 0);
		animateCamera(CameraUpdateFactory.newCameraPosition(camPos));

		return !origPosition.equals(camPos);
	}
}
