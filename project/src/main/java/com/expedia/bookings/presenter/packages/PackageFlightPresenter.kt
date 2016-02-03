package com.expedia.bookings.presenter.packages

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightOverviewViewModel
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.FlightToolbarViewModel
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import kotlin.properties.Delegates

public class PackageFlightPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val ANIMATION_DURATION = 400
    val toolbar: Toolbar by bindView(R.id.flights_toolbar)
    var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
    var menuFilter: ActionMenuItemView by Delegates.notNull()
    var menuSearch: ActionMenuItemView by Delegates.notNull()
    val filterPlaceholderIcon by lazy {
        val sortDrawable = ContextCompat.getDrawable(context, R.drawable.sort).mutate()
        sortDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        sortDrawable
    }

    val resultsPresenter: PackageFlightResultsPresenter by lazy {
        var viewStub = findViewById(R.id.results_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageFlightResultsPresenter
        presenter.resultsViewModel = FlightResultsViewModel()
        presenter.flightSelectedSubject.subscribe(selectedFlightObserver)
        presenter
    }

    val overViewPresenter: PackageFlightOverviewPresenter by lazy {
        var viewStub = findViewById(R.id.overview_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageFlightOverviewPresenter
        presenter.vm = FlightOverviewViewModel(context)
        presenter
    }

    init {
        View.inflate(getContext(), R.layout.package_flight_presenter, this)
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.inflateMenu(R.menu.package_flights_menu)
        menuFilter = toolbar.findViewById(R.id.menu_filter) as ActionMenuItemView
        menuSearch = toolbar.findViewById(R.id.menu_search) as ActionMenuItemView
        menuFilter.setIcon(filterPlaceholderIcon)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbarViewModel = FlightToolbarViewModel(context)
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            var lp = resultsPresenter.layoutParams as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
            lp = overViewPresenter.layoutParams as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }

        addTransition(overviewTransition)
        addDefaultTransition(defaultTransition)
        show(resultsPresenter)
    }

    var toolbarViewModel : FlightToolbarViewModel by notNullAndObservable { vm ->
        vm.titleSubject.subscribe {
            toolbar.title = it
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }

        vm.menuVisibilitySubject.subscribe { showMenu ->
            menuSearch.visibility = if (showMenu) View.VISIBLE else View.GONE
            menuFilter.visibility = if (showMenu) View.VISIBLE else View.GONE
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(PackageFlightResultsPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            overViewPresenter.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    private val overviewTransition = object : Presenter.Transition(PackageFlightOverviewPresenter::class.java, PackageFlightResultsPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(forward)
            overViewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            resultsPresenter.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    val selectedFlightObserver = object : Observer<FlightLeg> {
        override fun onNext(flight: FlightLeg) {
            show(overViewPresenter)
            overViewPresenter.vm.selectedFlightLeg.onNext(flight)
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
            throw OnErrorNotImplementedException(e)
        }
    }

}
