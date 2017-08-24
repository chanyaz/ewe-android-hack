package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.Strings;
import com.google.gson.annotations.SerializedName;

public class LXActivity {

	public String id;
	public final String title;
	@SerializedName("imageUrl")
	public final String smallImageUrl;
	// API considers this large relative to the other. But its smaller than the large image in Details response.
	@SerializedName("largeImageURL")
	public final String mediumImageURL;
	public String fromPriceValue;
	public final LXTicketType fromPriceTicketCode;
	public final List<String> categories;
	public final String duration;
	public final String fromOriginalPriceValue;
	// True if the offers have different durations.
	public final boolean isMultiDuration;

	//Utility Members
	public String location;
	public String destination;
	public String regionId;
	public int freeCancellationMinHours;
	public Money price;
	public final Money originalPrice;
	public transient int popularityForClientSort;
	public final int recommendationScore;

	public List<LXImage> getImages() {
		List<LXImage> images = new ArrayList<>();
		if (Strings.isNotEmpty(smallImageUrl)) {
			images.add(new LXImage(smallImageUrl, LXImage.ImageSize.SMALL));
		}
		if (Strings.isNotEmpty(mediumImageURL)) {
			images.add(new LXImage(mediumImageURL, LXImage.ImageSize.MEDIUM));
		}
		return images;
	}
}
