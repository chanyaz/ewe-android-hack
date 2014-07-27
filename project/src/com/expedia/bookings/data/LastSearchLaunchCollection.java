package com.expedia.bookings.data;

import com.expedia.bookings.utils.Images;

public class LastSearchLaunchCollection extends LaunchCollection {

	@Override
	public String getImageUrl() {
		return Images.getFlightDestination(imageCode);
	}
}
