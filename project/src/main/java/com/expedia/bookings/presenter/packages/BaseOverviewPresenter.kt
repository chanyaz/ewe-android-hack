package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.BundleOverviewHeader
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.PackagePaymentWidget
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseCheckoutViewModel

abstract class BaseOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {
    val ANIMATION_DURATION = 450L

    val bundleOverHeader: BundleOverviewHeader by bindView(R.id.coordinator_layout)
    protected val checkoutPresenter: BaseCheckoutPresenter by lazy { findViewById(R.id.checkout_presenter) as BaseCheckoutPresenter  }
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)

    open fun inflate() {

    }

    init {
        inflate()
        checkoutPresenter.viewModel = BaseCheckoutViewModel(context)
        checkoutPresenter.viewModel.lineOfBusiness.onNext(checkoutPresenter.lineOfBusiness())
        checkoutPresenter.paymentWidget.viewmodel.billingInfoAndStatusUpdate.map{it.first}.subscribe(checkoutPresenter.viewModel.paymentCompleted)
        bundleOverHeader.toolbar.inflateMenu(R.menu.menu_package_checkout)
        bundleOverHeader.toolbar.overflowIcon = ContextCompat.getDrawable(context, R.drawable.ic_create_white_24dp)
        bundleOverHeader.toolbar.viewModel.showChangePackageMenuObservable.subscribe { visible ->
            bundleOverHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, visible)
        }

        checkoutPresenter.checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }

        checkoutPresenter.paymentWidget.viewmodel.toolbarTitle.subscribe(bundleOverHeader.toolbar.viewModel.toolbarTitle)
        checkoutPresenter.paymentWidget.viewmodel.editText.subscribe(bundleOverHeader.toolbar.viewModel.editText)
        checkoutPresenter.paymentWidget.viewmodel.menuVisibility.subscribe(bundleOverHeader.toolbar.viewModel.menuVisibility)
        checkoutPresenter.paymentWidget.viewmodel.enableMenuItem.subscribe(bundleOverHeader.toolbar.viewModel.enableMenuItem)
        checkoutPresenter.paymentWidget.viewmodel.visibleMenuWithTitleDone.subscribe(bundleOverHeader.toolbar.viewModel.visibleMenuWithTitleDone)
        checkoutPresenter.paymentWidget.viewmodel.toolbarNavIcon.subscribe(bundleOverHeader.toolbar.viewModel.toolbarNavIcon)
        bundleOverHeader.toolbar.viewModel.doneClicked.subscribe {
            if (checkoutPresenter.currentState == PackagePaymentWidget::class.java.name) {
                checkoutPresenter.paymentWidget.viewmodel.doneClicked.onNext(Unit)
            }
        }

        checkoutPresenter.checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }

        bundleOverHeader.setUpCollapsingToolbar()
        checkoutPresenter.checkoutTranslationObserver.subscribe { y ->
            val distance = -bundleOverHeader.appBarLayout.totalScrollRange + (y / checkoutPresenter.height * bundleOverHeader.appBarLayout.totalScrollRange).toInt()
            val params = bundleOverHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            val enable = y != 0f
            bundleOverHeader.toggleCollapsingToolBar(enable)
            behavior.topAndBottomOffset = distance
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(checkoutTransition)
        addTransition(checkoutToCvv)
        show(BundleDefault())
        cvv.setCVVEntryListener(this)
        checkoutPresenter.slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)

        val checkoutPresenterLayoutParams = checkoutPresenter.layoutParams as ViewGroup.MarginLayoutParams
        checkoutPresenterLayoutParams.setMargins(0, toolbarHeight, 0, 0)
        checkoutPresenter.mainContent.visibility = View.GONE
    }

    val defaultTransition = object : Presenter.DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
            bundleOverHeader.toggleCollapsingToolBar(!forward)
        }
    }

    val checkoutTransition = object : Presenter.Transition(BundleDefault::class.java, getCheckoutTransitionClass()) {
        var translationDistance = 0f
        var headerOffset = 0

        override fun startTransition(forward: Boolean) {
            bundleOverHeader.toggleCollapsingToolBar(true)
            translationDistance = checkoutPresenter.mainContent.translationY
            val params = bundleOverHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            headerOffset = behavior.topAndBottomOffset
            checkoutPresenter.checkoutButton.translationY = if (forward) 0f else checkoutPresenter.checkoutButton.height.toFloat()
            checkoutPresenter.chevron.rotation = 0f
            bundleOverHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, !forward)
            bundleOverHeader.toolbar.alpha = if (forward) 0f else 1f
            checkoutPresenter.mainContent.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            translateHeader(f, forward)
            translateCheckout(f, forward)
            translateBottomContainer(f, forward)
            bundleOverHeader.toolbar.alpha = 1f * if (forward) f else (1 - f)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverHeader.toggleCollapsingToolBar(!forward)
            checkoutPresenter.checkoutButton.translationY = if (forward) checkoutPresenter.checkoutButton.height.toFloat() else 0f
            checkoutPresenter.mainContent.visibility = if (forward) View.VISIBLE else View.GONE
            checkoutPresenter.mainContent.translationY = 0f
            bundleOverHeader.toolbar.alpha = 1f
            bundleOverHeader.isDisabled = forward
        }

        private fun translateHeader(f: Float, forward: Boolean) {
            val params = bundleOverHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            val scrollY = if (forward) f * - bundleOverHeader.appBarLayout.totalScrollRange else (f - 1) * (bundleOverHeader.appBarLayout.totalScrollRange + headerOffset)
            behavior.topAndBottomOffset = scrollY.toInt()
        }

        private fun translateCheckout(f: Float, forward: Boolean) {
            var distance = height - translationDistance - Ui.getStatusBarHeight(context)
            checkoutPresenter.mainContent.translationY = if (forward) translationDistance + ((1 - f) * distance) else translationDistance + (f * distance)
        }

        private fun translateBottomContainer(f: Float, forward: Boolean) {
            val hasCompleteInfo = checkoutPresenter.viewModel.infoCompleted.value
            val bottomDistance = checkoutPresenter.sliderHeight - checkoutPresenter.checkoutButtonHeight
            var slideIn = if (hasCompleteInfo) bottomDistance - (f * (bottomDistance))
            else checkoutPresenter.sliderHeight - ((1 - f) * checkoutPresenter.checkoutButtonHeight)
            var slideOut = if (hasCompleteInfo) f * (bottomDistance)
            else checkoutPresenter.sliderHeight - (f * checkoutPresenter.checkoutButtonHeight)
            checkoutPresenter.bottomContainer.translationY = if (forward) slideIn else slideOut
            checkoutPresenter.checkoutButton.translationY = if (forward) f * checkoutPresenter.checkoutButtonHeight else (1 - f) * checkoutPresenter.checkoutButtonHeight
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, getCheckoutTransitionClass(), CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                checkoutPresenter.slideToPurchase.resetSlider()
            } else {
                cvv.visibility = View.VISIBLE
            }
        }
    }

    val checkoutSliderSlidObserver = endlessObserver<Unit> {
        val billingInfo = checkoutPresenter.paymentWidget.sectionBillingInfo.billingInfo
        cvv.bind(billingInfo)
        show(cvv)
    }

    override fun onBook(cvv: String?) {
        checkoutPresenter.viewModel.cvvCompleted.onNext(cvv)
    }


    class BundleDefault

    abstract fun getCheckoutTransitionClass() : Class<out Any>
}