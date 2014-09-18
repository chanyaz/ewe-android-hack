package com.expedia.bookings.data;

import android.text.TextUtils;

import com.expedia.bookings.utils.Images;

public class LastSearchLaunchCollection extends LaunchCollection {

	public String launchImageCode;

	@Override
	public String getImageUrl() {
		if (!TextUtils.isEmpty(launchImageCode)) {
			return Images.getTabletLaunch(launchImageCode);
		}
		else {
			return Images.getTabletDestination(imageCode);
		}
	}
}
