package com.expedia.bookings.gear;


import java.util.Set;

import org.joda.time.DateTime;
import org.json.JSONObject;

import android.util.Log;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripHotel;

public class HotelBookingResponse extends GearResponse {

	@Override
	public JSONObject getResponseForGear() {

		generateHotelResponse();

		return responseForGear;
	}

	public void generateHotelResponse() {

		TripComponent component = this.tripComponent;
		TripHotel compHotel = (TripHotel) component;
		Property hotelProperty = compHotel.getProperty();
		Location hotelLocation = hotelProperty.getLocation();
		Set<String> confNumbers = compHotel.getConfirmationNumbers();
		String cNos = null;
		if (confNumbers != null && !confNumbers.isEmpty()) {
			cNos = confNumbers.iterator().next();
		}
		DateTime startDate = compHotel.getStartDate();
		responseForGear = new JSONObject();
		try {
			responseForGear.put("type", "HOTEL");
			responseForGear.put("checkInTime", compHotel.getCheckInTime());
			responseForGear.put("checkOutTime", compHotel.getCheckOutTime());
			responseForGear.put("startDate", startDate);
			responseForGear.put("startDateMS", startDate.getMillis());
			responseForGear.put("endDate", compHotel.getEndDate());
			responseForGear.put("hotelName", hotelProperty.getName());
			responseForGear.put("streetAddress", hotelLocation.getStreetAddress().get(0));
			responseForGear.put("city", hotelLocation.getCity());
			responseForGear.put("stateCode", hotelLocation.getStateCode());
			responseForGear.put("postalCode", hotelLocation.getPostalCode());
			responseForGear.put("confNum", cNos);

		} catch (Exception e) {
			Log.e(GearAccessoryProviderService.TAG, " Exception HotelsBookingResponse -- ", e);
		}

	}


}