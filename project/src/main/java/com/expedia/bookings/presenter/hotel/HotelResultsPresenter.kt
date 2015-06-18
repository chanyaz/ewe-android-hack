package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Ui
import com.mobiata.android.Log
import rx.Observer
import rx.Subscription
import javax.inject.Inject

public class HotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    var hotelServices : HotelServices? = null
    @Inject set

    var downloadSubscription: Subscription? = null

    init {
        View.inflate(context, R.layout.widget_hotel_results, this)
    }

    override fun onFinishInflate() {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
    }

    fun doSearch(params : HotelSearchParams) {
        downloadSubscription = hotelServices?.suggestHotels(params, downloadListener)
    }

    val downloadListener : Observer<List<Hotel>> = object : Observer<List<Hotel>> {
        override fun onNext(t: List<Hotel>?) {
            Log.d("Hotel Results Next")
        }

        override fun onCompleted() {
            Log.d("Hotel Results Completed")
        }

        override fun onError(e: Throwable?) {
            Log.d("Hotel Results Error")
        }
    }
}