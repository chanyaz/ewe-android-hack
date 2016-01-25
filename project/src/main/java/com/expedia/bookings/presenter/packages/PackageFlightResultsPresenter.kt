package com.expedia.bookings.presenter.packages

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PackageFlightListAdapter
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.FlightToolbarViewModel
import rx.subjects.PublishSubject
import kotlin.collections.filter
import kotlin.properties.Delegates

public class PackageFlightResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    var adapterPackage: PackageFlightListAdapter by Delegates.notNull()
    val flightSelectedSubject = PublishSubject.create<FlightLeg>()
    val toolbar: Toolbar by bindView(R.id.flight_results_toolbar)
    var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
        adapterPackage = PackageFlightListAdapter(flightSelectedSubject, context)
        recyclerView.adapter = adapterPackage
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
    }

    override fun onFinishInflate() {
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            var lp = recyclerView.layoutParams as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }
    }

    var resultsViewModel: FlightResultsViewModel by notNullAndObservable { vm ->
        vm.flightResultsObservable.subscribe(listResultsObserver)
    }

    var toolbarViewModel : FlightToolbarViewModel by notNullAndObservable { vm ->
        vm.titleSubject.subscribe {
            toolbar.title = it
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        adapterPackage.resultsSubject.onNext(it)
    }

}