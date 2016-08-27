package com.expedia.bookings.data.insurance;

import java.util.List;

import com.expedia.bookings.data.Money;

public class InsuranceProduct {
	public List<InsuranceSolicitationItem> description;
	public Money displayPrice;
	public InsurancePriceType displayPriceType;
	public String name;
	public String productId;
	public InsuranceSolicitationItem terms;
	public String title;
	public Money totalPrice;
	public Money tripTotalPriceWithInsurance;
	public String typeId;
}
