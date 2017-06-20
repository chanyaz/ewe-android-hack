package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelItinDetailsResponse
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.CarDataUtils
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.LXDataUtils
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.NumberUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.mobiata.android.Log
import com.mobiata.android.SocialUtils
import com.mobiata.android.util.SettingUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.json.JSONObject
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.properties.Delegates

class HotelConfirmationViewModel(context: Context, isWebCheckout: Boolean = false) {

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
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val checkoutResponseObservable = PublishSubject.create<HotelCheckoutResponse>()
    val itinDetailsResponseObservable = PublishSubject.create<HotelItinDetailsResponse>()
    val hotelCheckoutResponseObservable = BehaviorSubject.create<HotelCheckoutResponse>()
    val percentagePaidWithPointsObservable = BehaviorSubject.create<Int>()
    val totalAppliedRewardCurrencyObservable = BehaviorSubject.create<String>()
    val couponCodeObservable = BehaviorSubject.create<String>()
    val hotelConfirmationDetailsSetObservable = BehaviorSubject.create<Boolean>()
    val hotelConfirmationUISetObservable = BehaviorSubject.create<Boolean>()

    val showAddToCalendar = BehaviorSubject.create<Boolean>()
    protected lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    protected lateinit var userStateManager: UserStateManager
        @Inject set

    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
    val userAccountRefresher: UserAccountRefresher = UserAccountRefresher(context, LineOfBusiness.HOTELS, null)

    init {
        Ui.getApplication(context).hotelComponent().inject(this)

        if (isWebCheckout) {
            setUpItinResponseSubscription(context)
        } else {
            setUpCheckoutResponseSubscription(context)
        }

    }

    private fun setUpCheckoutResponseSubscription(context: Context) {
        checkoutResponseObservable.withLatestFrom(paymentModel.paymentSplits, { checkoutResponse, paymentSplits ->
            object {
                val hotelCheckoutResponse = checkoutResponse
                val percentagePaidWithPoints = if (BigDecimal(checkoutResponse.totalCharges).equals(BigDecimal.ZERO)) 0 else NumberUtils.getPercentagePaidWithPointsForOmniture(paymentSplits.payingWithPoints.amount.amount, BigDecimal(checkoutResponse.totalCharges))
                val totalAppliedRewardCurrency = if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType) paymentSplits.payingWithPoints.points.toString() else paymentSplits.payingWithPoints.amount.amount.toString()
            }
        }).subscribe {
            val coupon = Db.getTripBucket().hotelV2.mHotelTripResponse.coupon
            hotelCheckoutResponseObservable.onNext(it.hotelCheckoutResponse)
            percentagePaidWithPointsObservable.onNext(it.percentagePaidWithPoints)
            totalAppliedRewardCurrencyObservable.onNext(it.totalAppliedRewardCurrency)
            couponCodeObservable.onNext(coupon?.code ?: "")
            hotelConfirmationDetailsSetObservable.onNext(true)
        }

        checkoutResponseObservable.subscribe {
            val product = it.checkoutResponse.productResponse
            val itinNumber = it.checkoutResponse.bookingResponse.itineraryNumber
            val tripId = it.checkoutResponse.bookingResponse.tripId
            val email = it.checkoutResponse.bookingResponse.email
            val checkInLocalDate = dtf.parseLocalDate(product.checkInDate)
            val checkOutLocalDate = dtf.parseLocalDate(product.checkOutDate)
            val location = Location()

            itineraryNumber.onNext(itinNumber)
            itineraryNumberLabel.onNext(context.resources.getString(R.string.successful_checkout_TEMPLATE, itinNumber))
            checkInDate.onNext(checkInLocalDate)
            checkOutDate.onNext(checkOutLocalDate)
            formattedCheckInOutDate.onNext(DateFormatUtils.formatDateRange(context, checkInLocalDate, checkOutLocalDate, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH))

            if (!Strings.isEmpty(product.bigImageUrl))
                bigImageUrl.onNext(BuildConfig.MEDIA_URL + product.bigImageUrl)
            else bigImageUrl.onNext("")
            hotelName.onNext(product.getHotelName())
            addressLineOne.onNext(product.hotelAddress)
            addressLineTwo.onNext(context.resources.getString(R.string.stay_summary_TEMPLATE, product.hotelCity, product.hotelStateProvince))
            hotelCity.onNext(product.hotelCity)
            addCarBtnText.onNext(context.resources.getString(R.string.rent_a_car_TEMPLATE, product.hotelCity))
            addFlightBtnText.onNext(context.resources.getString(R.string.flights_to_TEMPLATE, product.hotelCity))
            customerEmail.onNext(it.checkoutResponse.bookingResponse.email)

            // Adding the guest trip in itin
            if (!userStateManager.isUserAuthenticated()) {
                ItineraryManager.getInstance().addGuestTrip(it.checkoutResponse.bookingResponse.email, itinNumber)
            } else if (PointOfSale.getPointOfSale().isPwPEnabledForHotels || PointOfSale.getPointOfSale().isSWPEnabledForHotels) {
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

            // Show Add to Calendar only if sharing is supported.
            showAddToCalendar.onNext(ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare())

            location.city = product.hotelCity
            location.countryCode = product.hotelCountry
            location.stateCode = product.hotelStateProvince
            location.addStreetAddressLine(product.hotelAddress)
            hotelLocation.onNext(location)
            AdImpressionTracking.trackAdConversion(context, it.checkoutResponse.bookingResponse.tripId)

            // LX Cross sell
            val isUserBucketedForLXCrossSellTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppLXCrossSellOnHotelConfirmationTest)
                    && PointOfSale.getPointOfSale().supports(LineOfBusiness.LX)
            addLXBtn.onNext(if (isUserBucketedForLXCrossSellTest) context.resources.getString(R.string.add_lx_TEMPLATE, product.hotelCity) else "")

            SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)
            val user = com.expedia.bookings.data.Db.getUser()
                        if(user != null) {
                            val expediaUserId = user.expediaUserId;
                            val tuid = user.tuidString
                            val deviceId = com.google.firebase.iid.FirebaseInstanceId.getInstance().getToken() as String
                            sendTravelNotifications(expediaUserId,tuid,deviceId,email,tripId,itinNumber)
                        }
            hotelConfirmationUISetObservable.onNext(true)
        }
    }

    private fun setUpItinResponseSubscription(context: Context) {
        itinDetailsResponseObservable.subscribe { response ->
            val hotel = response.responseData.hotels[0]
            val checkInLocalDate = hotel.checkInDateTime!!.toLocalDate()
            val checkOutLocalDate = hotel.checkOutDateTime!!.toLocalDate()
            val itinNumber = response.responseData.tripNumber.toString()
            val tripId = response.responseData.tripId
            val hotelPropertyInfo = hotel.hotelPropertyInfo
            val photoThumbnailURL = hotelPropertyInfo.photoThumbnailURL
            val hotelAddress = hotelPropertyInfo.address
            checkInDate.onNext(checkInLocalDate)
            checkOutDate.onNext(checkOutLocalDate)
            formattedCheckInOutDate.onNext(DateFormatUtils.formatDateRange(context, checkInLocalDate, checkOutLocalDate, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH))

            val location = Location()

            itineraryNumber.onNext(itinNumber)
            itineraryNumberLabel.onNext(context.resources.getString(R.string.successful_checkout_TEMPLATE, itinNumber))

            if (!Strings.isEmpty(photoThumbnailURL))
                bigImageUrl.onNext(BuildConfig.MEDIA_URL + photoThumbnailURL)
            else bigImageUrl.onNext("")
            hotelName.onNext(hotelPropertyInfo.name)
            addressLineOne.onNext(hotelAddress.addressLine1)
            addressLineTwo.onNext(context.resources.getString(R.string.stay_summary_TEMPLATE, hotelAddress.city, hotelAddress.countrySubdivisionCode))
            hotelCity.onNext(hotelAddress.city)
            addCarBtnText.onNext(context.resources.getString(R.string.rent_a_car_TEMPLATE, hotelAddress.city))
            addFlightBtnText.onNext(context.resources.getString(R.string.flights_to_TEMPLATE, hotelAddress.city))
            val email = hotel.rooms[0].roomPreferences.primaryOccupant.email
            customerEmail.onNext(email)

            // Adding the guest trip in itin
            if (!userStateManager.isUserAuthenticated()) {
                ItineraryManager.getInstance().addGuestTrip(email, itinNumber)
            } else if (PointOfSale.getPointOfSale().isPwPEnabledForHotels || PointOfSale.getPointOfSale().isSWPEnabledForHotels) {
                userAccountRefresher.forceAccountRefresh()
            }
            showFlightCrossSell.onNext(false)
            showCarCrossSell.onNext(false)

            showAddToCalendar.onNext(ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare())

            location.city = hotelAddress.city
            location.countryCode = hotelAddress.countryName
            location.stateCode = hotelAddress.countrySubdivisionCode
            location.addStreetAddressLine(hotelAddress.addressLine1)
            hotelLocation.onNext(location)

            val isUserBucketedForLXCrossSellTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppLXCrossSellOnHotelConfirmationTest)
                    && PointOfSale.getPointOfSale().supports(LineOfBusiness.LX)
            addLXBtn.onNext(if (isUserBucketedForLXCrossSellTest) context.resources.getString(R.string.add_lx_TEMPLATE, hotelAddress.city) else "")

            HotelTracking.trackHotelPurchaseFromWebView(response)
            SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)
            val user = com.expedia.bookings.data.Db.getUser()
                        if(user != null) {
                            val expediaUserId = user.expediaUserId;
                            val tuid = user.tuidString
                            val deviceId = com.google.firebase.iid.FirebaseInstanceId.getInstance().getToken() as String
                            sendTravelNotifications(expediaUserId,tuid,deviceId,email,tripId,itinNumber)
                        }
        }
    }

    fun getAddLXBtnObserver(context: Context): Observer<Unit> {
        return object : Observer<Unit> {
            override fun onNext(t: Unit?) {
                NavUtils.goToActivities(context, null, LXDataUtils.fromHotelParams(context, checkInDate.value, checkOutDate.value, hotelLocation.value),
                        NavUtils.FLAG_OPEN_RESULTS)
                HotelTracking.trackHotelCrossSellLX()
                (context as Activity).finish()
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
                val flightSearchParams = Db.getFlightSearch().searchParams
                flightSearchParams.reset()
                val loc = Location()
                loc.destinationId = hotelLocation.value.toShortFormattedString()
                flightSearchParams.arrivalLocation = loc
                flightSearchParams.departureDate = checkInDate.value
                flightSearchParams.returnDate = checkOutDate.value

                NavUtils.goToFlights(context, true)
                HotelTracking.trackHotelCrossSellFlight()
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
                val originSuggestion = CarDataUtils.getSuggestionFromLocation(hotelLocation.value.toShortFormattedString(),
                        null, hotelLocation.value.toShortFormattedString())
                val carSearchParams = CarSearchParam.Builder().origin(originSuggestion)
                        .startDate(checkInDate.value).endDate(checkOutDate.value).build() as CarSearchParam
                NavUtils.goToCars(context, null, carSearchParams, NavUtils.FLAG_OPEN_SEARCH)
                HotelTracking.trackHotelCrossSellCar()
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
                HotelTracking.trackHotelConfirmationCalendar()
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
                HotelTracking.trackHotelCallCustomerSupport()
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
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + hotelLocation.value.toLongFormattedString()))
                context.startActivity(intent)
                HotelTracking.trackHotelConfirmationDirection()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }


    fun setSearchParams(params: HotelSearchParams) {
        hotelSearchParams = params
    }

    private fun sendTravelNotifications( expediaUserId: String, tuid: String, deviceId: String, email: String, tripId: String?, itinNumber: String?){
            val thread = Thread(Runnable {
                try {
                    val httpclient = DefaultHttpClient()
                    val httppost = HttpPost("http://DELC02NG1WGG3QC.sea.corp.expecn.com:8080/notification/bookingConfirmation")
                    httppost.addHeader("Accept", "application/json")
                    httppost.addHeader("Content-Type", "application/json")
                    val json = JSONObject()
                    json.put("expUserId", expediaUserId)
                    json.put("tuId", tuid)
                    json.put("emailAddress", email)
                    json.put("deviceId", deviceId)
                    json.put("tripId", tripId)
                    json.put("itinId", itinNumber)
                    val params = StringEntity(json.toString())
                    httppost.entity = params
                    //execute http post
                    val response = httpclient.execute(httppost)
                    Log.e(response.toString())
                    //Your code goes here
                } catch (e: Exception) {
                    Log.e("ERROR Occurred!!!")
                    e.printStackTrace()
                }
            })
            thread.start()
        }
}
