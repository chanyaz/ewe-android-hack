package com.expedia.bookings.data;

import java.util.List;

public class LaunchCollection {
	public String title;
	public String id;
	public String imageCode;
	public List<LaunchLocation> locations;

	public static final String LAUNCH_MEDIA = "http://media.expedia.com/mobiata/mobile/apps/ExpediaBooking/LaunchDestinations/images/";

	public String getImageUrl() {
		return LAUNCH_MEDIA + imageCode + ".jpg";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LaunchCollection) {
			return title.equals(((LaunchCollection) o).title);
		}
		return false;
	}
}
