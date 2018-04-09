package com.expedia.bookings.itin.flight.details

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.common.ItinLinkOffCardViewViewModel
import com.expedia.bookings.itin.flight.manageBooking.FlightItinManageBookingActivity
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerInfoActivity
import io.reactivex.subjects.PublishSubject

open class FlightItinBookingInfoViewModel(private val context: Context, private val itinId: String) {
    data class WidgetParams(
            val travelerNames: String,
            val isShared: Boolean,
            val url: String?,
            val cardId: String,
            val tripId: String?
    )

    fun updateBookingInfoWidget(widgetParams: WidgetParams) {
        val isSharedItin = widgetParams.isShared
        widgetSharedSubject.onNext(isSharedItin)
        if (!isSharedItin) {
            updateCardViewVMs(widgetParams.travelerNames, widgetParams.url, widgetParams.cardId, widgetParams.tripId)
        }
    }

    private fun updateCardViewVMs(travelerNames: CharSequence, url: String?, cardId: String, tripId: String?) {
        additionalInfoCardViewWidgetVM.updateCardView(params = ItinLinkOffCardViewViewModel.CardViewParams(
                context.getString(R.string.itin_hotel_details_additional_info_heading),
                null,
                false,
                R.drawable.ic_itin_additional_info_icon,
                buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, url, null, tripId)?.intent
        ))

        travelerInfoCardViewWidgetVM.updateCardView(params = ItinLinkOffCardViewViewModel.CardViewParams(
                context.getString(R.string.itin_flight_traveler_info),
                travelerNames,
                true,
                R.drawable.ic_traveler_icon,
                FlightItinTravelerInfoActivity.createIntent(context, cardId)
        ))

        manageBookingCardViewWidgetVM.updateCardView(params = ItinLinkOffCardViewViewModel.CardViewParams(
                context.getString(R.string.itin_hotel_manage_booking_header),
                context.getString(R.string.itin_hotel_details_manage_booking_subheading),
                false,
                R.drawable.ic_itin_manage_booking_icon,
                FlightItinManageBookingActivity.createIntent(context, itinId)
        ))

        priceSummaryCardViewWidgetVM.updateCardView(params = ItinLinkOffCardViewViewModel.CardViewParams(
                context.getString(R.string.itin_hotel_details_price_summary_heading),
                null,
                false,
                R.drawable.ic_itin_credit_card_icon,
                buildWebViewIntent(R.string.itin_hotel_details_price_summary_heading, url, "price-summary", tripId)?.intent
        ))
    }

    val additionalInfoCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val travelerInfoCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val manageBookingCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val priceSummaryCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()

    val widgetSharedSubject: PublishSubject<Boolean> = PublishSubject.create<Boolean>()

    @VisibleForTesting
    fun buildWebViewIntent(title: Int, url: String?, anchor: String?, tripId: String?): WebViewActivity.IntentBuilder? {
        if (url != null) {
            val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
            if (anchor != null) builder.setUrlWithAnchor(url, anchor) else builder.setUrl(url)
            builder.setTitle(title)
            if (!tripId.isNullOrEmpty()) {
                builder.setItinTripIdForRefresh(tripId)
            }
            builder.setInjectExpediaCookies(true)
            builder.setAllowMobileRedirects(true)
            return builder
        }
        return null
    }
}
