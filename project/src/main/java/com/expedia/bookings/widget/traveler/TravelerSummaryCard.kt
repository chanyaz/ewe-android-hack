package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextColor
import com.expedia.vm.traveler.BaseSummaryViewModel
import com.squareup.phrase.Phrase

class TravelerSummaryCard(context: Context, attrs: AttributeSet?) : TravelerDetailsCard(context, attrs) {

    val isFeatureEnabledForTravelerInfoTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidCheckoutPaymentTravelerInfo)

    var viewModel: BaseSummaryViewModel by notNullAndObservable { vm ->

        vm.titleObservable.subscribeText(detailsText)
        if (isFeatureEnabledForTravelerInfoTest) {
            vm.subtitleObservable.subscribeTextAndVisibility(secondaryText)
        } else {
            vm.subtitleObservable.subscribeText(secondaryText)
        }
        vm.subtitleColorObservable.subscribeTextColor(secondaryText)
        vm.iconStatusObservable.subscribe {
            travelerStatusIcon.status = it
            setTravelerCardContentDescription(it, vm.titleObservable.value, vm.subtitleObservable.value)
        }
    }

    fun getStatus(): TravelerCheckoutStatus {
        return viewModel.travelerStatusObserver.value
    }

    fun setTravelerCardContentDescription(status: ContactDetailsCompletenessStatus, title: String, subtitle: String) {
        if (ContactDetailsCompletenessStatus.INCOMPLETE == status) {
            this.contentDescription = Phrase.from(context, R.string.traveler_details_incomplete_cont_desc_TEMPLATE).put("title", title).format().toString()
        } else if (ContactDetailsCompletenessStatus.COMPLETE == status) {
            this.contentDescription = Phrase.from(context, R.string.traveler_details_complete_cont_desc_TEMPLATE)
                    .put("title", title)
                    .put("subtitle", subtitle)
                    .format().toString()
        } else {
            AccessibilityUtil.appendRoleContDesc(this, title, R.string.accessibility_cont_desc_role_button)
        }
    }
}

