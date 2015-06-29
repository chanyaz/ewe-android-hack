package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.mobiata.android.Log
import com.squareup.phrase.Phrase
import rx.Observer
import rx.Subscription
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelDetailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    var hotelServices: HotelServices? = null
    @Inject set

    var downloadSubscription: Subscription? = null
    var hotelSearchParams : HotelSearchParams by Delegates.notNull()
    val toolbar: Toolbar by bindView(R.id.toolbar)

    init {
        View.inflate(context, R.layout.widget_hotel_detail, this)
    }

    override fun onFinishInflate() {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        toolbar.inflateMenu(R.menu.cars_search_menu)
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp))
        toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
        toolbar.setNavigationOnClickListener { view -> back() }
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
    }

    fun setSearchParams(params: HotelSearchParams) {
        hotelSearchParams = params
    }

    fun getDetail(params: Hotel) {
        downloadSubscription = hotelServices?.getHotelDetails(hotelSearchParams, params.hotelId, downloadListener)
        toolbar.setTitle(hotelSearchParams.city.regionNames.shortName)
        var text = Phrase.from(getContext(), R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(hotelSearchParams.checkIn)).put("enddate", DateUtils.localDateToMMMd(hotelSearchParams.checkOut)).put("guests", hotelSearchParams.children.size() + 1).format()
        toolbar.setSubtitle(text)
    }

    val downloadListener: Observer<HotelOffersResponse> = object : Observer<HotelOffersResponse> {
        override fun onNext(hotelOffersResponse: HotelOffersResponse) {
            Log.d("Hotel Detail Next")
        }

        override fun onCompleted() {
            Log.d("Hotel Detail Completed")
        }

        override fun onError(e: Throwable?) {
            Log.d("Hotel Detail Error")
        }
    }
}