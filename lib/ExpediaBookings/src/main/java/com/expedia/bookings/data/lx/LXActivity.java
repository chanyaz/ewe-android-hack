package com.expedia.bookings.data.lx;

import java.util.List;

public class LXActivity {

	public String id;
	public String title;
	public String imageUrl;
	public String fromPrice;
	public LXTicketType fromPriceTicketCode;
	public List<String> categories;
	public String duration;
	// True if the offers have different durations.
	public boolean isMultiDuration;
	// Currency code and location do not come from the search response and taken from the activity details.
	public String currencyCode;
	public String location;
	public String bestApplicableCategoryEN;
	public String bestApplicableCategoryLocalized;
}
