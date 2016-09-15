package com.expedia.bookings.presenter.rail

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.RailResultsAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailResultsViewModel
import rx.Observer
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class RailResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    var offerSelectedObserver: Observer<RailOffer>? = null

    val legSelectedSubject = PublishSubject.create<RailLegOption>()

    var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)

    var viewmodel: RailResultsViewModel by notNullAndObservable { vm ->
        vm.railResultsObservable.subscribe {
            adapter.resultsSubject.onNext(it)
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

    val recyclerView: RecyclerView by bindView(R.id.list_view)
    var adapter: RailResultsAdapter by Delegates.notNull()
    val toolbar: Toolbar by bindView(R.id.toolbar)

    init {
        Ui.getApplication(context).railComponent().inject(this)
        View.inflate(context, R.layout.widget_rail_results, this)
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, recyclerView, color)
            addView(statusBar)
        }

        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon

        adapter = RailResultsAdapter(context, legSelectedSubject)
        recyclerView.adapter = adapter
        adapter.showLoading()

        legSelectedSubject.subscribe {
            val offer = adapter.resultsSubject.value.findOfferForLeg(it)
            offer.outboundLeg = it
            offerSelectedObserver?.onNext(offer)
        }
    }
}

