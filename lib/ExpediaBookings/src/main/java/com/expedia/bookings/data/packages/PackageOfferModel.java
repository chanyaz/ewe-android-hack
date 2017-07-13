package com.expedia.bookings.data.packages;

import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.flights.FlightTripDetails.SeatClassAndBookingCode;
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
	public List<SeatClassAndBookingCode> segmentsSeatClassAndBookingCode;

	public PackageOfferModel() {
		//default constructor
	}

	public PackageOfferModel(MultiItemOffer multiItemOffer) {
		loyaltyInfo = multiItemOffer.getLoyaltyInfo();

		PackageDeal packageDeal = multiItemOffer.getPackageDeal();
		if (packageDeal != null && packageDeal.getDeal() != null) {
			featuredDeal = true;//TODO PUK confirm

			brandedDealData = new BrandedDealData();
			brandedDealData.dealVariation = packageDeal.getDeal().getSticker();
			brandedDealData.savingsAmount = "" + packageDeal.getSavingsAmount();
			brandedDealData.savingPercentageOverPackagePrice = "" + packageDeal.getSavingsPercentage();
			brandedDealData.freeNights = packageDeal.getDeal().getMagnitude();
		}
	}

	public static class PackagePrice {
		public Money averageTotalPricePerTicket;
		public Money packageTotalPrice;
		public Money pricePerPerson;
		public Money hotelPrice;
		public Money flightPrice;
		public Money tripSavings;
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

