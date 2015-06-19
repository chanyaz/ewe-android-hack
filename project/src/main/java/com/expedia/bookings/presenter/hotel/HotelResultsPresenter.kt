package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelListAdapter
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.mobiata.android.Log
import rx.Observer
import rx.Subscription
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"

    var hotelServices : HotelServices? = null
    @Inject set

    var downloadSubscription: Subscription? = null

    val listView: RecyclerView by Delegates.lazy {
        findViewById(R.id.list_view) as RecyclerView
    }

    val toolbar: Toolbar by Delegates.lazy {
        findViewById(R.id.toolbar) as Toolbar
    }

    init {
        View.inflate(context, R.layout.widget_hotel_results, this)
    }

    override fun onFinishInflate() {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        listView.setLayoutManager(LinearLayoutManager(getContext()))
        listView.addItemDecoration(RecyclerDividerDecoration(getContext(), 10, 12, 10, 12, 0, 0, false))
        listView.setOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
        toolbar.setNavigationOnClickListener { view -> back() }
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
    }

    fun doSearch(params : HotelSearchParams) {
        downloadSubscription = hotelServices?.suggestHotels(params, downloadListener)
        toolbar.setTitle(params.city.shortName)
        var text = getResources().getString(R.string.calendar_instructions_date_range_with_guests_TEMPLATE, DateUtils.localDateToMMMd(params.checkIn), DateUtils.localDateToMMMd(params.checkOut), params.children.size() + 1)
        toolbar.setSubtitle(text)
    }


    val downloadListener : Observer<List<Hotel>> = object : Observer<List<Hotel>> {
        override fun onNext(hotels: List<Hotel>) {
            listView.setAdapter(HotelListAdapter(hotels))
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