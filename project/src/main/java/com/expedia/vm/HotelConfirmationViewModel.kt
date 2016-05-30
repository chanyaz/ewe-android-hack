package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.cars.CarSearchParamsBuilder
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.NumberUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.LXDataUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.mobiata.android.SocialUtils
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import java.math.BigDecimal
import javax.inject.Inject

class HotelConfirmationViewModel(checkoutResponseObservable: Observable<HotelCheckoutResponse>, context: Context) {

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
    val addLXBtn = BehaviorSubject.create<String>()
    val customerEmail = BehaviorSubject.create<String>()
    val hotelLocation = BehaviorSubject.create<Location>()
    val showFlightCrossSell = BehaviorSubject.create<Boolean>()
    val showCarCrossSell = BehaviorSubject.create<Boolean>()

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
    val userAccountRefresher: UserAccountRefresher = UserAccountRefresher(context, LineOfBusiness.HOTELS, null)

    init {
        Ui.getApplication(context).hotelComponent().inject(this)

        checkoutResponseObservable.withLatestFrom(paymentModel.paymentSplits, { checkoutResponse, paymentSplits ->
            object {
                val hotelCheckoutResponse = checkoutResponse
                val percentagePaidWithPoints = if (BigDecimal(checkoutResponse.totalCharges).equals(BigDecimal.ZERO)) 0 else NumberUtils.getPercentagePaidWithPointsForOmniture(paymentSplits.payingWithPoints.amount.amount, BigDecimal(checkoutResponse.totalCharges))
                val totalAppliedRewardCurrency = if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType) paymentSplits.payingWithPoints.points.toString() else paymentSplits.payingWithPoints.amount.amount.toString()
            }
        } ).subscribe{
            val product = it.hotelCheckoutResponse.checkoutResponse.productResponse
            val itinNumber = it.hotelCheckoutResponse.checkoutResponse.bookingResponse.itineraryNumber
            val checkInLocalDate = dtf.parseLocalDate(product.checkInDate)
            val checkOutLocalDate = dtf.parseLocalDate(product.checkOutDate)
            val location = Location()

            itineraryNumber.onNext(itinNumber)
            itineraryNumberLabel.onNext(context.getResources().getString(com.expedia.bookings.R.string.successful_checkout_TEMPLATE, itinNumber))
            checkInDate.onNext(checkInLocalDate)
            checkOutDate.onNext(checkOutLocalDate)
            formattedCheckInOutDate.onNext(DateFormatUtils.formatDateRange(context, checkInLocalDate, checkOutLocalDate, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH))

            if (!Strings.isEmpty(product.bigImageUrl))
                bigImageUrl.onNext(BuildConfig.MEDIA_URL + product.bigImageUrl)
            else bigImageUrl.onNext("")
            hotelName.onNext(product.getHotelName())
            addressLineOne.onNext(product.hotelAddress)
            addressLineTwo.onNext(context.getResources().getString(R.string.stay_summary_TEMPLATE, product.hotelCity, product.hotelStateProvince))
            hotelCity.onNext(product.hotelCity)
            addCarBtnText.onNext(context.getResources().getString(com.expedia.bookings.R.string.rent_a_car_TEMPLATE, product.hotelCity))
            addFlightBtnText.onNext(context.getResources().getString(com.expedia.bookings.R.string.flights_to_TEMPLATE, product.hotelCity))
            customerEmail.onNext(it.hotelCheckoutResponse.checkoutResponse.bookingResponse.email)

            // Adding the guest trip in itin
            if (!User.isLoggedIn(context)) {
                ItineraryManager.getInstance().addGuestTrip(it.hotelCheckoutResponse.checkoutResponse.bookingResponse.email, itinNumber)
            }
            else if (PointOfSale.getPointOfSale().isPwPEnabledForHotels || PointOfSale.getPointOfSale().isSWPEnabledForHotels) {
                // If user is logged in and if PWP or SWP is enabled for hotels, refresh user.
                userAccountRefresher.forceAccountRefresh()
            }
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
            AdImpressionTracking.trackAdConversion(context, it.hotelCheckoutResponse.checkoutResponse.bookingResponse.tripId)
            HotelV2Tracking().trackHotelV2PurchaseConfirmation(it.hotelCheckoutResponse, it.percentagePaidWithPoints, it.totalAppliedRewardCurrency)

            // LX Cross sell
            val isUserBucketedForLXCrossSellTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppLXCrossSellOnHotelConfirmationTest)
                    && PointOfSale.getPointOfSale().supports(LineOfBusiness.LX)
            addLXBtn.onNext(if(isUserBucketedForLXCrossSellTest) context.getResources().getString(com.expedia.bookings.R.string.add_lx_TEMPLATE, product.hotelCity) else "")
        }

    }

    fun getAddLXBtnObserver(context: Context): Observer<Unit> {
        return object : Observer<Unit> {
            override fun onNext(t: Unit?) {
                NavUtils.goToActivities(context, null, LXDataUtils.fromHotelParams(context, checkInDate.value, hotelLocation.value),
                        NavUtils.FLAG_OPEN_RESULTS)
                HotelV2Tracking().trackHotelV2CrossSellLX()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }

    fun getAddFlightBtnObserver(context: Context): Observer<Unit> {
        return object : Observer<Unit> {
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
        return object : Observer<Unit> {
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
        return object : Observer<Unit> {
            override fun onNext(t: Unit?) {
                showAddToCalendarIntent(checkIn = true, context = context)
                HotelV2Tracking().trackHotelV2ConfirmationCalendar()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }

    fun showAddToCalendarIntent(checkIn: Boolean, context: Context) {
        fun generateHotelCalendarIntent(checkIn: Boolean): Intent {
            val property = Property()
            property.name = hotelName.value
            property.location = hotelLocation.value
            val date = if (checkIn) checkInDate.value else checkOutDate.value
            return AddToCalendarUtils.generateHotelAddToCalendarIntent(context, property, date, checkIn, null, itineraryNumber.value)
        }

        val requestCode = if (checkIn) AddToCalendarUtils.requestCodeAddCheckInToCalendarActivity else 0
        // #see HotelActivity.onActivityResult() fall callback action
        (context as Activity).startActivityForResult(generateHotelCalendarIntent(checkIn), requestCode, null)
    }

    fun getCallSupportBtnObserver(context: Context): Observer<Unit> {
        return object : Observer<Unit> {
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
        return object : Observer<Unit> {
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
