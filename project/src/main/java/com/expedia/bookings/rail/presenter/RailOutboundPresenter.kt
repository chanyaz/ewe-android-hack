package com.expedia.bookings.rail.presenter

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.rail.widget.RailResultsAdapter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.Optional
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.rail.RailOutboundResultsViewModel
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class RailOutboundPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.rail_outbound_toolbar)
    val childWarning: TextView by bindView(R.id.child_warning)
    val recyclerView: RecyclerView by bindView(R.id.rail_outbound_list)
    var adapter: RailResultsAdapter by Delegates.notNull()
    val legalBanner: TextView by bindView(R.id.outbound_legal_banner)

    val legSelectedSubject = PublishSubject.create<RailLegOption>()
    val legalBannerClicked = PublishSubject.create<Unit>()

    var viewmodel: RailOutboundResultsViewModel by notNullAndObservable { vm ->
        adapter.outboundOfferSubject.onNext(Optional(null))
        vm.legOptionsAndCheapestPriceSubject.subscribe(adapter.legOptionsAndCompareToPriceSubject)
        vm.showChildrenWarningObservable.subscribeVisibility(childWarning)

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
        vm.legalBannerMessageObservable.subscribeText(legalBanner)
    }

    init {
        View.inflate(context, R.layout.widget_rail_outbound_results, this)
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        adapter = RailResultsAdapter(context, legSelectedSubject, false)
        recyclerView.adapter = adapter
        adapter.showLoading()
        legalBanner.subscribeOnClick(legalBannerClicked)
    }
}

