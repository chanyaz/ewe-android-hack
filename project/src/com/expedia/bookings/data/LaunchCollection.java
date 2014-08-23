package com.expedia.bookings.data;

import java.util.List;

import android.text.TextUtils;

import com.expedia.bookings.utils.Images;

public class LaunchCollection {
	public String title;
	public String id;
	public String imageCode;
	public List<LaunchLocation> locations;

	// Not parsed from json
	public CharSequence stylizedTitle;

	public String getImageUrl() {
		return Images.getTabletLaunch(imageCode);
	}

	public boolean hasStylizedTitle() {
		return !TextUtils.isEmpty(stylizedTitle);
	}

	public CharSequence getTitle() {
		if (hasStylizedTitle()) {
			return stylizedTitle;
		}

		return title;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LaunchCollection) {
			return title.equals(((LaunchCollection) o).title);
		}
		return false;
	}
}
