package com.expedia.bookings.data.lx;

import java.util.List;

import com.expedia.bookings.data.Money;

public class LXActivity {

	public String id;
	public String title;
	public String imageUrl;
	public String fromPriceValue;
	public LXTicketType fromPriceTicketCode;
	public List<String> categories;
	public String duration;
	// True if the offers have different durations.
	public boolean isMultiDuration;
	// Location does not come from the search response and taken from the activity details.
	public String location;
	public Money price;
	public String bestApplicableCategoryEN;
	public String bestApplicableCategoryLocalized;

}
