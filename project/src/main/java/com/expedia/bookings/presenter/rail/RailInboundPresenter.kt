package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.RailResultsAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailInboundResultsViewModel
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class RailInboundPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.rail_inbound_toolbar)
    val recyclerView: RecyclerView by bindView(R.id.rail_inbound_list)
    var adapter: RailResultsAdapter by Delegates.notNull()
    val legSelectedSubject = PublishSubject.create<RailLegOption>()

    var viewmodel: RailInboundResultsViewModel by notNullAndObservable { vm ->
        vm.legOptionListSubject.subscribe { legOptionList ->
            adapter.legOptionListSubject.onNext(legOptionList)
        }

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

        adapter = RailResultsAdapter(context, legSelectedSubject)
        recyclerView.adapter = adapter
    }

}
