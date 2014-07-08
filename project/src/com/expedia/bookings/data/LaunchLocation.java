package com.expedia.bookings.data;

import com.expedia.bookings.utils.Images;

public class LaunchLocation {
	public String title;
	public String subtitle;
	public String description;
	public String id;
	public String imageCode;
	public SuggestionV2 location;

	public String getImageUrl() {
		return Images.getTabletLaunch(imageCode);
	}
}
