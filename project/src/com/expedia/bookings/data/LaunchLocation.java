package com.expedia.bookings.data;

public class LaunchLocation {
	public String title;
	public String subtitle;
	public String description;
	public String id;
	public String imageCode;
	public SuggestionV2 location;

	public String getImageUrl() {
		return LaunchCollection.LAUNCH_MEDIA + imageCode + ".jpg";
	}
}
