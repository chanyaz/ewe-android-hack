package com.expedia.bookings.data;

import java.util.List;

import com.expedia.bookings.utils.Images;

public class LaunchCollection {
	public String title;
	public String id;
	public String imageCode;
	public List<LaunchLocation> locations;

	public String getImageUrl() {
		return Images.getTabletLaunch(imageCode);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LaunchCollection) {
			return title.equals(((LaunchCollection) o).title);
		}
		return false;
	}
}
