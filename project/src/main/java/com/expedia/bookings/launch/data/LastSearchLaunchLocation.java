package com.expedia.bookings.launch.data;

import com.expedia.bookings.utils.Images;

public class LastSearchLaunchLocation extends LaunchLocation {
	public String getImageUrl() {
		return Images.getFlightDestination(imageCode);
	}

}
