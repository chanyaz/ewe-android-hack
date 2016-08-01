package com.expedia.bookings.presenter

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.presenter.packages.TravelerPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.BundleOverviewHeader
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.packages.PackagePaymentWidget
import com.expedia.util.endlessObserver

abstract class BaseOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {
    val ANIMATION_DURATION = 400

    val bundleOverviewHeader: BundleOverviewHeader by bindView(R.id.coordinator_layout)
    protected val checkoutPresenter: BaseCheckoutPresenter by lazy  { findViewById(R.id.checkout_presenter) as BaseCheckoutPresenter }
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)

    abstract fun inflate()

    init {
        inflate()
        checkoutPresenter.paymentWidget.viewmodel.billingInfoAndStatusUpdate.map{it.first}.subscribe(checkoutPresenter.getCheckoutViewModel().paymentCompleted)
        checkoutPresenter.getCreateTripViewModel().tripResponseObservable.subscribe { trip ->
            checkoutPresenter.getCheckoutViewModel().tripResponseObservable.onNext(trip)
            resetCheckoutState()
        }

        checkoutPresenter.getCheckoutViewModel().priceChangeObservable.subscribe {
            resetCheckoutState()
        }

        bundleOverviewHeader.toolbar.overflowIcon = ContextCompat.getDrawable(context, R.drawable.ic_create_white_24dp)
        bundleOverviewHeader.toolbar.viewModel.showChangePackageMenuObservable.subscribe { visible ->
            bundleOverviewHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, visible)
        }

        checkoutPresenter.paymentWidget.toolbarTitle.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        checkoutPresenter.paymentWidget.focusedView.subscribe(bundleOverviewHeader.toolbar.viewModel.currentFocus)
        checkoutPresenter.paymentWidget.filledIn.subscribe(bundleOverviewHeader.toolbar.viewModel.formFilledIn)
        checkoutPresenter.paymentWidget.menuVisibility.subscribe(bundleOverviewHeader.toolbar.viewModel.menuVisibility)
        checkoutPresenter.paymentWidget.enableMenuItem.subscribe(bundleOverviewHeader.toolbar.viewModel.enableMenuItem)
        checkoutPresenter.paymentWidget.visibleMenuWithTitleDone.subscribe(bundleOverviewHeader.toolbar.viewModel.visibleMenuWithTitleDone)
        checkoutPresenter.paymentWidget.toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)

        checkoutPresenter.travelerPresenter.toolbarTitleSubject.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        checkoutPresenter.travelerPresenter.travelerEntryWidget.focusedView.subscribe(bundleOverviewHeader.toolbar.viewModel.currentFocus)
        checkoutPresenter.travelerPresenter.travelerEntryWidget.filledIn.subscribe(bundleOverviewHeader.toolbar.viewModel.formFilledIn)
        checkoutPresenter.travelerPresenter.menuVisibility.subscribe(bundleOverviewHeader.toolbar.viewModel.menuVisibility)
        checkoutPresenter.travelerPresenter.toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)

        bundleOverviewHeader.toolbar.viewModel.doneClicked.subscribe {
            if (checkoutPresenter.currentState == PackagePaymentWidget::class.java.name) {
                checkoutPresenter.paymentWidget.doneClicked.onNext(Unit)
            } else if (checkoutPresenter.currentState == TravelerPresenter::class.java.name) {
                checkoutPresenter.travelerPresenter.doneClicked.onNext(Unit)
            }
        }

        checkoutPresenter.checkoutButton.setOnClickListener {
            showCheckout()
        }

        bundleOverviewHeader.setUpCollapsingToolbar()
        checkoutPresenter.checkoutTranslationObserver.subscribe { y ->
            val distance = -bundleOverviewHeader.appBarLayout.totalScrollRange + (y / checkoutPresenter.height * bundleOverviewHeader.appBarLayout.totalScrollRange).toInt()
            val params = bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            val enable = y != 0f
            bundleOverviewHeader.nestedScrollView.visibility = if (enable) VISIBLE else GONE
            bundleOverviewHeader.toggleCollapsingToolBar(enable)
            behavior.topAndBottomOffset = distance
            val range = Math.abs(distance)/bundleOverviewHeader.appBarLayout.totalScrollRange.toFloat()
            bundleOverviewHeader.nestedScrollView.foreground.alpha = (255 * range).toInt()
            translateBottomContainer(range, true)
        }
    }

    fun showCheckout() {
        resetCheckoutState()
        show(checkoutPresenter)
        checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        trackCheckoutPageLoad()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(checkoutTransition)
        addTransition(checkoutToCvv)
        show(BundleDefault())
        cvv.setCVVEntryListener(this)
        checkoutPresenter.slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)

        val checkoutPresenterLayoutParams = checkoutPresenter.layoutParams as MarginLayoutParams
        checkoutPresenterLayoutParams.setMargins(0, toolbarHeight, 0, 0)
        checkoutPresenter.mainContent.visibility = View.GONE
    }

    val defaultTransition = object : DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
            bundleOverviewHeader.toggleCollapsingToolBar(!forward)
            checkoutPresenter.toolbarDropShadow.visibility = View.GONE
        }
    }

    val checkoutTransition = object : Transition(BundleDefault::class.java, getCheckoutTransitionClass(), AccelerateDecelerateInterpolator(), ANIMATION_DURATION) {
        var translationDistance = 0f
        var range = 0f
        var userStoppedScrollingAt = 0
        override fun startTransition(forward: Boolean) {
            if (!forward) checkoutPresenter.toolbarDropShadow.visibility = View.GONE
            bundleOverviewHeader.nestedScrollView.visibility = VISIBLE
            toggleToolbar(forward)
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
            bundleOverviewHeader.toggleCollapsingToolBar(true)
            translationDistance = checkoutPresenter.mainContent.translationY
            val params = bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            range = if (forward) 0f else (bundleOverviewHeader.appBarLayout.totalScrollRange - Math.abs(behavior.topAndBottomOffset)) / bundleOverviewHeader.appBarLayout.totalScrollRange.toFloat()
            bundleOverviewHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, !forward)
            checkoutPresenter.mainContent.visibility = View.VISIBLE
            bundleOverviewHeader.nestedScrollView.foreground = ContextCompat.getDrawable(context, R.drawable.dim_background)
            behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return currentState == BundleDefault::class.java.name
                }
            });
            userStoppedScrollingAt = behavior.topAndBottomOffset
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val progress = Math.min(1f, range + f)
            translateHeader(progress, forward)
            translateCheckout(progress, forward)
            translateBottomContainer(progress, forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = if (forward) View.GONE else View.VISIBLE
            bundleOverviewHeader.toggleCollapsingToolBar(!forward)
            checkoutPresenter.toggleCheckoutButton(!forward)

            checkoutPresenter.mainContent.visibility = if (forward) View.VISIBLE else View.GONE
            checkoutPresenter.mainContent.translationY = 0f
            if (forward) checkoutPresenter.toolbarDropShadow.visibility = View.VISIBLE
            bundleOverviewHeader.isDisabled = forward
            bundleOverviewHeader.nestedScrollView.foreground.alpha = if (forward) 255 else 0
            checkoutPresenter.chevron.rotation = if (forward) 0f else 180f
            bundleOverviewHeader.nestedScrollView.visibility =  if (forward) GONE else VISIBLE
            if (!forward) {
                checkoutPresenter.trackShowBundleOverview()
            }
        }

        private fun translateHeader(f: Float, forward: Boolean) {
            val params = bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            val scrollY = if (forward) Math.min(userStoppedScrollingAt.toFloat(), (f * -bundleOverviewHeader.appBarLayout.totalScrollRange)) else (f - 1) * (bundleOverviewHeader.appBarLayout.totalScrollRange)
            behavior.topAndBottomOffset = scrollY.toInt()
        }

        private fun translateCheckout(f: Float, forward: Boolean) {
            var distance = height - translationDistance - Ui.getStatusBarHeight(context)
            checkoutPresenter.mainContent.translationY = if (forward) translationDistance + ((1 - f) * distance) else translationDistance + (f * distance)
            bundleOverviewHeader.nestedScrollView.foreground.alpha = (255 * if (forward) f else (1 - f)).toInt()
        }

    }

    private fun translateBottomContainer(f: Float, forward: Boolean) {
        val hasCompleteInfo = checkoutPresenter.getCheckoutViewModel().isValid()
        val bottomDistance = checkoutPresenter.sliderHeight - checkoutPresenter.checkoutButtonHeight
        val slideIn = if (hasCompleteInfo) {
            bottomDistance - (f * (bottomDistance))
        } else {
            checkoutPresenter.sliderHeight - ((1 - f) * checkoutPresenter.checkoutButtonHeight)
        }
        val slideOut = if (hasCompleteInfo) {
            f * (bottomDistance)
        } else {
            checkoutPresenter.sliderHeight - (f * checkoutPresenter.checkoutButtonHeight)
        }
        checkoutPresenter.bottomContainer.translationY = if (forward) slideIn else slideOut
        checkoutPresenter.checkoutButton.translationY = if (forward) f * checkoutPresenter.checkoutButtonHeight else (1 - f) * checkoutPresenter.checkoutButtonHeight
    }

    private fun resetCheckoutState() {
        checkoutPresenter.slideToPurchase.resetSlider()
        if (currentState == BundleDefault::class.java.name) {
            bundleOverviewHeader.toggleOverviewHeader(true)
            checkoutPresenter.toggleCheckoutButton(true)
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, getCheckoutTransitionClass(), CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                checkoutPresenter.slideToPurchase.resetSlider()
            } else {
                cvv.visibility = View.VISIBLE
                trackPaymentCIDLoad()
            }
        }
    }

    val checkoutSliderSlidObserver = endlessObserver<Unit> {
        val billingInfo = checkoutPresenter.paymentWidget.sectionBillingInfo.billingInfo
        cvv.bind(billingInfo)
        show(cvv)
    }

    override fun onBook(cvv: String?) {
        checkoutPresenter.getCheckoutViewModel().cvvCompleted.onNext(cvv)
    }


    class BundleDefault

    open fun toggleToolbar(forward: Boolean) { }
    abstract fun getCheckoutTransitionClass() : Class<out Any>
    abstract fun trackCheckoutPageLoad()
    abstract fun trackPaymentCIDLoad()
}