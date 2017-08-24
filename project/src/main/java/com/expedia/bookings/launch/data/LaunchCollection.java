package com.expedia.bookings.launch.data;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.expedia.bookings.utils.Images;

public class LaunchCollection {
	public final String title;
	public String id;
	public final String imageCode;
	public List<LaunchLocation> locations;

	// Not parsed from json
	public final CharSequence stylizedTitle;
	public final boolean isDestinationImageCode;
	public transient Drawable imageDrawable;

	public String getImageUrl() {
		if (!isDestinationImageCode) {
			return Images.getTabletLaunch(imageCode);
		}
		else {
			return Images.getTabletDestination(imageCode);
		}
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
