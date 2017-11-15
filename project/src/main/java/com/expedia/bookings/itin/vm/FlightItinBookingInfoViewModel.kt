package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.activity.FlightItinManageBookingActivity
import com.expedia.bookings.utils.FeatureToggleUtil
import rx.subjects.PublishSubject

open class FlightItinBookingInfoViewModel(private val context: Context, private val itinId: String) {
    data class WidgetParams(
            val travelerNames: String,
            val isShared: Boolean,
            val url: String?
    )

    fun updateBookingInfoWidget(widgetParams: WidgetParams) {
        widgetSharedSubject.onNext(widgetParams.isShared)
        updateCardViewVMs(widgetParams.travelerNames, widgetParams.url)

    }

    private fun updateCardViewVMs(travelerNames: CharSequence, url: String?) {
        additionalInfoCardViewWidgetVM.updateCardView(params = ItinLinkOffCardViewViewModel.CardViewParams(
                context.getString(R.string.itin_hotel_details_additional_info_heading),
                null,
                false,
                R.drawable.ic_itin_additional_info_icon,
                buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, url, null)?.intent
        ))

        travelerInfoCardViewWidgetVM.updateCardView(params = ItinLinkOffCardViewViewModel.CardViewParams(
                context.getString(R.string.itin_flight_traveller_info),
                travelerNames,
                true,
                R.drawable.ic_traveler_icon,
                null
        ))

        val webViewIntent = buildWebViewIntent(R.string.itin_flight_details_manage_booking_heading, url, "manage_reservation")?.intent
        val manageBookingIntent = FlightItinManageBookingActivity.createIntent(context, itinId)
        val intent = if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_trips_new_flights_managing_booking_design)) manageBookingIntent else webViewIntent
        manageBookingCardViewWidgetVM.updateCardView(params = ItinLinkOffCardViewViewModel.CardViewParams(
                context.getString(R.string.itin_hotel_manage_booking_header),
                context.getString(R.string.itin_hotel_details_manage_booking_subheading),
                false,
                R.drawable.ic_itin_manage_booking_icon,
                intent
        ))

        priceSummaryCardViewWidgetVM.updateCardView(params = ItinLinkOffCardViewViewModel.CardViewParams(
                context.getString(R.string.itin_hotel_details_price_summary_heading),
                null,
                false,
                R.drawable.ic_itin_credit_card_icon,
                buildWebViewIntent(R.string.itin_hotel_details_price_summary_heading, url, "price-summary")?.intent
        ))
    }

    val additionalInfoCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val travelerInfoCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val manageBookingCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val priceSummaryCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()


    val widgetSharedSubject: PublishSubject<Boolean> = PublishSubject.create<Boolean>()

    @VisibleForTesting
    fun buildWebViewIntent(title: Int, url: String?, anchor: String?): WebViewActivity.IntentBuilder? {
        if (url != null) {
            val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
            if (anchor != null) builder.setUrlWithAnchor(url, anchor) else builder.setUrl(url)
            builder.setTitle(title)
            builder.setInjectExpediaCookies(true)
            builder.setAllowMobileRedirects(true)
            return builder
        }
        return null
    }

}