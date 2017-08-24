package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LXSearchResponse {
	public final String regionId;
	public String startDate;
	public String endDate;
	public List<LXActivity> activities;
	public final Map<String, LXCategoryMetadata> filterCategories;
	public boolean searchFailure;
	public String currencyCode;
	public final boolean isFromCachedResponse;
	public String destination;

	// Utility Members - Required for Filtering
	public transient List<LXActivity> unFilteredActivities = new ArrayList<>();
	public final transient List<LXTheme> lxThemes = new ArrayList<>();

	public LXActivity getActivityFromID(String activityId) {
		for (LXActivity lxActivity : activities) {
			if (lxActivity.id.equals(activityId)) {
				return lxActivity;
			}
		}
		return null;
	}

	public LXActivity getLowestPriceActivity() {
		LXActivity lowestPriceActivity = null;
		for (LXActivity activity : activities) {
			if (lowestPriceActivity == null
				|| activity.price.getAmount().compareTo(lowestPriceActivity.price.getAmount()) < 0) {
				lowestPriceActivity = activity;
			}
		}
		return lowestPriceActivity;
	}
}
