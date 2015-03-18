package com.expedia.bookings.data.lx;

import java.util.List;

public class ActivityDetailsResponse {
	public String id;
	public String title;
	public String description;
	public List<ActivityImages> images;
	public String fromPrice;
	public LXTicketType fromPriceTicketCode;
	public String bestApplicableCategoryEN;
	public String bestApplicableCategoryLocalized;
	public String location;
	public List<String> highlights;
	public OffersDetail offersDetail;
	public String currencyCode;
	public String regionId;
}
