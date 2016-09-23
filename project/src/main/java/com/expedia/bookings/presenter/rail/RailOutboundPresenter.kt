package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.RailResultsAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailOutboundResultsViewModel
import rx.Observer
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class RailOutboundPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.rail_outbound_toolbar)
    val recyclerView: RecyclerView by bindView(R.id.rail_outbound_list)
    var adapter: RailResultsAdapter by Delegates.notNull()

    var offerSelectedObserver: Observer<RailOffer>? = null
    val legSelectedSubject = PublishSubject.create<RailLegOption>()

    var viewmodel: RailOutboundResultsViewModel by notNullAndObservable { vm ->
        vm.railResultsObservable.subscribe {
            adapter.legOptionListSubject.onNext(it.legList[0].legOptionList)
        }

        vm.titleSubject.subscribe {
            toolbar.title = it
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }

        vm.paramsSubject.subscribe {
            adapter.showLoading()
        }

        vm.directionHeaderSubject.subscribe(adapter.directionHeaderSubject)
        vm.priceHeaderSubject.subscribe(adapter.priceHeaderSubject)
    }

    init {
        Ui.getApplication(context).railComponent().inject(this)
        View.inflate(context, R.layout.widget_rail_outbound_results, this)
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        adapter = RailResultsAdapter(context, legSelectedSubject)
        recyclerView.adapter = adapter
        adapter.showLoading()

        legSelectedSubject.subscribe {
            val offer = viewmodel.railResultsObservable.value.findOfferForLeg(it)
            offer.outboundLeg = it
            offerSelectedObserver?.onNext(offer)
        }
    }
}

