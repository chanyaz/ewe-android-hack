package com.expedia.bookings.data;

import com.expedia.bookings.enums.PassengerCategory;

public class PassengerCategoryPrice implements Comparable<PassengerCategoryPrice> {

	private PassengerCategory mPassengerCategory;
	private Money mTotalPrice;
	private Money mBasePrice;
	private Money mTaxesPrice;

	public PassengerCategoryPrice(PassengerCategory passengerCategory, Money totalPrice, Money basePrice, Money taxesPrice) {
		mPassengerCategory = passengerCategory;
		mTotalPrice = totalPrice;
		mBasePrice = basePrice;
		mTaxesPrice = taxesPrice;
	}

	// Setters

	public PassengerCategory getPassengerCategory() {
		return mPassengerCategory;
	}

	public Money getTotalPrice() {
		return mTotalPrice;
	}

	public Money getBasePrice() {
		return mBasePrice;
	}

	public Money getTaxes() {
		return mTaxesPrice;
	}

	///////////////////////////////////////////////////////////////////////////
	// Comparable

	// When we sort a Collection of type PassengerCategoryPrice, we want
	// adults (ADULT, SENIOR) always before children (CHILD, ADULT_CHILD)
	// always before infants (INFANT_IN_SEAT, INFANT_IN_LAP)

	@Override
	public int compareTo(PassengerCategoryPrice another) {
		return mPassengerCategory.compareTo(another.getPassengerCategory());
	}

}
