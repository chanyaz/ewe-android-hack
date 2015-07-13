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
import com.expedia.vm.HotelSearchViewModel
import rx.Observer

public class HotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val searchPresenter: HotelSearchPresenter by bindView(R.id.widget_hotel_params)
    val resultsPresenter: HotelResultsPresenter by bindView(R.id.widget_hotel_results)
    val detailPresenter: HotelDetailPresenter by bindView(R.id.widget_hotel_detail)

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()

        searchPresenter.viewmodel = HotelSearchViewModel(getContext())

        addTransition(searchToResults)
        addTransition(resultsToDetail)
        addDefaultTransition(defaultTransition)
        show(searchPresenter)
        searchPresenter.viewmodel.searchParamsObservable.subscribe(searchObserver)
        resultsPresenter.hotelSubject.subscribe(detailObserver)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(javaClass<HotelSearchPresenter>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.GONE)
        }
    }
    private val searchToResults = LeftToRightTransition(this, javaClass<HotelSearchPresenter>(), javaClass<HotelResultsPresenter>())

    private val resultsToDetail = LeftToRightTransition(this, javaClass<HotelResultsPresenter>(), javaClass<HotelDetailPresenter>())

    val searchObserver : Observer<HotelSearchParams> = object : Observer<HotelSearchParams> {

        override fun onNext(params: HotelSearchParams) {
            resultsPresenter.doSearch(params)
            detailPresenter.setSearchParams(params)
            show(resultsPresenter)
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
        }
    }

    val detailObserver: Observer<Hotel> = object : Observer<Hotel> {

        override fun onNext(params: Hotel) {
            detailPresenter.getDetail(params)
            show(detailPresenter)
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
        }
    }
}
