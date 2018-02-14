package com.expedia.bookings.presenter.flight

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.shared.FlightOverviewPresenter
import com.expedia.bookings.presenter.shared.FlightResultsListViewPresenter
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.utils.setFocusForView
import com.expedia.bookings.widget.BaggageFeeInfoWebView
import com.expedia.bookings.widget.BaseFlightFilterWidget
import com.expedia.bookings.widget.flights.PaymentFeeInfoWebView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.BaseFlightFilterViewModel
import com.expedia.vm.BaseToolbarViewModel
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.WebViewViewModel
import io.reactivex.Observer
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.observers.DisposableObserver

abstract class BaseFlightPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {

    val ANIMATION_DURATION = 400
    val toolbar: Toolbar by bindView(R.id.flights_toolbar)
    val toolbarShadow: View by bindView(R.id.toolbar_dropshadow)
    var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)

    val menuFilter: MenuItem by lazy {
        val menuFilter = toolbar.menu.findItem(R.id.menu_filter)
        menuFilter
    } // not used for flights LOB
    val menuSearch: MenuItem by lazy {
        val menuSearch = toolbar.menu.findItem(R.id.menu_search)
        MenuItemCompat.setContentDescription(menuSearch, context.getString(R.string.search_button_cont_desc))
        menuSearch
    }

    val baggageFeeInfoWebView: BaggageFeeInfoWebView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.baggage_fee_stub)
        val baggageFeeView = viewStub.inflate() as BaggageFeeInfoWebView
        baggageFeeView.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        baggageFeeView.viewModel = WebViewViewModel()
        baggageFeeView
    }

    val paymentFeeInfoWebView: PaymentFeeInfoWebView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.payment_fee_info_stub)
        val paymentFeeInfoWidget = viewStub.inflate() as PaymentFeeInfoWebView
        paymentFeeInfoWidget.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        paymentFeeInfoWidget.viewModel = WebViewViewModel()
        paymentFeeInfoWidget
    }

    val filter: BaseFlightFilterWidget by lazy {
        val viewStub = findViewById<ViewStub>(R.id.filter_stub)
        val filterView = viewStub.inflate() as BaseFlightFilterWidget
        filterView.viewModelBase = BaseFlightFilterViewModel(context, getLineOfBusiness())
        resultsPresenter.resultsViewModel.flightResultsObservable.subscribe {
            filterView.viewModelBase.flightResultsObservable.onNext(it)
            filterView.viewModelBase.clearObservable.onNext(Unit)
        }
        filterView.viewModelBase.filterObservable.subscribe {
            resultsPresenter.listResultsObserver.onNext(it)
            super.back()
        }
        filterView.viewModelBase.filterCountObservable.subscribe(filterCountObserver)
        filterView
    }

    val resultsPresenter: FlightResultsListViewPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.results_stub)
        val presenter = viewStub.inflate() as FlightResultsListViewPresenter
        presenter.resultsViewModel = FlightResultsViewModel(context, getLineOfBusiness())
        toolbarViewModel.isOutboundSearch.subscribe(presenter.resultsViewModel.isOutboundResults)
        if (!AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppSimplifyFlightShopping) || getLineOfBusiness() == LineOfBusiness.PACKAGES) {
            presenter.flightSelectedSubject.subscribe(selectedFlightResults)
        }
        presenter.showSortAndFilterViewSubject.subscribe {
            show(filter)
            filter.viewModelBase.resetFilterTracking.onNext(Unit)
        }
        alignViewWithStatusBar(presenter)
        presenter
    }

    val overviewPresenter: FlightOverviewPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.overview_stub)
        val presenter = viewStub.inflate() as FlightOverviewPresenter
        presenter.vm = makeFlightOverviewModel()
        presenter.baggageFeeShowSubject.subscribe { url ->
            baggageFeeInfoWebView.viewModel.webViewURLObservable.onNext(url)
            trackShowBaggageFee()
            show(baggageFeeInfoWebView)
        }
        presenter.showPaymentFeesObservable.subscribe {
            if (it) {
                trackShowPaymentFees()
                show(paymentFeeInfoWebView)
            }
        }
        alignViewWithStatusBar(presenter)
        presenter
    }

    var toolbarViewModel: BaseToolbarViewModel by notNullAndObservable { vm ->
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

        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }
        toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_cont_desc)
        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) toolbarShadow.visibility = View.GONE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
        addTransition(listToFiltersTransition)
        addTransition(baggageFeeTransition)
        addTransition(paymentFeeTransition)
        addResultOverViewTransition()
    }

    open fun addResultOverViewTransition() {
        addDefaultTransition(defaultTransition)
        addTransition(overviewTransition)
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
            filter.visibility = View.INVISIBLE
            postDelayed({ AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar) }, 50L)
        }
    }

    open class OverviewTransition(override val presenter: BaseFlightPresenter) : ScaleTransition(presenter, FlightResultsListViewPresenter::class.java, FlightOverviewPresenter::class.java) {
        @CallSuper override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            presenter.toolbarViewModel.menuVisibilitySubject.onNext(false)
        }

        @CallSuper override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            presenter.toolbarViewModel.refreshToolBar.onNext(!forward)
            presenter.viewBundleSetVisibility(!forward)
            if (!forward) {
                presenter.trackFlightResultsLoad()
            }
            presenter.postDelayed({ AccessibilityUtil.setFocusToToolbarNavigationIcon(presenter.toolbar) }, 50L)
        }
    }

    open val overviewTransition = object : OverviewTransition(this) {}

    private val baggageFeeTransition = object : Transition(FlightOverviewPresenter::class.java, BaggageFeeInfoWebView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbar.visibility = if (forward) View.GONE else View.VISIBLE
            viewBundleSetVisibility(false)
            overviewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            paymentFeeInfoWebView.visibility = View.GONE
            baggageFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
            if (forward) {
                AccessibilityUtil.delayFocusToToolbarNavigationIcon(baggageFeeInfoWebView.toolbar, 50L)
            } else {
                AccessibilityUtil.delayFocusToToolbarNavigationIcon(toolbar, 50L)
            }
        }
    }

    private val paymentFeeTransition = object : Transition(FlightOverviewPresenter::class.java, PaymentFeeInfoWebView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbar.visibility = if (forward) View.GONE else View.VISIBLE
            overviewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            baggageFeeInfoWebView.visibility = View.GONE
            paymentFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
            AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
        }
    }

    private val listToFiltersTransition = object : Presenter.Transition(FlightResultsListViewPresenter::class.java, BaseFlightFilterWidget::class.java, DecelerateInterpolator(2f), 500) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            resultsPresenter.recyclerView.isEnabled = !forward
            filter.visibility = View.VISIBLE
            if (!forward) {
                resultsPresenter.visibility = VISIBLE
                toolbar.visibility = VISIBLE
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            val translatePercentage = if (forward) 1f - f else f
            filter.translationY = filter.height * translatePercentage
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            viewBundleSetVisibility(!forward)
            if (forward) {
                filter.visibility = View.VISIBLE
                filter.translationY = 0f
                trackFlightSortFilterLoad()
                resultsPresenter.visibility = GONE
                toolbar.visibility = GONE
                postDelayed({ filter.toolbar.setFocusForView() }, 50L)
            } else {
                filter.visibility = View.GONE
                filter.translationY = (filter.height).toFloat()
            }
        }
    }

    val selectedFlightResults = object : DisposableObserver<FlightLeg>() {
        override fun onNext(flight: FlightLeg) {
            show(overviewPresenter)
            overviewPresenter.vm.selectedFlightLegSubject.onNext(flight)
            trackFlightOverviewLoad()
        }

        override fun onComplete() {
        }

        override fun onError(e: Throwable) {
            throw OnErrorNotImplementedException(e)
        }
    }

    val filterCountObserver: Observer<Int> = endlessObserver {
        if (resultsPresenter.filterButton != null) {
            resultsPresenter.filterButton.showNumberOfFilters(it)
        }
    }

    fun showResults() {
        defaultTransition.endTransition(true)
    }

    fun backToOutboundResults() {
        back()
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
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        setupToolbarMenu()
    }

    private fun alignViewWithStatusBar(view: View) {
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            val lp = view.layoutParams as LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }
    }

    open fun disableSlidingWidget(isDisabled: Boolean) {
    }

    abstract fun makeFlightOverviewModel(): AbstractFlightOverviewViewModel
    abstract fun setupToolbarMenu()
    abstract fun getLineOfBusiness(): LineOfBusiness
    abstract fun isOutboundResultsPresenter(): Boolean
    abstract fun trackFlightResultsLoad()
    abstract fun trackFlightOverviewLoad()
    abstract fun trackFlightSortFilterLoad()
    abstract fun trackShowBaggageFee()
    abstract fun trackShowPaymentFees()
    abstract fun viewBundleSetVisibility(forward: Boolean)
}
