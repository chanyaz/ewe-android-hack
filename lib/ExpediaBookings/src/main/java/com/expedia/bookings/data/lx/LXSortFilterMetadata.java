package com.expedia.bookings.data.lx;

import java.util.HashMap;
import java.util.Map;

public class LXSortFilterMetadata {
	public static final String DEEPLINK_FILTER_DILIMITER = "\\|";

	public Map<String, LXCategoryMetadata> lxCategoryMetadataMap;
	public LXSortType sort;
	public String filter;

	public LXSortFilterMetadata() {
	}

	public LXSortFilterMetadata(
		Map<String, LXCategoryMetadata> lxCategoryMetadataMap, LXSortType sort) {
		this(lxCategoryMetadataMap, sort, "");
	}

	public LXSortFilterMetadata(
		Map<String, LXCategoryMetadata> lxCategoryMetadataMap, LXSortType sort, String filter) {
		this.lxCategoryMetadataMap = lxCategoryMetadataMap;
		this.sort = sort;
		this.filter = filter;
	}

	public LXSortFilterMetadata(String delimitedFiltersList) {
		this(delimitedFiltersToMap(delimitedFiltersList), LXSortType.POPULARITY);
	}

	private static Map<String, LXCategoryMetadata> delimitedFiltersToMap(String delimitedFilters) {
		Map<String, LXCategoryMetadata> filterCategories = new HashMap<>();

		String[] filter = delimitedFilters.split(DEEPLINK_FILTER_DILIMITER);
		for (String filterDisplayValue : filter) {
			LXCategoryMetadata lxCategoryMetadata = new LXCategoryMetadata();
			lxCategoryMetadata.checked = true;
			lxCategoryMetadata.displayValue = filterDisplayValue;
			filterCategories.put(filterDisplayValue, lxCategoryMetadata);
		}

		return filterCategories;
	}
}
