package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinBookingInfoViewModel
import com.expedia.bookings.itin.vm.ItinLinkOffCardViewViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import kotlinx.android.synthetic.main.widget_flight_itin_booking_details_widget.view.booking_info_container

class FlightItinBookingDetailsWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val manageBookingCard: ItinLinkOffCardView by bindView(R.id.itin_flight_manage_booking_card_view)
    val travelerInfoCard: ItinLinkOffCardView by bindView(R.id.itin_flight_traveler_info_card_view)
    val priceSummaryCard: ItinLinkOffCardView by bindView(R.id.itin_flight_price_summary_card_view)
    val additionalInfoCard: ItinLinkOffCardView by bindView(R.id.itin_flight_additional_info_card_view)

    init {
        View.inflate(context, R.layout.widget_flight_itin_booking_details_widget, this)
        this.orientation = LinearLayout.VERTICAL
    }

    private fun setManageBookingOnClick(intent: Intent?) = manageBookingCard.setOnClickListener {
        OmnitureTracking.trackItinFlightManageBooking()
        if (intent != null) context.startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
    }

    private fun setPriceOnClick(intent: Intent?) = priceSummaryCard.setOnClickListener {
        OmnitureTracking.trackItinFlightPriceSummary()
        if (intent != null) context.startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_partially, 0).toBundle())
    }

    private fun setAdditionalOnClick(intent: Intent?) = additionalInfoCard.setOnClickListener {
        OmnitureTracking.trackItinFlightAdditionalInfo()
        if (intent != null) context.startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_partially, 0).toBundle())
    }

    private fun setTravelerInfoOnClick(intent: Intent?) = travelerInfoCard.setOnClickListener {
        OmnitureTracking.trackItinFlightTravelerInfo()
        if (intent != null) context.startActivity(intent, ActivityOptionsCompat
                .makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete)
                .toBundle())
    }

    var viewModel: FlightItinBookingInfoViewModel by notNullAndObservable { vm ->
        vm.widgetSharedSubject.subscribeInverseVisibility(booking_info_container)

        vm.additionalInfoCardViewWidgetVM.cardViewParamsSubject.subscribe { params ->
            setUpCardView(params, additionalInfoCard)
            setAdditionalOnClick(params.intent)
        }
        vm.priceSummaryCardViewWidgetVM.cardViewParamsSubject.subscribe { params ->
            setUpCardView(params, priceSummaryCard)
            setPriceOnClick(params.intent)
        }
        vm.manageBookingCardViewWidgetVM.cardViewParamsSubject.subscribe { params ->
            setUpCardView(params, manageBookingCard)
            setManageBookingOnClick(params.intent)
        }
        vm.travelerInfoCardViewWidgetVM.cardViewParamsSubject.subscribe { params ->
            setUpCardView(params, travelerInfoCard)
            setTravelerInfoOnClick(params.intent)
        }
    }

    private fun setUpCardView(params: ItinLinkOffCardViewViewModel.CardViewParams, card: ItinLinkOffCardView) {
        card.setHeadingText(params.heading)
        AccessibilityUtil.appendRoleContDesc(card, params.heading.toString(), R.string.accessibility_cont_desc_role_button)
        if (params.subHeading != null) {
            card.setSubHeadingText(params.subHeading)
            if (params.wrapSubHeading) card.wrapSubHeading()
        } else {
            card.hideSubheading()
        }
        card.setIcon(params.iconId)
    }
}
