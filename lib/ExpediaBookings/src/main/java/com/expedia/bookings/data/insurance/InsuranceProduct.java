package com.expedia.bookings.data.insurance;

import java.util.List;

import com.expedia.bookings.data.Money;

public class InsuranceProduct {
	public final List<InsuranceSolicitationItem> description;
	public final Money displayPrice;
	public final InsurancePriceType displayPriceType;
	public final String name;
	public final String productId;
	public final InsuranceSolicitationItem terms;
	public final String title;
	public final Money totalPrice;
	public final Money tripTotalPriceWithInsurance;
	public final String typeId;
}
