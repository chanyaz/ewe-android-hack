package com.expedia.util

import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager

object PackageUtil {

    val isPackageLOBUnderABTest: Boolean
        get() {
            val pointOfSaleId = PointOfSale.getPointOfSale().pointOfSaleId
            return pointOfSaleId == PointOfSaleId.SINGAPORE ||
                    pointOfSaleId == PointOfSaleId.MALAYSIA ||
                    pointOfSaleId == PointOfSaleId.AUSTRALIA ||
                    pointOfSaleId == PointOfSaleId.NEW_ZEALND ||
                    pointOfSaleId == PointOfSaleId.CANADA ||
                    pointOfSaleId == PointOfSaleId.TRAVELOCITY_CA ||
                    pointOfSaleId == PointOfSaleId.AIRASIAGO_MALAYSIA ||
                    pointOfSaleId == PointOfSaleId.AIRASIAGO_SINGAPORE ||
                    pointOfSaleId == PointOfSaleId.AIRASIAGO_AUSTRALIA ||
                    pointOfSaleId == PointOfSaleId.AIRASIAGO_JAPAN ||
                    pointOfSaleId == PointOfSaleId.WOTIF ||
                    pointOfSaleId == PointOfSaleId.WOTIF_NZ ||
                    pointOfSaleId == PointOfSaleId.LASTMINUTE ||
                    pointOfSaleId == PointOfSaleId.LASTMINUTE_NZ
        }

    val isPackagesLobTitleABTestEnabled: Boolean
        get() {
            val pointOfSaleId = PointOfSale.getPointOfSale().pointOfSaleId
            return pointOfSaleId == PointOfSaleId.UNITED_STATES ||
                    pointOfSaleId == PointOfSaleId.UNITED_KINGDOM
        }

    val packageTitle: Int
        get() {
            if (isPackagesLobTitleABTestEnabled) {
                if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppPackagesTitleChange)) {
                    val variateForTest = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppPackagesTitleChange)
                    if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal) {
                        return R.string.nav_hotel_plus_flight
                    } else {
                        return R.string.nav_hotel_plus_flight_deals
                    }
                } else {
                    return R.string.nav_packages
                }
            }

            val pointOfSaleId = PointOfSale.getPointOfSale().pointOfSaleId
            if (pointOfSaleId == PointOfSaleId.SINGAPORE ||
                    pointOfSaleId == PointOfSaleId.AIRASIAGO_SINGAPORE ||
                    pointOfSaleId == PointOfSaleId.AIRASIAGO_JAPAN ||
                    pointOfSaleId == PointOfSaleId.AIRASIAGO_MALAYSIA ||
                    pointOfSaleId == PointOfSaleId.MALAYSIA ||
                    pointOfSaleId == PointOfSaleId.JAPAN) {
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

            if (pointOfSaleId == PointOfSaleId.CANADA || pointOfSaleId == PointOfSaleId.TRAVELOCITY_CA) {
                return R.string.nav_flight_plus_hotel
            }

            return R.string.nav_packages
        }
}