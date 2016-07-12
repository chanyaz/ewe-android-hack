package com.expedia.bookings.presenter.flight

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
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
import com.expedia.bookings.widget.BaseFlightFilterWidget
import com.expedia.bookings.widget.flights.PaymentFeeInfoWebView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightOverviewViewModel
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.FlightToolbarViewModel
import com.expedia.vm.WebViewViewModel
import com.expedia.vm.BaseFlightFilterViewModel
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException

abstract class BaseFlightPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {

    val ANIMATION_DURATION = 400
    val toolbar: Toolbar by bindView(R.id.flights_toolbar)
    var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
    val menuFilter: MenuItem by lazy {
        val menuFilter = toolbar.menu.findItem(R.id.menu_filter)
        menuFilter
    } // not used for flights LOB
    val menuSearch: MenuItem by lazy {
        val menuSearch = toolbar.menu.findItem(R.id.menu_search)
        menuSearch
    }

    val baggageFeeInfoWebView: BaggageFeeInfoWidget by lazy {
        val viewStub = findViewById(R.id.baggage_fee_stub) as ViewStub
        val baggageFeeView = viewStub.inflate() as BaggageFeeInfoWidget
        baggageFeeView.viewModel = WebViewViewModel()
        baggageFeeView
    }

    val paymentFeeInfoWebView: PaymentFeeInfoWebView by lazy {
        val viewStub = findViewById(R.id.payment_fee_info_stub) as ViewStub
        val paymentFeeInfoWidget = viewStub.inflate() as PaymentFeeInfoWebView
        paymentFeeInfoWidget.viewModel = WebViewViewModel()
        paymentFeeInfoWidget
    }

    val filter: BaseFlightFilterWidget by lazy {
        val viewStub = findViewById(R.id.filter_stub) as ViewStub
        val filterView = viewStub.inflate() as BaseFlightFilterWidget
        filterView.viewModelBase = BaseFlightFilterViewModel(context)
        resultsPresenter.resultsViewModel.flightResultsObservable.subscribe {
            filterView.viewModelBase.flightResultsObservable.onNext(it)
        }
        filterView.viewModelBase.filterObservable.subscribe {
            resultsPresenter.listResultsObserver.onNext(it)
            super.back()
        }
        filterView
    }

    val resultsPresenter: FlightResultsListViewPresenter by lazy {
        val viewStub = findViewById(R.id.results_stub) as ViewStub
        val presenter = viewStub.inflate() as FlightResultsListViewPresenter
        presenter.resultsViewModel = FlightResultsViewModel()
        toolbarViewModel.isOutboundSearch.subscribe(presenter.resultsViewModel.isOutboundResults)
        presenter.flightSelectedSubject.subscribe(selectedFlightResults)
        presenter.showSortAndFilterViewSubject.subscribe { show(filter) }
        presenter
    }

    val overviewPresenter: FlightOverviewPresenter by lazy {
        val viewStub = findViewById(R.id.overview_stub) as ViewStub
        val presenter = viewStub.inflate() as FlightOverviewPresenter
        presenter.vm = FlightOverviewViewModel(context, shouldShowBundlePrice())
        presenter
    }

    var toolbarViewModel: FlightToolbarViewModel by notNullAndObservable { vm ->
        vm.titleSubject.subscribe {
            toolbar.title = it
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }
    }

    init {
        View.inflate(context, R.layout.base_flight_presenter, this)
        setupToolbar()

        overviewPresenter.baggageFeeShowSubject.subscribe { url ->
            baggageFeeInfoWebView.viewModel.webViewURLObservable.onNext(url)
            trackShowBaggageFee()
            show(baggageFeeInfoWebView)
        }
        overviewPresenter.showPaymentFeesObservable.subscribe {
            trackShowPaymentFees()
            show(paymentFeeInfoWebView)
        }

        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
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

        addTransition(listToFiltersTransition)
        addTransition(baggageFeeTransition)
        addTransition(paymentFeeTransition)
        addResultOverViewTransition()
    }

    open fun addResultOverViewTransition() {
        addDefaultTransition(defaultTransition)
        addTransition(overviewTransition)
        show(resultsPresenter)
    }

    private val defaultTransition = object : DefaultTransition(FlightResultsListViewPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(forward)
            viewBundleSetVisibility(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            overviewPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            baggageFeeInfoWebView.visibility = View.GONE
            paymentFeeInfoWebView.visibility = View.GONE
            filter.visibility = View.GONE
        }
    }

    private val overviewTransition = object : ScaleTransition(this, FlightResultsListViewPresenter::class.java, FlightOverviewPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbarViewModel.refreshToolBar.onNext(!forward)
            viewBundleSetVisibility(!forward)
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
            viewBundleSetVisibility(false)
            overviewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            paymentFeeInfoWebView.visibility = View.GONE
            baggageFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    private val paymentFeeTransition = object : Transition(FlightOverviewPresenter::class.java, PaymentFeeInfoWebView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                toolbarViewModel.setTitleOnly.onNext(context.getString(R.string.flights_flight_overview_payment_fees))
            }
            else {
                toolbarViewModel.refreshToolBar.onNext(false)
            }
            overviewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            baggageFeeInfoWebView.visibility = View.GONE
            paymentFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    val listToFiltersTransition: Transition = object : Transition(FlightResultsListViewPresenter::class.java, BaseFlightFilterWidget::class.java, DecelerateInterpolator(2f), 500) {
        override fun startTransition(forward: Boolean) {
            resultsPresenter.recyclerView.visibility = View.VISIBLE
            toolbar.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val translatePercentage = if (forward) 1f - f else f
            filter.translationY = filter.height * translatePercentage
        }

        override fun endTransition(forward: Boolean) {
            viewBundleSetVisibility(!forward)
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
            resultsPresenter.recyclerView.visibility = if(forward) View.GONE else View.VISIBLE
        }
    }

    val selectedFlightResults = object : Observer<FlightLeg> {
        override fun onNext(flight: FlightLeg) {
            show(overviewPresenter)
            overviewPresenter.vm.selectedFlightLegSubject.onNext(flight)
            trackFlightOverviewLoad()
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
            throw OnErrorNotImplementedException(e)
        }
    }

    override fun back(): Boolean {
        if (BaseFlightFilterWidget::class.java.name == currentState) {
            if (filter.viewModelBase.isFilteredToZeroResults()) {
                filter.dynamicFeedbackWidget.animateDynamicFeedbackWidget()
                return true
            } else {
                filter.viewModelBase.doneObservable.onNext(Unit)
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
    }

    abstract fun setupToolbarMenu()
    abstract fun shouldShowBundlePrice(): Boolean
    abstract fun isOutboundResultsPresenter(): Boolean
    abstract fun trackFlightResultsLoad()
    abstract fun trackFlightOverviewLoad()
    abstract fun trackFlightSortFilterLoad()
    abstract fun trackShowBaggageFee()
    abstract fun trackShowPaymentFees()
    abstract fun viewBundleSetVisibility(forward: Boolean)
}

