package com.expedia.bookings.data.lx;

import java.util.List;
import java.util.Map;

public class LXSearchResponse {
	public String regionId;
	public String startDate;
	public String endDate;
	public List<LXActivity> activities;
	public Map<String, LXCategoryMetadata> filterCategories;
	public boolean searchFailure;
	public String currencyCode;
}
