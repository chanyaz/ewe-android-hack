package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import rx.Observer
import kotlin.properties.Delegates

public class HotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val searchPresenter: HotelSearchPresenter by Delegates.lazy {
        findViewById(R.id.widget_hotel_params) as HotelSearchPresenter
    }

    val resultsPresenter: HotelResultsPresenter by Delegates.lazy {
        findViewById(R.id.widget_hotel_results) as HotelResultsPresenter
    }

    override fun onFinishInflate() {
        super<Presenter>.onFinishInflate()
        addTransition(searchToResults)
        addDefaultTransition(defaultTransition)
        show(searchPresenter)
        searchPresenter.paramsSubject.subscribe(searchObserver)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(javaClass<HotelSearchPresenter>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.setVisibility(View.VISIBLE)
            resultsPresenter.setVisibility(View.GONE)
        }
    }
    private val searchToResults = LeftToRightTransition(this, javaClass<HotelSearchPresenter>(), javaClass<HotelResultsPresenter>())

    val searchObserver : Observer<HotelSearchParams> = object : Observer<HotelSearchParams> {

        override fun onNext(params: HotelSearchParams) {
            resultsPresenter.doSearch(params)
            show(resultsPresenter)
        }

        override fun onCompleted() {

        }

        override fun onError(e: Throwable?) {
        }
    }
}
