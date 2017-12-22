package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.util.notNullAndObservable
import com.expedia.util.safeSubscribe
import com.expedia.vm.packages.PackageCreateTripViewModel
import com.squareup.phrase.Phrase

class PackageWebCheckoutViewViewModel(var context: Context) : WebCheckoutViewViewModel(context) {

    var multiItemCreateTripResponse: MultiItemApiCreateTripResponse? = null

    var packageCreateTripViewModel by notNullAndObservable<PackageCreateTripViewModel> {
        it.multiItemResponseSubject.safeSubscribe { multiItemResponse ->
            multiItemCreateTripResponse = multiItemResponse
        }
    }

    override fun doCreateTrip() {}

    fun loadURL() {
        if (multiItemCreateTripResponse != null) {
            webViewURLObservable.onNext(getMultiItemURL())
        }
    }

    private fun getMultiItemURL(): String {
        val pointOfSaleURL = PointOfSale.getPointOfSale().url
        val tripId = multiItemCreateTripResponse?.tripId
        return Phrase.from(context, R.string.multi_item_web_view_URL_TEMPLATE)
                .put("point_of_sale_url", pointOfSaleURL)
                .put("trip_id", tripId)
                .format()
                .toString()
    }
}
