package com.expedia.bookings.itin.flight.details

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.common.ItinLinkOffCardViewViewModel
import com.expedia.bookings.itin.flight.manageBooking.FlightItinManageBookingActivity
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerInfoActivity
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.utils.Ui
import io.reactivex.subjects.PublishSubject

open class FlightItinBookingInfoViewModel(private val context: Context, private val itinId: String, private val tripId: String = "") {
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
                buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, url, null, tripId)
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
                buildWebViewIntent(R.string.itin_hotel_details_price_summary_heading, url, "price-summary", tripId)
        ))
    }

    val additionalInfoCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val travelerInfoCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val manageBookingCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()
    val priceSummaryCardViewWidgetVM = FlightItinLinkOffCardViewViewModel()

    val widgetSharedSubject: PublishSubject<Boolean> = PublishSubject.create<Boolean>()
    var readJsonUtil: IJsonToItinUtil = Ui.getApplication(context).tripComponent().jsonUtilProvider()

    @VisibleForTesting
    fun buildWebViewIntent(title: Int, url: String?, anchor: String?, tripNumber: String?): Intent? {
        if (url != null) {
            val isGuest = readJsonUtil.getItin(tripId)?.isGuest
            return if (isGuest != null && isGuest) {
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            } else {
                val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
                if (anchor != null) builder.setUrlWithAnchor(url, anchor) else builder.setUrl(url)
                builder.setTitle(title)
                if (!tripNumber.isNullOrEmpty()) {
                    builder.setItinTripIdForRefresh(tripNumber)
                }
                builder.setInjectExpediaCookies(true)
                builder.setAllowMobileRedirects(true)
                builder.intent
            }
        } else {
            return null
        }
    }
}
