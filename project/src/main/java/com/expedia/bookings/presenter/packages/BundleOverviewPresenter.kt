package com.expedia.bookings.presenter.packages

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PackageBundlePriceWidget
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.bookings.widget.PackagePaymentWidget
import com.expedia.bookings.widget.PriceChangeWidget
import com.expedia.bookings.widget.packages.CheckoutOverviewHeader
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.endlessObserver
import com.expedia.vm.BundlePriceViewModel
import com.expedia.vm.CheckoutToolbarViewModel
import com.expedia.vm.PriceChangeViewModel

class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener, AppBarLayout.OnOffsetChangedListener {
    val ANIMATION_DURATION = 450L

    var statusBar: View? = null
    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)

    val collapsingToolbarLayout: CollapsingToolbarLayout by bindView(R.id.collapsing_toolbar)

    val coordinatorLayout: CoordinatorLayout by bindView(R.id.coordinator_layout)
    val appBarLayout: AppBarLayout by bindView(R.id.app_bar)
    val imageHeader: ImageView by bindView(R.id.overview_image)
    val checkoutOverviewHeaderToolbar: CheckoutOverviewHeader by bindView(R.id.checkout_overview_header_toolbar)
    val checkoutOverviewFloatingToolbar: CheckoutOverviewHeader by bindView(R.id.checkout_overview_floating_toolbar)
    val bundleWidget: BundleWidget by bindView(R.id.bundle_widget)
    val checkoutPresenter: PackageCheckoutPresenter by bindView(R.id.checkout_presenter)
    val priceChangeWidget: PriceChangeWidget by bindView(R.id.price_change)
    val bundleTotalPriceWidget: PackageBundlePriceWidget by bindView(R.id.bundle_total)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    var isHideToolbarView = false;

    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)
    val changeHotel by lazy { toolbar.menu.findItem(R.id.package_change_hotel) }
    val changeHotelRoom by lazy { toolbar.menu.findItem(R.id.package_change_hotel_room) }
    val changeFlight by lazy { toolbar.menu.findItem(R.id.package_change_flight) }
    val TOOLBAR_TEXT_ANIMATION_DURATION = 200L

    init {
        View.inflate(context, R.layout.bundle_overview, this)
        priceChangeWidget.viewmodel = PriceChangeViewModel(context)
        bundleTotalPriceWidget.viewModel = BundlePriceViewModel(context)
        toolbar.viewModel = CheckoutToolbarViewModel(context)

        toolbar.inflateMenu(R.menu.menu_package_checkout)
        toolbar.overflowIcon = ContextCompat.getDrawable(context, R.drawable.ic_create_white_24dp)

        toolbar.viewModel.showChangePackageMenuObservable.subscribe { visible ->
            toolbar.menu.setGroupVisible(R.id.package_change_menu, visible)
        }

        checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }

        checkoutPresenter.paymentWidget.viewmodel.toolbarTitle.subscribe(toolbar.viewModel.toolbarTitle)
        checkoutPresenter.paymentWidget.viewmodel.editText.subscribe(toolbar.viewModel.editText)
        checkoutPresenter.paymentWidget.viewmodel.menuVisibility.subscribe(toolbar.viewModel.menuVisibility)
        checkoutPresenter.paymentWidget.viewmodel.enableMenuItem.subscribe(toolbar.viewModel.enableMenuItem)
        checkoutPresenter.paymentWidget.viewmodel.visibleMenuWithTitleDone.subscribe(toolbar.viewModel.visibleMenuWithTitleDone)
        toolbar.viewModel.doneClicked.subscribe {
            if (checkoutPresenter.currentState == PackagePaymentWidget::class.java.name) {
                checkoutPresenter.paymentWidget.viewmodel.doneClicked.onNext(Unit)
            }
        }

        checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }

        bundleWidget.toggleMenuObservable.subscribe(toolbar.toggleMenuObserver)
        bundleWidget.toggleMenuObservable.subscribe {
            toggleCheckoutButton(0.25f, false)
        }

        //we need to set this empty space inorder to remove the title string
        collapsingToolbarLayout.title = " ";
        collapsingToolbarLayout.setContentScrimColor(resources.getColor(R.color.packages_primary_color))
        collapsingToolbarLayout.setStatusBarScrimColor(resources.getColor(R.color.packages_primary_color))
        checkoutOverviewHeaderToolbar.travelers.visibility = View.GONE
        appBarLayout.addOnOffsetChangedListener(this);
        val floatingToolbarLayoutParams = checkoutOverviewFloatingToolbar.destinationText.layoutParams as LinearLayout.LayoutParams
        floatingToolbarLayoutParams.gravity = Gravity.CENTER
        toggleCheckoutHeader(false)
    }

    fun swapViews(show: Boolean) {
        var parent = bundleWidget.parent
        (parent as ViewGroup).removeView(bundleWidget)
        if (show) {
            removeView(bundleWidget)
            coordinatorLayout.addView(bundleWidget, 2)
            val bundleWidgetLayoutParams = bundleWidget.layoutParams as CoordinatorLayout.LayoutParams
            bundleWidgetLayoutParams.behavior = AppBarLayout.ScrollingViewBehavior();
            bundleWidgetLayoutParams.gravity = Gravity.FILL_VERTICAL
            bundleWidget.setPadding(0, 0, 0, 0)
            bundleWidget.isFillViewport = true
        } else {
            coordinatorLayout.removeView(bundleWidget)
            addView(bundleWidget)
            bundleWidget.setPadding(0, toolbarHeight, 0, 0)
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
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(context, R.style.ToolbarSubtitleTextAppearance)

        changeHotel.setOnMenuItemClickListener({
            toggleCheckoutHeader(false)
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            params.searchProduct = null
            bundleWidget.viewModel.hotelParamsObservable.onNext(params)
            true
        })

        changeHotelRoom.setOnMenuItemClickListener({
            toggleCheckoutHeader(false)
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            val intent = Intent(context, PackageHotelActivity::class.java)
            intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
            (context as AppCompatActivity).startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)
            true
        })

        changeFlight.setOnMenuItemClickListener({
            toggleCheckoutHeader(false)
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_FLIGHT
            params.searchProduct = Constants.PRODUCT_FLIGHT
            params.selectedLegId = null
            bundleWidget.viewModel.flightParamsObservable.onNext(params)
            true
        })

        val checkoutPresenterLayoutParams = checkoutPresenter.layoutParams as ViewGroup.MarginLayoutParams
        checkoutPresenterLayoutParams.setMargins(0, toolbarHeight, 0, 0)
    }

    fun toggleCheckoutHeader(show: Boolean, swap: Boolean) {
        toolbar.setBackgroundColor(ContextCompat.getColor(context, if (show) android.R.color.transparent else R.color.packages_primary_color))
        statusBar?.setBackgroundColor(ContextCompat.getColor(context, if (show) android.R.color.transparent else R.color.packages_primary_color))
        checkoutOverviewFloatingToolbar.visibility = if (show) View.VISIBLE else View.GONE
        appBarLayout.setExpanded(show)
        appBarLayout.isActivated = show
        bundleWidget.isNestedScrollingEnabled = show
        collapsingToolbarLayout.isTitleEnabled = show
        if (swap) {
            swapViews(show)
        }
    }

    fun toggleCheckoutHeader(show: Boolean) {
        toggleCheckoutHeader(show, true)
    }

    val defaultTransition = object : Presenter.DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
        }
    }

    val checkoutTransition = object : Presenter.Transition(BundleDefault::class.java, PackageCheckoutPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            checkoutPresenter.visibility = View.VISIBLE
            checkoutOverviewHeaderToolbar.visibility = View.GONE
            if (forward) {
                toggleCheckoutHeader(false, false)
                toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
                checkoutButton.visibility = View.GONE
            } else {
                toggleCheckoutHeader(true)
                swapViews(true)
                toolbar.menu.setGroupVisible(R.id.package_change_menu, true)
                checkoutButton.visibility = View.VISIBLE
            }
            toolbar.alpha = if (forward) 0f else 1f
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            bundleWidget.translationY = if (forward) f * -bundleWidget.height.toFloat() else (1 - f) * bundleWidget.height.toFloat()
            checkoutPresenter.translationY = if (forward) (f - 1) * -checkoutPresenter.height.toFloat() else f * checkoutPresenter.height.toFloat()
            toolbar.alpha = 1f * if (forward) f else (1 - f)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutOverviewHeaderToolbar.visibility = View.GONE
            toolbar.alpha = 1f
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, PackageCheckoutPresenter::class.java, CVVEntryWidget::class.java) {
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

    override fun back(): Boolean {
        bundleWidget.bundleHotelWidget.backButtonPressed()
        return super.back()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        var maxScroll = appBarLayout.totalScrollRange;
        if (maxScroll != 0) {
            var percentage = (Math.abs(offset) / maxScroll).toFloat()

            if (percentage == 1f && isHideToolbarView) {
                if (!Strings.equals(currentState, PackageCheckoutPresenter::class.java.name)) {
                    checkoutOverviewHeaderToolbar.visibility = View.VISIBLE;
                }
                val lp1 = checkoutOverviewHeaderToolbar.destinationText.layoutParams as LinearLayout.LayoutParams
                lp1.gravity = Gravity.LEFT
                val lp2 = checkoutOverviewHeaderToolbar.checkInOutDates.layoutParams as LinearLayout.LayoutParams
                lp2.gravity = Gravity.LEFT
                val distance = checkoutOverviewFloatingToolbar.destinationText.height * .3f
                checkoutOverviewHeaderToolbar.destinationText.pivotX = 0f
                checkoutOverviewHeaderToolbar.destinationText.pivotY = 0f
                checkoutOverviewHeaderToolbar.destinationText.scaleX = .7f
                checkoutOverviewHeaderToolbar.destinationText.scaleY = .7f
                checkoutOverviewHeaderToolbar.checkInOutDates.alpha = 0f
                checkoutOverviewHeaderToolbar.checkInOutDates.translationY = -distance
                val animator = ObjectAnimator.ofFloat(checkoutOverviewHeaderToolbar.checkInOutDates, "alpha", 0f, 1f)
                animator.duration = TOOLBAR_TEXT_ANIMATION_DURATION
                animator.start()
                isHideToolbarView = !isHideToolbarView;

            } else if (percentage < 1f && !isHideToolbarView) {
                checkoutOverviewHeaderToolbar.visibility = View.GONE;
                isHideToolbarView = !isHideToolbarView;
            }
        }
    }

    fun toggleCheckoutButton(alpha: Float, isEnabled: Boolean) {
        checkoutButton.isEnabled = isEnabled
        checkoutButton.setTextColor(checkoutButton.textColors.withAlpha((alpha * 255f).toInt()))
    }

    class BundleDefault
}