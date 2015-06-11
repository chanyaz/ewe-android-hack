package com.mobiata.mocke3

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.Calendar
import java.util.Date

// Mocks out various mobile Expedia APIs
public class ExpediaDispatcher(protected var fileOpener: FileOpener) : Dispatcher() {

	private val travelAdRequests = hashMapOf<String, Int>()

	@throws(InterruptedException::class)
	override fun dispatch(request: RecordedRequest): MockResponse {

		// Hotels API
		if (request.getPath().startsWith("/m/api/hotel") || request.getPath().startsWith("/api/m/trip/coupon")) {
			return dispatchHotel(request)
		}

		// Flights API
		if (request.getPath().contains("/api/flight")) {
			return dispatchFlight(request)
		}

		// Cars API
		if (request.getPath().contains("/m/api/cars")) {
			return dispatchCar(request)
		}

		// LX API
		if (request.getPath().contains("/lx/api") || request.getPath().contains("m/api/lx")) {
			return dispatchLX(request)
		}

		// AbacusV2 API
		if (request.getPath().contains("/api/bucketing/v1/evaluateExperiments")) {
			return makeResponse("/api/bucketing/happy.json")
		}

		// AbacusV2 API
		if (request.getPath().contains("/api/bucketing/v1/logExperiments")) {
			return makeEmptyResponse()
		}

		// Trips API
		if (request.getPath().startsWith("/api/trips")) {
			return dispatchTrip(request)
		}

		// Expedia Suggest
		if (request.getPath().startsWith("/hint/es")) {
			return dispatchSuggest(request)
		}

		// User API
		if (request.getPath().contains("/api/user/sign-in")) {
			return dispatchSignIn(request)
		}

		// Omniture
		if (request.getPath().startsWith("/b/ss")) {
			return makeEmptyResponse()
		}

		// Static content like Mobiata image server
		if (request.getPath().startsWith("/static")) {
			return dispatchStaticContent(request)
		}

		// User Profile/Stored Traveler info
		if (request.getPath().startsWith("/api/user/profile")) {
			return dispatchUserProfile(request)
		}

		// Travel Ad Impression
		if (request.getPath().startsWith("/TravelAdsService/v3/Hotels/TravelAdImpression")) {
			return dispatchTravelAd("/TravelAdsService/v3/Hotels/TravelAdImpression")
		}

		// Travel Ad Click
		if (request.getPath().startsWith("/TravelAdsService/v3/Hotels/TravelAdClick")) {
			return dispatchTravelAd("/TravelAdsService/v3/Hotels/TravelAdClick")
		}

		// Travel Ad Beacon
		if (request.getPath().startsWith("/travel")) {
			return dispatchTravelAd("/travel")
		}

		// Travel Ad on Confirmation
		if (request.getPath().startsWith("/ads/hooklogic")) {
			return dispatchTravelAd("/ads/hooklogic")
		}

		return make404()
	}

	/////////////////////////////////////////////////////////////////////////////
	// Path dispatching

	public fun dispatchHotel(request: RecordedRequest): MockResponse {
		if (request.getPath().startsWith("/m/api/hotel/search")) {
			return makeResponse("m/api/hotel/search/happy.json")
		}
		else if (request.getPath().startsWith("/m/api/hotel/offers")) {
			val params = parseRequest(request)
			return makeResponse("m/api/hotel/offers/" + params.get("hotelId") + ".json", params)
		}
		else if (request.getPath().startsWith("/m/api/hotel/product")) {
			val params = parseRequest(request)
			if (params.get("productKey")!!.startsWith("hotel_coupon_errors")) {
				params.put("productKey", "hotel_coupon_errors")
			}
			return makeResponse("m/api/hotel/product/" + params.get("productKey") + ".json", params)
		}
		else if (request.getPath().startsWith("/m/api/hotel/trip/create")) {
			val params = parseRequest(request)
			var filename = "m/api/hotel/trip/create/" + params.get("productKey") + ".json"
			if (params.get("productKey")!!.startsWith("hotel_coupon_errors")) {
				filename = "m/api/hotel/trip/create/hotel_coupon_errors.json"
			}
			return makeResponse(filename, params)
		}
		else if (request.getPath().startsWith("/api/m/trip/coupon")) {
			val params = parseRequest(request)
			return makeResponse("api/m/trip/coupon/" + params.get("coupon.code") + ".json", params)
		}
		else if (request.getPath().startsWith("/m/api/hotel/trip/checkout")) {
			val params = parseRequest(request)
			var filename = "m/api/hotel/trip/checkout/" + params.get("tripId") + ".json"
			if (params.get("tripId")!!.startsWith("hotel_coupon_errors")) {
				filename = "m/api/hotel/trip/create/hotel_coupon_errors.json"
			}
			return makeResponse(filename, params)
		}
		return make404()
	}

	private fun dispatchFlight(request: RecordedRequest): MockResponse {
		if (request.getPath().startsWith("/api/flight/search")) {
			val params = parseRequest(request)
			var filename = "happy_oneway"

			val departCalTakeoff = parseYearMonthDay(params.get("departureDate"), 10, 0)
			val departCalLanding = parseYearMonthDay(params.get("departureDate"), 12 + 4, 0)
			params.put("departingFlightTakeoffTimeEpochSeconds", "" + (departCalTakeoff.getTimeInMillis() / 1000))
			params.put("departingFlightLandingTimeEpochSeconds", "" + (departCalLanding.getTimeInMillis() / 1000))

			if (params.containsKey("returnDate")) {
				filename = "happy_roundtrip"
				val returnCalTakeoff = parseYearMonthDay(params.get("returnDate"), 10, 0)
				val returnCalLanding = parseYearMonthDay(params.get("returnDate"), 12 + 4, 0)
				params.put("returnFlightTakeoffTimeEpochSeconds", "" + (returnCalTakeoff.getTimeInMillis() / 1000))
				params.put("returnFlightLandingTimeEpochSeconds", "" + (returnCalLanding.getTimeInMillis() / 1000))
			}

			params.put("tzOffsetSeconds", "" + (departCalTakeoff.getTimeZone().getOffset(departCalTakeoff.getTimeInMillis()) / 1000))

			return makeResponse("api/flight/search/" + filename + ".json", params)
		}
		else if (request.getPath().startsWith("/api/flight/trip/create")) {
			val params = parseRequest(request)
			return makeResponse("api/flight/trip/create/" + params.get("productKey") + ".json", params)
		}
		else if (request.getPath().startsWith("/api/flight/checkout")) {
			val params = parseRequest(request)

			if (params.get("tripId")!!.startsWith("air_attach_0")) {
				val c = Calendar.getInstance()
				c.setTime(Date())
				c.add(Calendar.DATE, 10)
				val millisFromEpoch = (c.getTimeInMillis() / 1000)
				val tzOffsetSeconds = (c.getTimeZone().getOffset(c.getTimeInMillis()) / 1000)

				params.put("airAttachEpochSeconds", "" + millisFromEpoch)
				params.put("airAttachTimeZoneOffsetSeconds", "" + tzOffsetSeconds)
			}
			return makeResponse("api/flight/checkout/" + params.get("tripId") + ".json", params)
		}
		return make404()
	}

	private fun dispatchTrip(request: RecordedRequest): MockResponse {
		val params = parseRequest(request)

		// Common to all trips
		val startOfTodayPacific = DateTime.now().withTimeAtStartOfDay().withZone(DateTimeZone.forOffsetHours(-7))
		val startOfTodayEastern = DateTime.now().withTimeAtStartOfDay().withZone(DateTimeZone.forOffsetHours(-4))
		val pacificDaylightTzOffset = -7 * 60 * 60.toLong()
		val easternDaylightTzOffset = -4 * 60 * 60.toLong()
		params.put("tzOffsetPacific", "" + pacificDaylightTzOffset)
		params.put("tzOffsetEastern", "" + easternDaylightTzOffset)

		// Inject hotel DateTimes
		val hotelCancellationEnd = startOfTodayPacific.plusDays(9).plusHours(11).plusMinutes(32)
		val hotelCheckIn = startOfTodayPacific.plusDays(10).plusHours(11).plusMinutes(32)
		val hotelCheckOut = startOfTodayPacific.plusDays(12).plusHours(18).plusMinutes(4)
		params.put("hotelCancellationEndEpochSeconds", "" + hotelCancellationEnd.getMillis() / 1000)
		params.put("hotelCheckInEpochSeconds", "" + hotelCheckIn.getMillis() / 1000)
		params.put("hotelCheckOutEpochSeconds", "" + hotelCheckOut.getMillis() / 1000)

		// Inject flight DateTimes
		val outboundFlightDeparture = startOfTodayPacific.plusDays(14).plusHours(11).plusMinutes(32)
		val outboundFlightArrival = startOfTodayEastern.plusDays(14).plusHours(18).plusMinutes(4)
		val inboundFlightDeparture = startOfTodayEastern.plusDays(22).plusHours(18).plusMinutes(59)
		val inboundFlightArrival = startOfTodayPacific.plusDays(22).plusHours(22).plusMinutes(11)
		params.put("outboundFlightDepartureEpochSeconds", "" + outboundFlightDeparture.getMillis() / 1000)
		params.put("outboundFlightArrivalEpochSeconds", "" + outboundFlightArrival.getMillis() / 1000)
		params.put("inboundFlightDepartureEpochSeconds", "" + inboundFlightDeparture.getMillis() / 1000)
		params.put("inboundFlightArrivalEpochSeconds", "" + inboundFlightArrival.getMillis() / 1000)

		// Inject air attach times
		params.put("airAttachOfferExpiresEpochSeconds", "" + startOfTodayPacific.plusDays(1).getMillis() / 1000);

		// Inject car DateTimes
		val carPickup = startOfTodayEastern.plusDays(14).plusHours(11).plusMinutes(32)
		val carDropoff = startOfTodayEastern.plusDays(22).plusHours(18).plusMinutes(29)
		params.put("carPickupEpochSeconds", "" + carPickup.getMillis() / 1000)
		params.put("carDropoffEpochSeconds", "" + carDropoff.getMillis() / 1000)

		// Inject lx DateTimes
		val lxStart = startOfTodayPacific.plusDays(25).plusHours(11)
		val lxEnd = startOfTodayPacific.plusDays(25).plusHours(17)
		params.put("lxStartEpochSeconds", "" + lxStart.getMillis() / 1000)
		params.put("lxEndEpochSeconds", "" + lxEnd.getMillis() / 1000)

		return makeResponse("/api/trips/happy.json", params)
	}

	private fun dispatchCar(request: RecordedRequest): MockResponse {
		if (request.getPath().contains("/search/airport")) {
			val params = parseRequest(request)
			val airportCode = params.get("airportCode")
			if ("KTM" == airportCode) {
				return makeResponse("m/api/cars/search/airport/ktm_no_product.json")
			}
			else if ("DTW" == airportCode) {
				return makeResponse("m/api/cars/search/airport/dtw_invalid_input.json")
			}
			else {
				return makeResponse("m/api/cars/search/airport/happy.json")
			}
		}
		else if (request.getPath().contains("/trip/create")) {
			val params = parseRequest(request)
			return makeResponse("m/api/cars/trip/create/" + params.get("productKey") + ".json", params)
		}
		else if (request.getPath().contains("/trip/checkout")) {
			val params = parseRequest(request)
			when (params.get("mainMobileTraveler.firstName")) {
				"AlreadyBooked"  -> return makeResponse("m/api/cars/trip/checkout/trip_already_booked.json")
				"PriceChange"    -> return makeResponse("m/api/cars/trip/checkout/price_change.json")
				"PaymentFailed"  -> return makeResponse("m/api/cars/trip/checkout/payment_failed.json")
				"UnknownError"   -> return makeResponse("m/api/cars/trip/checkout/unknown_error.json")
				"SessionTimeout" -> return makeResponse("m/api/cars/trip/checkout/session_timeout.json")
				"InvalidInput"   -> return makeResponse("m/api/cars/trip/checkout/invalid_input.json")
				else             -> return makeResponse("m/api/cars/trip/checkout/happy.json")
			}
		}
		return make404()
	}

	private fun dispatchSuggest(request: RecordedRequest): MockResponse {
		var type: String? = ""
		var latlong: String? = ""
		val params = parseRequest(request)
		if (params.containsKey("type")) {
			type = params.get("type")
		}
		if (params.containsKey("latlong")) {
			latlong = params.get("latlong")
		}

		if (request.getPath().startsWith("/hint/es/v2/ac/en_US")) {
			val requestPath = request.getPath()
			val filename = requestPath.substring(requestPath.lastIndexOf('/') + 1, requestPath.indexOf('?'))
			return makeResponse("hint/es/v2/ac/en_US/" + unUrlEscape(filename) + ".json")
		}
		else if (request.getPath().startsWith("/hint/es/v3/ac/en_US")) {
			if (type == "14") {
				return makeResponse("/hint/es/v3/ac/en_US/suggestion_city.json")
			}
			else {
				return makeResponse("/hint/es/v3/ac/en_US/suggestion.json")
			}
		}
		else if (request.getPath().startsWith("/hint/es/v1/nearby/en_US")) {
			if (latlong == "31.32|75.57") {
				return makeResponse("/hint/es/v1/nearby/en_US/suggestion_with_no_lx_activities.json")
			}
			else if (type == "14") {
				return makeResponse("/hint/es/v1/nearby/en_US/suggestion_city.json")
			}
			else {
				return makeResponse("/hint/es/v1/nearby/en_US/suggestion.json")
			}// City
		}
		return make404()
	}

	private fun dispatchSignIn(request: RecordedRequest): MockResponse {
		// TODO Handle the case when there's no email parameter in 2nd sign-in request
		val params = parseRequest(request)
		params.put("email", "qa-ehcc@mobiata.com")
		return makeResponse("api/user/sign-in/login.json", params)
	}

	private fun dispatchLX(request: RecordedRequest): MockResponse {
		if (request.getPath().startsWith("/lx/api/search")) {
			val params = parseRequest(request)
			val location = params.get("location")
			// Return happy path response if not testing for special cases.
			if (location == "search_failure") {
				return makeResponse("lx/api/search/" + location + ".json")
			}
			else {
				return makeResponse("lx/api/search/happy.json")
			}
		}
		else if (request.getPath().startsWith("/lx/api/activity")) {
			val params = parseRequest(request)
			val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
			val startDateTime = DateTime.now().withTimeAtStartOfDay()
            // supply the dates to the response
			params.put("startDate", startDateTime.toString(DATE_TIME_PATTERN))
			// Add availability dates for 13 days which should make the last date selector disabled.
			for (iPlusDays in 1..12) {
				params.put("startDatePlus" + iPlusDays, startDateTime.plusDays(iPlusDays).toString(DATE_TIME_PATTERN))
			}
			return makeResponse("lx/api/activity/happy.json", params)
		}
		else if (request.getPath().contains("/trip/create")) {
			val body = request.getUtf8Body()
			if (body.contains("error_activity_id")) {
				return makeResponse("m/api/lx/trip/create/error_create_trip.json")
			}
			if (body.contains("price_change")) {
				return makeResponse("m/api/lx/trip/create/price_change.json")
			}
			return makeResponse("m/api/lx/trip/create/happy.json")
		}
		else if (request.getPath().contains("/trip/checkout")) {
			val params = parseRequest(request)
			val firstName = params.get("firstName")
			val tripId = params.get("tripId")

			if (firstName != null) {
				when (firstName) {
					"AlreadyBooked"  -> return makeResponse("m/api/lx/trip/checkout/trip_already_booked.json")
					"PaymentFailed"  -> return makeResponse("m/api/lx/trip/checkout/payment_failed_trip_id.json")
					"UnknownError"   -> return makeResponse("m/api/lx/trip/checkout/unknown_error.json")
					"SessionTimeout" -> return makeResponse("m/api/lx/trip/checkout/session_timeout.json")
					"InvalidInput"   -> return makeResponse("m/api/lx/trip/checkout/invalid_input.json")
					"PriceChange"    -> return makeResponse("m/api/lx/trip/checkout/price_change.json")
				}
			}
			return makeResponse("m/api/lx/trip/checkout/" + tripId + ".json")
		}
		return make404()
	}

	private fun dispatchStaticContent(request: RecordedRequest): MockResponse {
		return makeResponse(request.getPath())
	}

	private fun dispatchUserProfile(request: RecordedRequest): MockResponse {
		val params = parseRequest(request)
		return makeResponse("api/user/profile/user_profile_" + params.get("tuid") + ".json")
	}

	private fun makeResponse(fileName: String, params: Map<String, String>? = null) : MockResponse {
		return makeResponse(fileName, params, fileOpener)
	}

	private fun dispatchTravelAd(endPoint: String): MockResponse {
		var count = 0;
		if (travelAdRequests.get(endPoint) != null) {
			count = travelAdRequests.get(endPoint);
		}
		travelAdRequests.put(endPoint, count + 1)
		return makeEmptyResponse();
	}

	public fun numOfTravelAdRequests(key: String) : Int {
		if (travelAdRequests.get(key) != null) {
			return travelAdRequests.get(key)
		}
		return 0
	}
}

