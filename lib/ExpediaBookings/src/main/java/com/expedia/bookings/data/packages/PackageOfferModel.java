package com.expedia.bookings.data.packages;

import com.expedia.bookings.data.Money;

public class PackageOfferModel {
	public String piid;
	public String hotel;
	public String flight;
	public PackagePrice price;

	public static class PackagePrice {
		public Money packageTotalPrice;
		public Money pricePerPerson;
		public Money hotelPrice;
		public Money flightPrice;
		public Money sumFlightAndHotel;
		public Money tripSavings;
		public Money hotelAvgPricePerNight;
		public String packageTotalPriceFormatted;
		public String pricePerPersonFormatted;
		public String hotelPriceFormatted;
		public String flightPriceFormatted;
		public String sumFlightAndHotelFormatted;
		public String tripSavingsFormatted;
		public String hotelAvgPricePerNightFormatted;
		public String differentialPriceFormatted;
		public int percentageSavings;
		public boolean deltaPositive;
	}
}
