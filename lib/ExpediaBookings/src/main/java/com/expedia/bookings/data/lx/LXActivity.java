package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.Strings;
import com.google.gson.annotations.SerializedName;

public class LXActivity {

	public String id;
	public String title;
	@SerializedName("imageUrl")
	public String smallImageUrl;
	// API considers this large relative to the other. But its smaller than the large image in Details response.
	@SerializedName("largeImageURL")
	public String mediumImageURL;
	public String fromPriceValue;
	public LXTicketType fromPriceTicketCode;
	public List<String> categories;
	public String duration;
	public String fromOriginalPriceValue;
	// True if the offers have different durations.
	public boolean isMultiDuration;
	public String mipFromOriginalPriceValue;
	public String mipFromPriceValue;
	public String discountType;
	public int mipDiscountPercentage;
	public int discountPercentage;

	//Utility Members
	public String location;
	public String destination;
	public String regionId;
	public int freeCancellationMinHours;
	public Money price;
	public Money originalPrice;
	public transient int popularityForClientSort;
	public int recommendationScore;
	public Money mipOriginalPrice;
	public Money mipPrice;

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

	public boolean modPricingEnabled(boolean modTestEnabled) {
		return modTestEnabled && mipDiscountPercentage != 0;
	}
}
