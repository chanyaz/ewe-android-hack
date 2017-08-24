package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LXTheme {
	public final Map<String, LXCategoryMetadata> filterCategories = new HashMap<>();
	public LXThemeType themeType;
	public final List<LXActivity> activities = new ArrayList<>();
	public String title;
	public String description;
	public String titleEN;

	public List<LXActivity> unfilteredActivities = new ArrayList<>();
	public LXTheme() {

	}
	public LXTheme(LXThemeType lxThemeType) {
		this.themeType = lxThemeType;
	}
}
