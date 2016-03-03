package com.expedia.bookings.data.packages;

import java.util.ArrayList;
import java.util.List;

public class FlightLeg {
	public int airBookingProviderCode;
	public String airFareBasisCode;
	public String airlineLogo;
	public String airlineLogoURL;
	public AirlineMessageModel airlineMessageModel;
	public String arrivalDateFormatted;
	public String arrivalDateTimeISO;
	public String arrivalTimeShort;
	public String baggageFeesUrl;
	public String carrierCode;
	public String carrierName;
	public String departureDateFormatted;
	public String departureDateTimeISO;
	public String departureTimeShort;
	public String destinationAirport;
	public String destinationAirportCode;
	public String destinationAirportLocalName;
	public String destinationCity;
	public int durationHour;
	public int durationMinute;
	public int elapsedDays;
	public String flightFareTypeString;
	public List<FlightSegment> flightSegments;
//	flightSegments
	public boolean freeFlightPromotion;
	public boolean handBaggageOnly;
	public boolean hasAllTrainSegments;
	public boolean hasLayover;
	public boolean hasTrainSegment;
	public String legId;
	public boolean mixedCarrierFlight;
	public String originAirport;
	public String originAirportCode;
	public String originAirportLocalName;
	public String originCity;
	public boolean outbound;
	public String promoCampaignLogoURI;
	public List<String> seatMapUrlList;
	public int stopCount;
	public String totalTravelDistance;
	public String totalTravelDistanceUnits;

	public transient PackageOfferModel packageOfferModel;
	public transient String flightPid;
	public transient String departureLeg;
	public transient List<Airline> airlines = new ArrayList<>();
	public transient boolean isBestFlight;

	public static class AirlineMessageModel {
		public String airlineFeeLink;
		public String airlineName;
		public boolean  hasAirlineWithBagfee;
		public boolean  hasAirlineWithCCfee;
		public boolean  hasAirlineWithoutBagfee;
		public boolean  hasAirlineWithSpecialBagfee;
	}

	public static class FlightSegment {
		public String airplaneType;
		public String flightNumber;
		public String carrier;
		public boolean displayOperatedByAirlineName;
		public String operatedByAirlineName;
		public String operatingAirlineCode;

		public String departureCity;
		public String departureAirport;
		public String departureAirportCode;
		public String departureTime;

		public String arrivalCity;
		public String arrivalAirport;
		public String arrivalAirportCode;
		public String arrivalTime;

		public int durationHours;
		public int durationMinutes;
		public int layoverDurationHours;
		public int layoverDurationMinutes;

		public String seatClass;
	}
}
