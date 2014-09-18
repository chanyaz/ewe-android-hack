package com.expedia.bookings.data;

import com.expedia.bookings.utils.Images;

public class LastSearchLaunchLocation extends LaunchLocation {
	public String getImageUrl() {
		return Images.getFlightDestination(imageCode);
	}

}
