package com.expedia.bookings.data.user;

public class UserPreference {
	public enum Category {
		PRIMARY,
		ALTERNATE
	}

	public static UserPreference.Category parseCategoryString(String str) {
		if (str != null && str.equals("PRIMARY")) {
			return Category.PRIMARY;
		}
		else {
			return Category.ALTERNATE;
		}
	}
}
