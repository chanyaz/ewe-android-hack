package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.utils.Strings;
import com.google.gson.annotations.SerializedName;

public class ActivityImages {
	@SerializedName("url")
	public String mediumImageURL;
	@SerializedName("large")
	public String largeImageURL;

	public List<LXImage> getImages() {
		List<LXImage> images = new ArrayList<>();
		if (Strings.isNotEmpty(mediumImageURL)) {
			images.add(new LXImage(mediumImageURL, LXImage.ImageSize.MEDIUM));
		}
		if (Strings.isNotEmpty(largeImageURL)) {
			images.add(new LXImage(largeImageURL, LXImage.ImageSize.LARGE));
		}
		return images;
	}
}
