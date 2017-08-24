package com.expedia.bookings.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents all properties in a particular price tier, along with some metadata about them.
 */
public class PriceTier {
	private final Set<Property> mProperties;

	private Money mMin;

	private Money mMax;

	public PriceTier(Collection<Property> properties) {
		mProperties = new HashSet<Property>(properties);

		// Calculate the min/max rates of the properties.  Assumes all are using the
		// same currency
		mMin = new Money();
		mMin.setAmount(new BigDecimal(Double.MAX_VALUE));
		mMax = new Money();
		mMax.setAmount(new BigDecimal(Double.MIN_VALUE));
		for (Property property : mProperties) {
			Money lowRate = property.getLowestRate().getDisplayPrice();
			if (lowRate != null) {
				BigDecimal amount = lowRate.getAmount();
				if (amount.compareTo(mMin.getAmount()) < 0) {
					mMin = lowRate;
				}
				if (amount.compareTo(mMax.getAmount()) > 0) {
					mMax = lowRate;
				}
			}
		}
	}

	public boolean containsProperty(Property property) {
		return mProperties.contains(property);
	}

	public Money getMinRate() {
		return mMin;
	}

	public Money getMaxRate() {
		return mMax;
	}
}
