package com.expedia.bookings.data.lx;

import java.util.List;

public class ActivityDetailsResponse {
	public String title;
	public String description;
	public List<ActivityImages> images;
	public String fromPrice;
	public LXTicketType fromPriceTicketCode;
	public String category;
	public String location;
	public List<String> highlights;
	public OffersDetail offersDetail;
	public String currencyCode;
}
