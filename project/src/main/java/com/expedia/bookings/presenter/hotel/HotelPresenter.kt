package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelSearchViewModel
import rx.Observer

public class HotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val searchPresenter: HotelSearchPresenter by bindView(R.id.widget_hotel_params)
    val resultsPresenter: HotelResultsPresenter by bindView(R.id.widget_hotel_results)
    val detailPresenter: HotelDetailPresenter by bindView(R.id.widget_hotel_detail)

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()

        addDefaultTransition(defaultTransition)
        addTransition(searchToResults)
        addTransition(resultsToDetail)
        show(searchPresenter)

        searchPresenter.viewmodel = HotelSearchViewModel(getContext())

        searchPresenter.viewmodel.searchParamsObservable.subscribe(searchObserver)
        resultsPresenter.hotelSubject.subscribe(hotelSelectedObserver)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(javaClass<HotelSearchPresenter>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.GONE)
            detailPresenter.setVisibility(View.GONE)
        }
    }
    private val searchToResults = LeftToRightTransition(this, javaClass<HotelSearchPresenter>(), javaClass<HotelResultsPresenter>())

    private val resultsToDetail = LeftToRightTransition(this, javaClass<HotelResultsPresenter>(), javaClass<HotelDetailPresenter>())

    val searchObserver: Observer<HotelSearchParams> = endlessObserver { params ->
        resultsPresenter.doSearch(params)
        detailPresenter.setSearchParams(params)
        show(resultsPresenter)
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { selectedHotel ->
        detailPresenter.getDetail(selectedHotel)
        show(detailPresenter)
    }
}
