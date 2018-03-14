package com.expedia.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager.Companion.isBucketedForVariant
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat

object PackageUtil {

    val isPackageLOBUnderABTest: Boolean
        get() {
            val pointOfSaleId = PointOfSale.getPointOfSale().pointOfSaleId
            return pointOfSaleId == PointOfSaleId.FRANCE ||
                    pointOfSaleId == PointOfSaleId.ITALY ||
                    pointOfSaleId == PointOfSaleId.SOUTH_KOREA ||
                    pointOfSaleId == PointOfSaleId.EBOOKERS_GERMANY
        }

    val isPackagesLobTitleABTestEnabled: Boolean
        get() {
            val pointOfSaleId = PointOfSale.getPointOfSale().pointOfSaleId
            return pointOfSaleId == PointOfSaleId.UNITED_STATES ||
                    pointOfSaleId == PointOfSaleId.UNITED_KINGDOM
        }

    fun packageTitle(context: Context): Int {
        if (isPackagesLobTitleABTestEnabled) {
            if (isBucketedForVariant(context, AbacusUtils.PackagesTitleChange, AbacusVariant.ONE)) {
                return R.string.nav_hotel_plus_flight
            } else if (isBucketedForVariant(context, AbacusUtils.PackagesTitleChange, AbacusVariant.TWO)) {
                return R.string.nav_hotel_plus_flight_deals
            }
            return R.string.nav_packages
        }

        val pointOfSaleId = PointOfSale.getPointOfSale().pointOfSaleId
        if (pointOfSaleId == PointOfSaleId.SINGAPORE ||
                pointOfSaleId == PointOfSaleId.MALAYSIA ||
                pointOfSaleId == PointOfSaleId.JAPAN ||
                pointOfSaleId == PointOfSaleId.HONG_KONG ||
                pointOfSaleId == PointOfSaleId.GERMANY ||
                pointOfSaleId == PointOfSaleId.THAILAND ||
                pointOfSaleId == PointOfSaleId.ITALY ||
                pointOfSaleId == PointOfSaleId.FRANCE ||
                pointOfSaleId == PointOfSaleId.SOUTH_KOREA ||
                pointOfSaleId == PointOfSaleId.AIRASIAGO_SINGAPORE ||
                pointOfSaleId == PointOfSaleId.AIRASIAGO_JAPAN ||
                pointOfSaleId == PointOfSaleId.AIRASIAGO_MALAYSIA ||
                pointOfSaleId == PointOfSaleId.AIRASIAGO_HONG_KONG ||
                pointOfSaleId == PointOfSaleId.AIRASIAGO_THAILAND ||
                pointOfSaleId == PointOfSaleId.EBOOKERS_GERMANY ) {
            return R.string.nav_hotel_plus_flight
        }

        if (pointOfSaleId == PointOfSaleId.AUSTRALIA ||
                pointOfSaleId == PointOfSaleId.AIRASIAGO_AUSTRALIA ||
                pointOfSaleId == PointOfSaleId.WOTIF ||
                pointOfSaleId == PointOfSaleId.LASTMINUTE ||
                pointOfSaleId == PointOfSaleId.NEW_ZEALND ||
                pointOfSaleId == PointOfSaleId.WOTIF_NZ ||
                pointOfSaleId == PointOfSaleId.LASTMINUTE_NZ) {
            return R.string.nav_hotel_plus_flight_deals
        }

        if (pointOfSaleId == PointOfSaleId.CANADA ||
                pointOfSaleId == PointOfSaleId.TRAVELOCITY_CA) {
            return R.string.nav_flight_plus_hotel
        }

        return R.string.nav_packages
    }

    fun getBundleHotelDatesAndGuestsText(context: Context, checkInDate: String, checkOutDate: String, guests: Int): String {
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")

        return Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(checkInDate)))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(checkOutDate)))
                .put("guests", StrUtils.formatGuestString(context, guests))
                .format()
                .toString()
    }
}
