package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;

public class LXCategoryMetadata {
	public String displayValue;
	// Using this only for client side manipulation, not using the server value.
	public boolean checked;

	// This does not come from API. Used as a utility.
	public transient List<LXActivity> activities = new ArrayList<>();
	// This is not localized and to be used for all lookups and non-display logic.
	public transient LXCategoryType categoryType = LXCategoryType.Unknown;
}
