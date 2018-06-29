package com.expedia.bookings.launch.widget

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripUtils
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.itin.common.ItinLaunchScreenHelper
import com.expedia.bookings.utils.LaunchNavBucketCache
import com.expedia.bookings.utils.Ui
import java.util.ArrayList

open class LaunchListLogic {

    companion object {
        private var launchListLogic: LaunchListLogic? = null
        private lateinit var context: Context
        private lateinit var userStateManager: UserStateManager

        @JvmStatic
        @Synchronized
        fun getInstance(): LaunchListLogic {
            if (launchListLogic == null) {
                launchListLogic = LaunchListLogic()
            }
            return launchListLogic as LaunchListLogic
        }
    }

    fun initialize(appContext: Context) {
        context = appContext
        userStateManager = Ui.getApplication(context).appComponent().userStateManager()
    }

    open fun showItinCard(): Boolean {
        return ItinLaunchScreenHelper.showActiveItinLaunchScreenCard(userStateManager)
    }

    open fun showAirAttachMessage(): Boolean {
        return (userStateManager.isUserAuthenticated()
                && getUpcomingAirAttachQualifiedFlightTrip() != null
                && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.VIETNAM
                && PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.ARGENTINA)
    }

    open fun getUpcomingAirAttachQualifiedFlightTrip(): Trip? {
        return TripUtils.getUpcomingAirAttachQualifiedFlightTrip(getCustomerTrips())
    }

    open fun getCustomerTrips(): List<Trip> {
        return ArrayList(ItineraryManager.getInstance().trips)
    }

    fun showLaunchHeaderItem(): Boolean {
        return LaunchNavBucketCache.isBucketed(context)
    }

    fun showSignInCard(): Boolean {
        return !userStateManager.isUserAuthenticated()
    }

    fun showMemberDeal(): Boolean {
        return userStateManager.isUserAuthenticated()
    }

    fun showMesoHotelAd(): Boolean {
        if (AbacusFeatureConfigManager.isBucketedInAnyVariant(context, AbacusUtils.MesoAd)) {
            val variateForTest = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.MesoAd)
            return variateForTest == AbacusVariant.ONE.value
        }
        return false
    }

    fun showMesoDestinationAd(): Boolean {
        if (AbacusFeatureConfigManager.isBucketedInAnyVariant(context, AbacusUtils.MesoAd)) {
            val variateForTest = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.MesoAd)
            return variateForTest == AbacusVariant.TWO.value
        }
        return false
    }

    fun show2XBanner(): Boolean {
        if (!showSignInCard() && AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelEarn2xMessaging)) {
            return true
        }
        return false
    }

    fun showJoinRewardsCard(): Boolean {
        return PointOfSale.getPointOfSale().shouldShowJoinRewardsCard() && AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.JoinRewardsLaunchCard)
    }
}
