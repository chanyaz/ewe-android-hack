package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.bookings.widget.PackagePaymentWidget
import com.expedia.bookings.widget.packages.CheckoutOverviewHeader
import com.expedia.util.endlessObserver
import com.expedia.vm.CheckoutToolbarViewModel
import kotlin.properties.Delegates

class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {
    val ANIMATION_DURATION = 450L


    var statusBar: View? = null
    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val checkoutOverviewHeader: CheckoutOverviewHeader by bindView(R.id.checkout_overview_header)
    var toolbarHeight: Int by Delegates.notNull()
    val scrollViewTopPadding = resources.getDimensionPixelSize(R.dimen.package_bundle_scroll_view_padding)
    val bundleWidget: BundleWidget by bindView(R.id.bundle_widget)
    val checkoutPresenter: PackageCheckoutPresenter by bindView(R.id.checkout_presenter)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    init {
        View.inflate(context, R.layout.bundle_overview, this)

        toolbar.viewModel = CheckoutToolbarViewModel(context)

        checkoutPresenter.paymentWidget.viewmodel.toolbarTitle.subscribe(toolbar.viewModel.toolbarTitle)
        checkoutPresenter.paymentWidget.viewmodel.editText.subscribe(toolbar.viewModel.editText)
        checkoutPresenter.paymentWidget.viewmodel.enableMenu.subscribe(toolbar.viewModel.enableMenu)
        checkoutPresenter.paymentWidget.viewmodel.enableMenuDone.subscribe(toolbar.viewModel.enableMenuDone)
        toolbar.viewModel.doneClicked.subscribe {
            if (checkoutPresenter.currentState == PackagePaymentWidget::class.java.name) {
                checkoutPresenter.paymentWidget.viewmodel.doneClicked.onNext(Unit)
            }
        }
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.packages_primary_color)
            statusBar = Ui.setUpStatusBar(getContext(), toolbar, null, color)
            addView(statusBar)
        }
        val padding = Ui.getToolbarSize(context) + statusBarHeight
        checkoutPresenter.setPadding(0, padding, 0, 0)

        bundleWidget.checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
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
        toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)
        bundleWidget.bundleContainer.setPadding(0, toolbarHeight, 0, 0)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(context, R.style.ToolbarSubtitleTextAppearance)
    }

    public fun hideCheckoutHeaderImage() {
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        statusBar?.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))

        toolbar.viewModel.toolbarTitle.onNext(resources.getString(R.string.Checkout))
        checkoutOverviewHeader.visibility = GONE
        bundleWidget.bundleContainer.setPadding(0, toolbarHeight, 0, 0)
    }

    public fun showCheckoutHeaderImage() {
        toolbar.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        statusBar?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        toolbar.viewModel.toolbarTitle.onNext("")
        toolbar.viewModel.toolbarSubtitle.onNext("")
        checkoutOverviewHeader.visibility = VISIBLE
        bundleWidget.bundleContainer.setPadding(0, scrollViewTopPadding, 0, 0)
    }

    val defaultTransition = object : Presenter.DefaultTransition(BundleDefault::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
        }
    }

    val checkoutTransition = object : Presenter.Transition(BundleDefault::class.java, PackageCheckoutPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            bundleWidget.visibility = View.VISIBLE
            checkoutPresenter.visibility = View.VISIBLE
            if (forward) {
                hideCheckoutHeaderImage()
            } else {
                showCheckoutHeaderImage()
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            bundleWidget.translationY = if (forward) f * -bundleWidget.height.toFloat() else (1 - f) * bundleWidget.height.toFloat()
            checkoutPresenter.translationY = if (forward) (f - 1) * -checkoutPresenter.height.toFloat() else f * checkoutPresenter.height.toFloat()
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, PackageCheckoutPresenter::class.java, CVVEntryWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
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

    override fun back(): Boolean {
        bundleWidget.bundleHotelWidget.backButtonPressed()
        hideCheckoutHeaderImage()
        return super.back()
    }

    class BundleDefault
}