package com.expedia.bookings.itin.utils

import android.content.Context
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItinShareInfo.ItinSharable
import com.expedia.bookings.itin.widget.ShareItinDialog
import com.expedia.bookings.widget.itin.ItinContentGenerator
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver

class ShareTripHelper(val context: Context, val itinCardData: ItinCardData) {

    private val mItinContentGenerator = ItinContentGenerator.createGenerator(context, itinCardData)
    var shortenShareUtil = ShortenShareUrlUtils.getInstance(context)

    fun fetchShortShareUrlShowShareDialog() {
        var tripComponent = itinCardData.tripComponent as ItinSharable
        if (itinCardData is ItinCardDataFlight) {
            tripComponent = itinCardData.flightLeg
        }
        val trip = itinCardData.tripComponent.parentTrip

        if (tripComponent.sharingEnabled && tripComponent.shareInfo.hasShortSharableDetailsUrl()) {
            showNativeShareDialog()
        } else if (tripComponent.sharingEnabled && tripComponent.shareInfo.hasSharableDetailsUrl()) {
            shortenShareUtil.shortenSharableUrl(tripComponent.shareInfo.sharableDetailsUrl, getShortUrlResultObserver(tripComponent))
        } else if (trip.sharingEnabled && trip.shareInfo.hasShortSharableDetailsUrl()) {
            showNativeShareDialog()
        } else if (trip.sharingEnabled && trip.shareInfo.hasSharableDetailsUrl()) {
            shortenShareUtil.shortenSharableUrl(trip.shareInfo.sharableDetailsUrl, getShortUrlResultObserver(trip))
        }
    }

    private fun getShortUrlResultObserver(itinSharable: ItinSharable): Observer<String> {
        return object : DisposableObserver<String>() {
            override fun onError(e: Throwable) {
                //we have the full url anyway
                showNativeShareDialog()
            }

            override fun onNext(t: String) {
                if (!t.isEmpty()) {
                    itinSharable.shareInfo.shortSharableDetailsUrl = t
                }
                showNativeShareDialog()
            }

            override fun onComplete() {
            }
        }
    }

    private fun showNativeShareDialog() {
        val shareDialog = ShareItinDialog(context)
        shareDialog.showNativeShareDialog(mItinContentGenerator?.shareTextShort.toString(), mItinContentGenerator?.type.toString())
    }
}
