package com.expedia.vm

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.cars.CarSearchParamsBuilder
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.NavUtils
import com.mobiata.android.SocialUtils
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject

public class HotelConfirmationViewModel(checkoutResponseObservable: Observable<HotelCheckoutResponse>, context: Context) {

    // output
    val itineraryNumber = BehaviorSubject.create<String>()
    val itineraryNumberLabel = BehaviorSubject.create<String>()
    val formattedCheckInOutDate = BehaviorSubject.create<String>()
    val checkInDate = BehaviorSubject.create<LocalDate>()
    val checkOutDate = BehaviorSubject.create<LocalDate>()
    val bigImageUrl = BehaviorSubject.create<String>()
    val hotelName = BehaviorSubject.create<String>()
    val addressLineOne = BehaviorSubject.create<String>()
    val addressLineTwo = BehaviorSubject.create<String>()
    val hotelCity = BehaviorSubject.create<String>()
    val addCarBtnText = BehaviorSubject.create<String>()
    val addFlightBtnText = BehaviorSubject.create<String>()
    val customerEmail = BehaviorSubject.create<String>()
    val hotelLocation = BehaviorSubject.create<Location>()
    val showFlightCrossSell = BehaviorSubject.create<Boolean>()
    val showCarCrossSell = BehaviorSubject.create<Boolean>()

    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")

    init {
        checkoutResponseObservable.subscribe({hotelCheckoutResponse ->
            val product = hotelCheckoutResponse.checkoutResponse.productResponse
            val itinNumber = hotelCheckoutResponse.checkoutResponse.bookingResponse.itineraryNumber
            val checkInLocalDate = dtf.parseLocalDate(product.checkInDate)
            val checkOutLocalDate = dtf.parseLocalDate(product.checkOutDate)
            val location = Location()

            itineraryNumber.onNext(itinNumber)
            itineraryNumberLabel.onNext(context.getResources().getString(com.expedia.bookings.R.string.successful_checkout_TEMPLATE, itinNumber))
            checkInDate.onNext(checkInLocalDate)
            checkOutDate.onNext(checkOutLocalDate)
            formattedCheckInOutDate.onNext(DateFormatUtils.formatDateRange(context, checkInLocalDate, checkOutLocalDate, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH))
            bigImageUrl.onNext("https://media.expedia.com" + product.bigImageUrl)
            hotelName.onNext(product.localizedHotelName)
            addressLineOne.onNext(product.hotelAddress)
            addressLineTwo.onNext(context.getResources().getString(R.string.stay_summary_TEMPLATE, product.hotelCity, product.hotelStateProvince))
            hotelCity.onNext(product.hotelCity)
            addCarBtnText.onNext(context.getResources().getString(com.expedia.bookings.R.string.rent_a_car_TEMPLATE, product.hotelCity))
            addFlightBtnText.onNext(context.getResources().getString(com.expedia.bookings.R.string.flights_to_TEMPLATE, product.hotelCity))
            customerEmail.onNext(hotelCheckoutResponse.checkoutResponse.bookingResponse.email)

            // disabled for now. See mingle: #5574
//            val pointOfSale = PointOfSale.getPointOfSale(context)
//            val showHotelCrossSell = pointOfSale.showHotelCrossSell()
//            showFlightCrossSell.onNext(showHotelCrossSell && pointOfSale.supports(LineOfBusiness.FLIGHTS))
//            showCarCrossSell.onNext(showHotelCrossSell && pointOfSale.supports(LineOfBusiness.CARS))
            showFlightCrossSell.onNext(false)
            showCarCrossSell.onNext(false)

            location.setCity(product.hotelCity)
            location.setCountryCode(product.hotelCountry)
            location.setStateCode(product.hotelStateProvince)
            location.addStreetAddressLine(product.hotelAddress)
            hotelLocation.onNext(location)
            AdImpressionTracking.trackAdConversion(context, hotelCheckoutResponse.checkoutResponse.bookingResponse.tripId)
            HotelV2Tracking().trackHotelV2PurchaseConfirmation(hotelCheckoutResponse)
        })

    }

    fun getAddFlightBtnObserver(context: Context): Observer<Unit> {
        return object: Observer<Unit> {
            override fun onNext(t: Unit?) {
                val flightSearchParams = Db.getFlightSearch().getSearchParams()
                flightSearchParams.reset()
                val loc = Location()
                loc.setDestinationId(hotelLocation.getValue().toShortFormattedString())
                flightSearchParams.setArrivalLocation(loc)
                flightSearchParams.setDepartureDate(checkInDate.getValue())
                flightSearchParams.setReturnDate(checkOutDate.getValue())

                NavUtils.goToFlights(context, true)
                HotelV2Tracking().trackHotelV2CrossSellFlight()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }

    fun getAddCarBtnObserver(context: Context): Observer<Unit> {
        return object: Observer<Unit> {
            override fun onNext(t: Unit?) {
                val builder = CarSearchParamsBuilder()
                val dateTimeBuilder = CarSearchParamsBuilder.DateTimeBuilder().startDate(checkInDate.getValue()).endDate(checkOutDate.getValue())
                builder.origin(hotelLocation.getValue().toShortFormattedString())
                builder.originDescription(hotelLocation.getValue().toShortFormattedString())
                builder.dateTimeBuilder(dateTimeBuilder)
                val carSearchParams = builder.build()

                NavUtils.goToCars(context, null, carSearchParams, NavUtils.FLAG_OPEN_SEARCH)
                HotelV2Tracking().trackHotelV2CrossSellCar()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }

    fun getAddToCalendarBtnObserver(context: Context): Observer<Unit> {
        return object: Observer<Unit> {
            override fun onNext(t: Unit?) {
                // Go in reverse order, so that "check in" is shown to the user first
                context.startActivity(generateHotelCalendarIntent(false))
                context.startActivity(generateHotelCalendarIntent(true))
                HotelV2Tracking().trackHotelV2ConfirmationCalendar()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }

            private fun generateHotelCalendarIntent(checkIn: Boolean): Intent {
                val property = Property()
                property.setName(hotelName.getValue())
                property.setLocation(hotelLocation.getValue())
                val date = if (checkIn) checkInDate.getValue() else checkOutDate.getValue()

                return AddToCalendarUtils.generateHotelAddToCalendarIntent(context, property, date, checkIn, null, itineraryNumber.getValue())
            }
        }
    }

    fun getCallSupportBtnObserver(context: Context): Observer<Unit> {
        return object: Observer<Unit> {
            override fun onNext(t: Unit?) {
                val phoneNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser())
                SocialUtils.call(context, phoneNumber)
                HotelV2Tracking().trackHotelV2CallCustomerSupport()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }

    fun getDirectionsToHotelBtnObserver(context: Context): Observer<Unit> {
        return object: Observer<Unit> {
            override fun onNext(t: Unit?) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + hotelLocation.getValue().toLongFormattedString()))
                context.startActivity(intent)
                HotelV2Tracking().trackHotelV2ConfirmationDirection()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }
}
