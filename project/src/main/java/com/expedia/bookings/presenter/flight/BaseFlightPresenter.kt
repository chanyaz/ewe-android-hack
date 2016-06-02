package com.expedia.bookings.presenter.flight

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
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.shared.FlightOverviewPresenter
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaggageFeeInfoWidget
import com.expedia.bookings.widget.FlightListAdapter
import com.expedia.bookings.widget.PackageFlightFilterWidget
import com.expedia.bookings.widget.flights.PaymentFeeInfoWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightOverviewViewModel
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.FlightToolbarViewModel
import com.expedia.vm.WebViewViewModel
import com.expedia.vm.packages.FlightFilterViewModel
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import kotlin.properties.Delegates

abstract class BaseFlightPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val ANIMATION_DURATION = 400
    val toolbar: Toolbar by bindView(R.id.flights_toolbar)
    var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
    var menuFilter: ActionMenuItemView? = null // not used for flights LOB
    var menuSearch: ActionMenuItemView by Delegates.notNull()
    var flightSearchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        val flightListAdapter = FlightListAdapter(context, resultsPresenter.flightSelectedSubject, vm)
        resultsPresenter.setAdapter(flightListAdapter)
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        vm.confirmedOutboundFlightSelection.subscribe(resultsPresenter.outboundFlightSelectedSubject)
        vm.flightOfferSelected.subscribe { overviewPresenter.paymentFeesMayApplyTextView.visibility = if (it.mayChargeOBFees) VISIBLE else GONE }
        vm.obFeeDetailsUrlObservable.subscribe(paymentFeeInfo.viewModel.webViewURLObservable)
    }

    val baggageFeeInfo: BaggageFeeInfoWidget by lazy {
        val viewStub = findViewById(R.id.baggage_fee_stub) as ViewStub
        val baggageFeeView = viewStub.inflate() as BaggageFeeInfoWidget
        baggageFeeView.viewModel = WebViewViewModel()
        baggageFeeView
    }

    val paymentFeeInfo: PaymentFeeInfoWidget by lazy {
        val viewStub = findViewById(R.id.payment_fee_info_stub) as ViewStub
        val paymentFeeInfoWidget = viewStub.inflate() as PaymentFeeInfoWidget
        paymentFeeInfoWidget.viewModel = WebViewViewModel()
        paymentFeeInfoWidget
    }

    val filter: PackageFlightFilterWidget by lazy {
        val viewStub = findViewById(R.id.filter_stub) as ViewStub
        val filterView = viewStub.inflate() as PackageFlightFilterWidget
        filterView.viewModel = FlightFilterViewModel(context)
        resultsPresenter.resultsViewModel.flightResultsObservable.subscribe {
            filterView.viewModel.flightResultsObservable.onNext(it)
        }
        filterView.viewModel.filterObservable.subscribe {
            resultsPresenter.listResultsObserver.onNext(it)
            super.back()
        }
        filterView
    }

    val resultsPresenter: FlightResultsListViewPresenter by lazy {
        var viewStub = findViewById(R.id.results_stub) as ViewStub
        var presenter = viewStub.inflate() as FlightResultsListViewPresenter
        presenter.resultsViewModel = FlightResultsViewModel()
        toolbarViewModel.isOutboundSearch.subscribe(presenter.resultsViewModel.isOutboundResults)
        presenter.flightSelectedSubject.subscribe(selectedFlightResults)
        presenter.showSortAndFilterViewSubject.subscribe { show(filter) }
        presenter
    }

    val overviewPresenter: FlightOverviewPresenter by lazy {
        var viewStub = findViewById(R.id.overview_stub) as ViewStub
        var presenter = viewStub.inflate() as FlightOverviewPresenter
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
            menuFilter?.visibility = if (showMenu) View.VISIBLE else View.GONE
        }
    }

    init {
        View.inflate(context, R.layout.package_flight_presenter, this)
        setupToolbar()
        overviewPresenter.baggageFeeShowSubject.subscribe { url ->
            baggageFeeInfo.viewModel.webViewURLObservable.onNext(url)
            trackShowBaggageFee()
            show(baggageFeeInfo)
        }
        overviewPresenter.showPaymentFeesObservable.subscribe {
            trackShowPaymentFees()
            show(paymentFeeInfo)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            var lp = resultsPresenter.layoutParams as LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
            lp = overviewPresenter.layoutParams as LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }

        addTransition(overviewTransition)
        addTransition(listToFiltersTransition)
        addTransition(baggageFeeTransition)
        addTransition(paymentFeeTransition)
        addDefaultTransition(defaultTransition)
        show(resultsPresenter)
    }

    private val defaultTransition = object : DefaultTransition(FlightResultsListViewPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            overviewPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            baggageFeeInfo.visibility = View.GONE
            paymentFeeInfo.visibility = View.GONE
        }
    }

    private val overviewTransition = object : ScaleTransition(this, FlightResultsListViewPresenter::class.java, FlightOverviewPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(!forward)
            if (!forward) {
                trackFlightResultsLoad()
            }
        }
    }

    private val baggageFeeTransition = object : Transition(FlightOverviewPresenter::class.java, BaggageFeeInfoWidget::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                toolbarViewModel.setTitleOnly.onNext(context.getString(R.string.package_flight_overview_baggage_fees))
            }
            else {
                toolbarViewModel.refreshToolBar.onNext(false)
            }
            overviewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            paymentFeeInfo.visibility = View.GONE
            baggageFeeInfo.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    private val paymentFeeTransition = object : Transition(FlightOverviewPresenter::class.java, PaymentFeeInfoWidget::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                toolbarViewModel.setTitleOnly.onNext(context.getString(R.string.flights_flight_overview_payment_fees))
            }
            else {
                toolbarViewModel.refreshToolBar.onNext(false)
            }
            overviewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            baggageFeeInfo.visibility = View.GONE
            paymentFeeInfo.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    val listToFiltersTransition: Transition = object : Transition(FlightResultsListViewPresenter::class.java, PackageFlightFilterWidget::class.java, DecelerateInterpolator(2f), 500) {
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
                trackFlightSortFilterLoad()
            } else {
                toolbar.visibility = View.VISIBLE
                filter.visibility = View.GONE
                filter.translationY = (filter.height).toFloat()
            }
        }
    }

    val selectedFlightResults = object : Observer<FlightLeg> {
        override fun onNext(flight: FlightLeg) {
            show(overviewPresenter)
            overviewPresenter.vm.selectedFlightLeg.onNext(flight)
            trackFlightOverviewLoad()
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

    private fun setupToolbar() {
        toolbarViewModel = FlightToolbarViewModel(context)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        setupToolbarMenu()
        menuSearch = toolbar.findViewById(R.id.menu_search) as ActionMenuItemView
    }

    abstract fun setupToolbarMenu()
    abstract fun isOutboundResultsPresenter(): Boolean
    abstract fun trackFlightResultsLoad()
    abstract fun trackFlightOverviewLoad()
    abstract fun trackFlightSortFilterLoad()
    abstract fun trackShowBaggageFee()
    abstract fun trackShowPaymentFees()
}

