package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.squareup.phrase.Phrase
import javax.inject.Inject

class TravelerSummaryViewModel(context: Context) : BaseSummaryViewModel(context) {

    val isFeatureEnabledForTravelerInfoTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidCheckoutPaymentTravelerInfo)
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    override fun inject() {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    override fun getTitle(): String {
        val traveler = getFirstTraveler()
        if (traveler?.fullName.isNullOrEmpty()) {
            return resources.getString(if (isFeatureEnabledForTravelerInfoTest) R.string.enter_traveler_details
                                        else R.string.checkout_enter_traveler_details)
        } else {
            return traveler?.getFullNameBasedOnPos(context) ?: ""
        }
    }

    override fun getSubtitle(): String {
        var numberOfTravelers = Db.getTravelers().size
        if (numberOfTravelers > 1) {
            return Phrase.from(resources.getQuantityString(R.plurals.checkout_more_travelers_TEMPLATE, numberOfTravelers - 1))
                    .put("travelercount", numberOfTravelers - 1).format().toString()
        }

        var traveler = getFirstTraveler()
        if (traveler == null || travelerStatusObserver.value != TravelerCheckoutStatus.COMPLETE) {
            return if (isFeatureEnabledForTravelerInfoTest) "" else resources.getString(R.string.enter_traveler_details)
        } else {
            return traveler!!.birthDate!!.toString("MM/dd/yyyy")
        }
    }

    override fun isTravelerEmpty(traveler: Traveler?): Boolean {
        if (traveler != null) {
            return travelerValidator.isTravelerEmpty(traveler)
        }
        return false
    }
}