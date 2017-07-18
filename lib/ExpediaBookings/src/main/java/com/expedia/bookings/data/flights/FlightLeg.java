package com.expedia.bookings.data.flights;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.expedia.bookings.data.packages.PackageOfferModel;

public class FlightLeg {
	public int airBookingProviderCode;
	public String airFareBasisCode;
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
	public List<FlightSegment> segments;
//	flightSegments
	public JSONObject freeCancellationBy;
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

	public PackageOfferModel packageOfferModel;
	public String flightPid;
	public String departureLeg;
	public List<Airline> airlines = new ArrayList<>();
	public boolean isBestFlight;
	public boolean mayChargeObFees;
	public boolean isBasicEconomy;
	public List<BasicEconomyTooltipInfo> basicEconomyTooltipInfo = new ArrayList<>();
	public int legRank;

	public boolean isFreeCancellable() {
		return freeCancellationBy != null;
	}

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
		public String airlineCode;
		public String airlineName;
		public String airlineLogoURL;
		public String airlineLogo;
		public String equipmentDescription;
		public boolean displayOperatedByAirlineName;
		public String operatingAirlineName;
		public String operatingAirlineCode;

		public String departureCity;
		public String departureAirport;
		public String departureAirportCode;
		public String departureAirportLocation;
		public String departureTime;
		public String departureTimeRaw;
		public String departureDateTimeISO;
		public AirportAddress departureAirportAddress;

		public static class AirportAddress {
			public String city;
			public String country;
			public String state;
		}

		public String arrivalCity;
		public String arrivalAirport;
		public String arrivalAirportCode;
		public String arrivalAirportLocation;
		public String arrivalTime;
		public String arrivalTimeRaw;
		public String arrivalDateTimeISO;
		public AirportAddress arrivalAirportAddress;

		public String duration;
		public int durationHours;
		public int durationMinutes;
		public int layoverDurationHours;
		public int layoverDurationMinutes;
		public int elapsedDays;

		public String seatClass;
		public String bookingCode;
	}

	public static class BasicEconomyTooltipInfo {
		public String fareRulesTitle;
		public String[] fareRules;
	}
}
