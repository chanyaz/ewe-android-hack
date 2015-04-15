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

	//Utility Members
	public String location;
	public String regionId;
	public Money price;
	public String bestApplicableCategoryEN;
}
