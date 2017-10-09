package com.expedia.bookings.data.flights;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Period;
import org.json.JSONObject;

import com.expedia.bookings.data.multiitem.FlightOffer;
import com.expedia.bookings.data.multiitem.MultiItemFlightLeg;
import com.expedia.bookings.data.multiitem.MultiItemFlightSegment;
import com.expedia.bookings.data.multiitem.MultiItemOffer;
import com.expedia.bookings.data.packages.PackageOfferModel;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.DateUtils;

public class FlightLeg {
	public AirlineMessageModel airlineMessageModel;
	public String arrivalDateTimeISO;
	public String arrivalTimeShort;
	public String baggageFeesUrl;
	public String carrierCode;
	public String carrierName;
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
	public boolean hasLayover;
	public String legId;
	public String originAirportCode;
	public String originCity;
	public boolean outbound;
	public int stopCount;

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

	public static FlightLeg convertMultiItemFlightLeg(String flightLegId, FlightOffer flightOffer, MultiItemFlightLeg multiItemFlightLeg,
		MultiItemOffer multiItemOffer) {

		FlightLeg flightLeg = new FlightLeg();

		PackageOfferModel.UrgencyMessage urgencyMessage = new PackageOfferModel.UrgencyMessage();
		urgencyMessage.ticketsLeft = flightOffer.getSeatsLeft();

		flightLeg.packageOfferModel = new PackageOfferModel(multiItemOffer);
		flightLeg.packageOfferModel.urgencyMessage = urgencyMessage;

		flightLeg.flightSegments = new ArrayList<>();
		for (MultiItemFlightSegment multiItemFlightSegment : multiItemFlightLeg.getSegments()) {
			FlightSegment flightSegment = FlightSegment.convertMultiItemFlightSegment(multiItemFlightSegment);
			flightLeg.flightSegments.add(flightSegment);
			flightLeg.airlines.add(new Airline(flightSegment.carrier, flightSegment.airlineLogoURL));
		}

		flightLeg.carrierName = multiItemFlightLeg.getSegments().get(0).getAirlineName();
		flightLeg.arrivalDateTimeISO = flightLeg.flightSegments.get(flightLeg.flightSegments.size() - 1).arrivalDateTimeISO;
		flightLeg.carrierCode = flightLeg.flightSegments.get(0).airlineCode;
		flightLeg.departureDateTimeISO = flightLeg.flightSegments.get(0).departureDateTimeISO;
		flightLeg.durationHour = 5;//TODO PUK
		flightLeg.durationMinute = 5;//TODO PUK
		flightLeg.elapsedDays = 1;//TODO PUK
		flightLeg.hasLayover = multiItemFlightLeg.getStops() > 1;
		flightLeg.legId = flightLegId;
		flightLeg.departureLeg = flightLegId;
		flightLeg.stopCount = multiItemFlightLeg.getStops();
		flightLeg.destinationCity = flightLeg.flightSegments.get(flightLeg.flightSegments.size() - 1).arrivalCity;
		flightLeg.destinationAirportCode = flightLeg.flightSegments.get(flightLeg.flightSegments.size() - 1).arrivalAirportCode;
		flightLeg.baggageFeesUrl = multiItemFlightLeg.getBaggageFeesUrl();
		return flightLeg;
	}

	public static class AirlineMessageModel {
		public String airlineFeeLink;
		public String airlineName;
		public boolean  hasAirlineWithCCfee;
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

		public static FlightSegment convertMultiItemFlightSegment(MultiItemFlightSegment multiItemFlightSegment) {
			FlightSegment flightSegment = new FlightSegment();
			if (multiItemFlightSegment.getAirplaneType() != null) {
				flightSegment.airplaneType = multiItemFlightSegment.getAirplaneType().getDescription();
			}
			else {
				flightSegment.airplaneType = "";
			}
			flightSegment.flightNumber = multiItemFlightSegment.getFlightNumber();
			flightSegment.carrier = multiItemFlightSegment.getAirlineName();
			flightSegment.airlineCode = multiItemFlightSegment.getAirlineCode();
			flightSegment.airlineLogoURL = Constants.AIRLINE_SQUARE_LOGO_BASE_URL.replace("**", flightSegment.airlineCode);

			flightSegment.departureCity = multiItemFlightSegment.getDepartureCity();
			flightSegment.departureAirportCode = multiItemFlightSegment.getDepartureAirportCode();
			flightSegment.departureDateTimeISO = multiItemFlightSegment.getDepartureDateTime();

			flightSegment.arrivalCity = multiItemFlightSegment.getArrivalCity();
			flightSegment.arrivalAirportCode = multiItemFlightSegment.getArrivalAirportCode();
			flightSegment.arrivalDateTimeISO = multiItemFlightSegment.getArrivalDateTime();

			Period durationPeriod = DateUtils.parseDurationFromISOFormat(multiItemFlightSegment.getFlightDuration());
			flightSegment.durationHours = durationPeriod.getHours();
			flightSegment.durationMinutes = durationPeriod.getMinutes();

			String layoverDuration = multiItemFlightSegment.getLayoverDuration();
			if (layoverDuration != null) {
				Period layoverPeriod = DateUtils.parseDurationFromISOFormat(layoverDuration);
				flightSegment.layoverDurationHours = layoverPeriod.getHours();
				flightSegment.layoverDurationMinutes = layoverPeriod.getMinutes();
			}
			flightSegment.elapsedDays = 0;//TODO PUK

			return flightSegment;
		}
	}

	public static class BasicEconomyTooltipInfo {
		public String fareRulesTitle;
		public String[] fareRules;
	}
}
