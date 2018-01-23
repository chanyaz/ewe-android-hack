package com.expedia.bookings.presenter

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.FrameLayout
import com.expedia.vm.CheckoutToolbarViewModel

abstract class BaseSingleScreenOverviewPresenter(context: Context, attrs: AttributeSet) : BaseOverviewPresenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    val summaryContainer: FrameLayout by bindView(R.id.summary_container)
    val bottomLayout: LinearLayout by bindView(R.id.bottom_container)
    val mainContent: LinearLayout by bindView(R.id.main_content)

    val statusBarHeight = Ui.getStatusBarHeight(context)
    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val toolbarBackground: View by bindView(R.id.toolbar_background)

    override fun onFinishInflate() {
        super.onFinishInflate()
        AnimUtils.slideDown(bottomLayout)
        setupToolbar()
        val mainContentParams = mainContent.layoutParams as MarginLayoutParams
        mainContentParams.setMargins(0, statusBarHeight, 0, 0)
    }

    private fun setupToolbar() {
        toolbar.viewModel = CheckoutToolbarViewModel(context)
        checkoutPresenter.paymentWidget.toolbarTitle.subscribe(toolbar.viewModel.toolbarTitle)
        checkoutPresenter.paymentWidget.viewmodel.enableMenuItem.subscribe(toolbar.viewModel.enableMenuItem)
        checkoutPresenter.paymentWidget.visibleMenuWithTitleDone.subscribe(toolbar.viewModel.visibleMenuWithTitleDone)
        checkoutPresenter.paymentWidget.toolbarNavIcon.subscribe(toolbar.viewModel.toolbarNavIcon)
        checkoutPresenter.paymentWidget.viewmodel.menuVisibility.subscribe(toolbar.viewModel.menuVisibility)

        checkoutPresenter.travelersPresenter.toolbarNavIconContDescSubject.subscribe(toolbar.viewModel.toolbarNavIconContentDesc)
        checkoutPresenter.travelersPresenter.travelerEntryWidget.focusedView.subscribe(toolbar.viewModel.currentFocus)
        checkoutPresenter.travelersPresenter.menuVisibility.subscribe(toolbar.viewModel.menuVisibility)
        checkoutPresenter.travelersPresenter.toolbarNavIcon.subscribe(toolbar.viewModel.toolbarNavIcon)
        checkoutPresenter.travelersPresenter.toolbarTitleSubject.subscribe(toolbar.viewModel.toolbarTitle)

        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        toolbarBackground.layoutParams.height = statusBarHeight
    }

    override val defaultTransition = object : DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            checkoutPresenter.adjustScrollingSpace(bottomLayout)
        }
    }
}
