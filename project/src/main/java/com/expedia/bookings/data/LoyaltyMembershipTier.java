package com.expedia.bookings.data;

import android.support.annotation.Nullable;

import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;

public enum LoyaltyMembershipTier {
	NONE, BASE, MIDDLE, TOP;

	public boolean isMidOrTopTier() {
		return this == MIDDLE || this == TOP;
	}

	private static final String[] expectedApiValues = ProductFlavorFeatureConfiguration.getInstance().getRewardTierAPINames();

	public static LoyaltyMembershipTier fromApiValue(@Nullable String apiValue) {

		if (apiValue != null && expectedApiValues != null && expectedApiValues.length > 0) {
			if (apiValue.equalsIgnoreCase(expectedApiValues[0])) {
				return BASE;
			}
			else if (expectedApiValues.length > 1 && apiValue.equalsIgnoreCase(expectedApiValues[1])) {
				return MIDDLE;
			}
			else if (expectedApiValues.length > 2 && apiValue.equalsIgnoreCase(expectedApiValues[2])) {
				return TOP;
			}
		}

		return NONE;
	}

	public String toApiValue() {
		if (expectedApiValues != null) {
			switch (this) {
			case NONE:
				return null;
			case BASE:
				return expectedApiValues.length > 0 ? expectedApiValues[0] : null;
			case MIDDLE:
				return expectedApiValues.length > 1 ? expectedApiValues[1] : null;
			case TOP:
				return expectedApiValues.length > 2 ? expectedApiValues[2] : null;
			}
		}

		return null;
	}
}
