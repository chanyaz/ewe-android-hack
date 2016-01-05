package com.expedia.bookings.data.packages;

public class PackageOfferModel {
	public String piid;
	public String hotel;
	public String flight;
	public Price price;

	public static class Price {
		public HotelPrice packageTotalPrice;
		public HotelPrice pricePerPerson;
		public HotelPrice hotelPrice;
		public HotelPrice flightPrice;
		public HotelPrice sumFlightAndHotel;
		public HotelPrice tripSavings;
		public HotelPrice hotelAvgPricePerNight;
		public String packageTotalPriceFormatted;
		public String pricePerPersonFormatted;
		public String hotelPriceFormatted;
		public String flightPriceFormatted;
		public String sumFlightAndHotelFormatted;
		public String tripSavingsFormatted;
		public String hotelAvgPricePerNightFormatted;
		public int percentageSavings;
	}

	public static class HotelPrice {
		public double amount;
		public String currency;
		public String currencyCode;
	}
}
