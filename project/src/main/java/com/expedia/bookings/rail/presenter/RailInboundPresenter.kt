package com.expedia.bookings.rail.presenter

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.rail.widget.RailOutboundHeaderView
import com.expedia.bookings.rail.widget.RailResultsAdapter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.Optional
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailInboundResultsViewModel
import com.expedia.vm.rail.RailOutboundHeaderViewModel
import io.reactivex.subjects.PublishSubject
import kotlin.properties.Delegates

class RailInboundPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.rail_inbound_toolbar)
    val outboundHeaderView: RailOutboundHeaderView by bindView(R.id.outbound_header_view)
    val openReturnHeaderView: LinearLayout by bindView(R.id.open_return_selected_header)
    val recyclerView: RecyclerView by bindView(R.id.rail_inbound_list)
    val legalBanner: TextView by bindView(R.id.inbound_legal_banner)
    var adapter: RailResultsAdapter by Delegates.notNull()
    val legSelectedSubject = PublishSubject.create<RailLegOption>()
    val legalBannerClicked = PublishSubject.create<Unit>()

    var viewmodel: RailInboundResultsViewModel by notNullAndObservable { vm ->
        val outboundHeaderViewModel = RailOutboundHeaderViewModel(context)
        vm.outboundOfferSubject.map { Optional(it) }.subscribe(adapter.outboundOfferSubject)

        vm.legOptionsAndCheapestPriceSubject.subscribe { pair ->
            adapter.legOptionsAndCompareToPriceSubject.onNext(pair)
            outboundHeaderViewModel.cheapestLegPriceObservable.onNext(Optional(pair.second))
        }

        vm.outboundLegOptionSubject.subscribe(outboundHeaderViewModel.legOptionObservable)
        vm.outboundOfferSubject.map { Optional(it) }.subscribe(outboundHeaderViewModel.offerSubject)
        outboundHeaderView.setViewModel(outboundHeaderViewModel)

        vm.openReturnSubject.subscribeVisibility(openReturnHeaderView)

        vm.titleSubject.subscribe {
            toolbar.title = it
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }

        vm.directionHeaderSubject.subscribe(adapter.directionHeaderSubject)
        vm.priceHeaderSubject.subscribe(adapter.priceHeaderSubject)
    }

    init {
        View.inflate(context, R.layout.widget_rail_inbound_results, this)

        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        adapter = RailResultsAdapter(context, legSelectedSubject, true)
        recyclerView.adapter = adapter
        legalBanner.subscribeOnClick(legalBannerClicked)
    }
}
