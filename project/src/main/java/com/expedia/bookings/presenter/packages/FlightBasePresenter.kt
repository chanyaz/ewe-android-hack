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
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaggageFeeInfoWidget
import com.expedia.bookings.widget.PackageFlightFilterWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaggageFeeInfoViewModel
import com.expedia.vm.FlightOverviewViewModel
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.FlightToolbarViewModel
import com.expedia.vm.PackageFlightFilterViewModel
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import kotlin.properties.Delegates

open class FlightBasePresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

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

    val baggageFeeinfo: BaggageFeeInfoWidget by lazy {
        var viewStub = findViewById(R.id.baggage_fee_stub) as ViewStub
        var baggageFeeView = viewStub.inflate() as BaggageFeeInfoWidget
        baggageFeeView.viewModel = BaggageFeeInfoViewModel()
        baggageFeeView
    }

    val filter: PackageFlightFilterWidget by lazy {
        val viewStub = findViewById(R.id.filter_stub) as ViewStub
        val filterView = viewStub.inflate() as PackageFlightFilterWidget
        filterView.viewModel = PackageFlightFilterViewModel()
        resultsPresenter.resultsViewModel.flightResultsObservable.subscribe {
            filterView.viewModel.flightResultsObservable.onNext(it)
        }
        filterView.viewModel.filterObservable.subscribe {
            resultsPresenter.listResultsObserver.onNext(it)
            super.back()
        }
        filterView
    }

    val resultsPresenter: PackageFlightResultsPresenter by lazy {
        var viewStub = findViewById(R.id.results_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageFlightResultsPresenter
        presenter.resultsViewModel = FlightResultsViewModel()
        presenter.flightSelectedSubject.subscribe(selectedFlightResults)
        presenter
    }

    val overViewPresenter: PackageFlightOverviewPresenter by lazy {
        var viewStub = findViewById(R.id.overview_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageFlightOverviewPresenter
        presenter.vm = FlightOverviewViewModel(context)
        presenter
    }

    var toolbarViewModel: FlightToolbarViewModel by notNullAndObservable { vm ->
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

    init {
        View.inflate(context, R.layout.package_flight_presenter, this)
        toolbarViewModel = FlightToolbarViewModel(context)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        toolbar.inflateMenu(R.menu.package_flights_menu)
        menuFilter = toolbar.findViewById(R.id.menu_filter) as ActionMenuItemView
        menuSearch = toolbar.findViewById(R.id.menu_search) as ActionMenuItemView
        menuFilter.setIcon(filterPlaceholderIcon)
        menuFilter.setOnClickListener { show(filter) }
        overViewPresenter.baggageFeeShowSubject.subscribe { url ->
            baggageFeeinfo.viewModel.baggageFeeURLObserver.onNext(url)
            show(baggageFeeinfo)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            var lp = resultsPresenter.layoutParams as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
            lp = overViewPresenter.layoutParams as FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }

        addTransition(overviewTransition)
        addTransition(listToFiltersTransition)
        addTransition(baggageFeeTransition)
        addDefaultTransition(defaultTransition)
        show(resultsPresenter)
    }


    private val defaultTransition = object : Presenter.DefaultTransition(PackageFlightResultsPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            overViewPresenter.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    private val overviewTransition = object : Presenter.Transition(PackageFlightOverviewPresenter::class.java, PackageFlightResultsPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(forward)
            overViewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            resultsPresenter.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    private val baggageFeeTransition = object : Presenter.Transition(PackageFlightOverviewPresenter::class.java, BaggageFeeInfoWidget::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                toolbarViewModel.setTitleOnly.onNext(context.getString(R.string.package_flight_overview_baggage_fees))
            }
            else {
                toolbarViewModel.refreshToolBar.onNext(false)
            }
            overViewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            baggageFeeinfo.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    val listToFiltersTransition: Presenter.Transition = object : Presenter.Transition(PackageFlightResultsPresenter::class.java, PackageFlightFilterWidget::class.java, DecelerateInterpolator(2f), 500) {
        override fun startTransition(forward: Boolean) {
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val translatePercentage = if (forward) 1f - f else f
            filter.translationY = filter.height * translatePercentage
        }

        override fun endTransition(forward: Boolean) {
            if (forward) {
                toolbar.visibility = View.GONE
                filter.visibility = View.VISIBLE
                filter.translationY = 0f
            } else {
                toolbar.visibility = View.VISIBLE
                filter.visibility = View.GONE
                filter.translationY = (filter.height).toFloat()
            }
        }
    }

    val selectedFlightResults = object : Observer<FlightLeg> {
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

    override fun back(): Boolean {
        if (PackageFlightFilterWidget::class.java.name == currentState) {
            if (filter.viewModel.isFilteredToZeroResults()) {
                filter.dynamicFeedbackWidget.animateDynamicFeedbackWidget()
                return true
            } else {
                filter.viewModel.doneObservable.onNext(Unit)
            }
        }
        return super.back()
    }
}

