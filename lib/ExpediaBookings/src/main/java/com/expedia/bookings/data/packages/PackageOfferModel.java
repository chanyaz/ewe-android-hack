package com.expedia.bookings.data.packages;

import java.math.BigDecimal;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.multiitem.MultiItemOffer;
import com.expedia.bookings.data.multiitem.PackageDeal;
import com.expedia.bookings.data.payment.LoyaltyInformation;

public class PackageOfferModel {
	public String piid;
	public String hotel;
	public String flight;
	public PackagePrice price;
	public UrgencyMessage urgencyMessage;
	public BrandedDealData brandedDealData;
	public boolean featuredDeal;
	public LoyaltyInformation loyaltyInfo;

	public PackageOfferModel() {
		//default constructor
	}

	public PackageOfferModel(MultiItemOffer multiItemOffer) {
		loyaltyInfo = multiItemOffer.getLoyaltyInfo();

		price = new PackagePrice();
		price.packageTotalPrice = multiItemOffer.getPrice().packageTotalPrice();
		price.packageReferenceTotalPrice = multiItemOffer.getPrice().packageReferenceTotalPrice();
		price.tripSavings = multiItemOffer.getPrice().packageSavings();
		price.pricePerPerson = multiItemOffer.getPrice().pricePerPerson();
		price.pricePerPersonFormatted = multiItemOffer.getPrice().pricePerPerson().getFormattedMoneyFromAmountAndCurrencyCode();
		price.showTripSavings = multiItemOffer.getPrice().getShowSavings();

		Money deltaPricePerPerson = multiItemOffer.getPrice().deltaPricePerPerson();
		if (deltaPricePerPerson != null) {
			setDeltaPriceAsPerFormat(deltaPricePerPerson, Money.F_NO_DECIMAL);
		}

		PackageDeal packageDeal = multiItemOffer.getPackageDeal();
		if (packageDeal != null && packageDeal.getDeal() != null) {
			featuredDeal = true;

			brandedDealData = new BrandedDealData();
			brandedDealData.dealVariation = packageDeal.getDeal().getSticker();
			brandedDealData.savingsAmount = Double.toString(packageDeal.getSavingsAmount());
			brandedDealData.savingPercentageOverPackagePrice = Double.toString(packageDeal.getSavingsPercentage());
			brandedDealData.freeNights = packageDeal.getDeal().getMagnitude();
		}
	}

	private void setDeltaPriceAsPerFormat(Money deltaPricePerPerson, int flag) {
		price.differentialPriceFormatted = deltaPricePerPerson.getFormattedMoney(flag);
		//For values -0.5<{val}<0, since differentialPriceFormatted comes out to be 0,
		// we need to have deltaPositive as true, since -0 does not makes sense
		if (flag == Money.F_NO_DECIMAL) {
			price.deltaPositive = deltaPricePerPerson.amount.compareTo(BigDecimal.valueOf(-0.5)) >= 0;
		}
	}

	public static class PackagePrice {
		public Money averageTotalPricePerTicket;
		public Money packageTotalPrice;
		public Money packageReferenceTotalPrice;
		public Money pricePerPerson;
		public Money hotelPrice;
		public Money flightPrice;
		public Money discountAmount;
		public Money tripSavings;
		public Money deltaPrice;
		public String packageTotalPriceFormatted;
		public String pricePerPersonFormatted;
		public String differentialPriceFormatted;
		public String flightPlusHotelPricePerPersonFormatted;
		public boolean showTripSavings;
		public boolean deltaPositive;
	}

	public static class UrgencyMessage {
		public int ticketsLeft;
	}

	public static class BrandedDealData {
		public DealVariation dealVariation;
		public String savingsAmount;
		public String savingPercentageOverPackagePrice;
		public String freeNights;
	}

	public enum DealVariation {
		FreeHotel,
		FreeFlight,
		HotelDeal,
		FreeOneNightHotel
	}
}

