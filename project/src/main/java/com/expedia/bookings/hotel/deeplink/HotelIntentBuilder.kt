package com.expedia.bookings.hotel.deeplink

import android.content.Context
import android.content.Intent
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.DeprecatedHotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.DeprecatedHotelSearchParams.SearchType
import com.expedia.bookings.deeplink.HotelDeepLink
import com.expedia.bookings.utils.validation.CalendarRulesDateValidator
import com.expedia.bookings.hotel.util.HotelCalendarRules
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.ui.HotelActivity
import com.mobiata.android.Log

class HotelIntentBuilder {
    private val TAG = "HotelIntentBuilder"

    private var params: HotelSearchParams? = null
    private var landingPage: HotelLandingPage? = null

    private var fromDeepLink = false
    private var memberDealSearch = false

    fun from(context: Context, deepLink: HotelDeepLink, fromDeepLink: Boolean = true): HotelIntentBuilder {
        var isDatelessSearch = false

        val hotelSearchParams = DeprecatedHotelSearchParams()
        this.fromDeepLink = fromDeepLink

        if (deepLink.checkInDate != null) {
            hotelSearchParams.checkInDate = deepLink.checkInDate
        }
        if (deepLink.checkOutDate != null) {
            hotelSearchParams.checkOutDate = deepLink.checkOutDate
        }
        if (deepLink.numAdults != 0) {
            hotelSearchParams.numAdults = deepLink.numAdults
        }
        if (deepLink.children != null) {
            hotelSearchParams.children = deepLink.children
        }
        if (deepLink.mctc != null) {
            hotelSearchParams.mctc = deepLink.mctc!!
        }
        // Determine the search location.  Defaults to "current location" if none supplied
        // or the supplied variables could not be parsed.
        if (deepLink.hotelId != null) {
            hotelSearchParams.searchType = SearchType.HOTEL
            val hotelId = deepLink.hotelId
            hotelSearchParams.query = context.getString(R.string.search_hotel_id_TEMPLATE, hotelId)
            hotelSearchParams.hotelId = hotelId
            hotelSearchParams.regionId = hotelId

            val calendarRules = HotelCalendarRules(context)
            val dateValidator = CalendarRulesDateValidator(calendarRules, false)
            isDatelessSearch = !dateValidator.validateStartEndDate(deepLink.checkInDate, deepLink.checkOutDate)

            Log.d(TAG, "Setting hotel search id: " + hotelSearchParams.regionId)
        } else if (deepLink.regionId != null) {
            hotelSearchParams.searchType = SearchType.CITY
            hotelSearchParams.regionId = deepLink.regionId
            hotelSearchParams.setQuery("", false)

            Log.d(TAG, "Setting hotel search location: " + hotelSearchParams.regionId)
        } else if (deepLink.location != null) {
            hotelSearchParams.searchType = SearchType.CITY
            hotelSearchParams.query = deepLink.location

            Log.d(TAG, "Setting hotel search location: " + hotelSearchParams.query)
        }

        if (deepLink.sortType != null) {
            hotelSearchParams.sortType = deepLink.sortType
            Log.d(TAG, "Setting hotel sort type: " + hotelSearchParams.sortType)
        }

        if (deepLink.selectedHotelId != null) {
            hotelSearchParams.searchType = SearchType.HOTEL
            hotelSearchParams.hotelId = deepLink.selectedHotelId
            landingPage = HotelLandingPage.RESULTS
        }
        memberDealSearch = deepLink.memberOnlyDealSearch

        params = HotelsV2DataUtil.getHotelV2SearchParams(context, hotelSearchParams, isDatelessSearch)

        if (deepLink.shopWithPoints != null) {
            params?.shopWithPoints = deepLink.shopWithPoints!!
        }

        return this
    }

    fun build(context: Context): Intent {
        val intent = Intent()

        if (fromDeepLink) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(Codes.FROM_DEEPLINK, true)
        }

        intent.putExtra(HotelExtras.LANDING_PAGE, landingPage?.id)

        if (memberDealSearch) {
            intent.putExtra(Codes.DEALS, true)
        }

        if (params != null) {
            val gson = HotelsV2DataUtil.generateGson()
            intent.putExtra(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS, gson.toJson(params))
            intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
        }

        val routingTarget = HotelActivity::class.java
        intent.setClass(context, routingTarget)

        return intent
    }
}
