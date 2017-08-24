package com.expedia.bookings.launch.data;

import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.utils.Images;

public class LaunchLocation {
	public String title;
	public String subtitle;
	public String description;
	public String id;
	public final String imageCode;
	public SuggestionV2 location;

	public String getImageUrl() {
		return Images.getTabletLaunch(imageCode);
	}
}
