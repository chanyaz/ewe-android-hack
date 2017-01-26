package com.expedia.bookings.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelItinDetailsResponse
import com.expedia.bookings.data.ItinDetailsResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import rx.Observer
import javax.inject.Inject

class ItinCardDetailsPresenter(context: Context, attributeSet: AttributeSet) : Presenter(context, attributeSet) {
    lateinit var tripServices: ItinTripServices
        @Inject set

    val tripDetailText: TextView by bindView(R.id.trip_id)

    init {
        Ui.getApplication(context).tripComponent().inject(this)
        View.inflate(context, R.layout.itin_card_details, this)
    }

    fun getTripId(tripId: String) {
        tripServices.getTripDetails(tripId, object : Observer<ItinDetailsResponse> {
            override fun onCompleted() {

            }

            override fun onNext(t: ItinDetailsResponse) {
                val isHotel = t is HotelItinDetailsResponse
                tripDetailText.text = t.responseData?.tripId + "isHotel:" + isHotel

            }

            override fun onError(e: Throwable?) {
                e?.printStackTrace()
            }

        })
    }

}
