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
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.bookings.widget.PackagePaymentWidget
import com.expedia.bookings.widget.packages.CheckoutOverviewHeader
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.endlessObserver
import com.expedia.vm.CheckoutToolbarViewModel

class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener, AppBarLayout.OnOffsetChangedListener {
    val ANIMATION_DURATION = 450L

    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val collapsingToolbarLayout: CollapsingToolbarLayout by bindView(R.id.collapsing_toolbar)
    val coordinatorLayout: CoordinatorLayout by bindView(R.id.coordinator_layout)
    val appBarLayout: AppBarLayout by bindView(R.id.app_bar)
    val imageHeader: ImageView by bindView(R.id.overview_image)
    val checkoutOverviewHeaderToolbar: CheckoutOverviewHeader by bindView(R.id.checkout_overview_header_toolbar)
    val checkoutOverviewFloatingToolbar: CheckoutOverviewHeader by bindView(R.id.checkout_overview_floating_toolbar)
    val bundleWidget: BundleWidget by bindView(R.id.bundle_widget)
    val checkoutPresenter: PackageCheckoutPresenter by bindView(R.id.checkout_presenter)

    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    var isHideToolbarView = false;

    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)
    val changeHotel by lazy { toolbar.menu.findItem(R.id.package_change_hotel) }
    val changeHotelRoom by lazy { toolbar.menu.findItem(R.id.package_change_hotel_room) }
    val changeFlight by lazy { toolbar.menu.findItem(R.id.package_change_flight) }
    val TOOLBAR_TEXT_ANIMATION_DURATION = 200L

    init {
        View.inflate(context, R.layout.bundle_overview, this)

        toolbar.viewModel = CheckoutToolbarViewModel(context)

        toolbar.inflateMenu(R.menu.menu_package_checkout)
        toolbar.overflowIcon = ContextCompat.getDrawable(context, R.drawable.ic_create_white_24dp)

        toolbar.viewModel.showChangePackageMenuObservable.subscribe { visible ->
            toolbar.menu.setGroupVisible(R.id.package_change_menu, visible)
        }

        checkoutPresenter.checkoutButton.setOnClickListener {
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

        checkoutPresenter.checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }

        bundleWidget.toggleMenuObservable.subscribe(toolbar.toggleMenuObserver)
        bundleWidget.toggleMenuObservable.subscribe {
            checkoutPresenter.toggleCheckoutButton(false)
        }

        setUpCollapsingToolbar()
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
            toggleOverviewHeader(false)
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            params.searchProduct = null
            bundleWidget.viewModel.hotelParamsObservable.onNext(params)
            true
        })

        changeHotelRoom.setOnMenuItemClickListener({
            toggleOverviewHeader(false)
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            val intent = Intent(context, PackageHotelActivity::class.java)
            intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
            (context as AppCompatActivity).startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)
            true
        })

        changeFlight.setOnMenuItemClickListener({
            toggleOverviewHeader(false)
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
        checkoutPresenter.mainContent.visibility = View.GONE
    }

    /** Collapsing Toolbar **/
    fun setUpCollapsingToolbar() {
        //we need to set this empty space inorder to remove the title string
        collapsingToolbarLayout.title = " ";
        collapsingToolbarLayout.setContentScrimColor(resources.getColor(R.color.packages_primary_color))
        collapsingToolbarLayout.setStatusBarScrimColor(resources.getColor(R.color.packages_primary_color))
        checkoutOverviewHeaderToolbar.travelers.visibility = View.GONE
        appBarLayout.addOnOffsetChangedListener(this);
        val floatingToolbarLayoutParams = checkoutOverviewFloatingToolbar.destinationText.layoutParams as LinearLayout.LayoutParams
        floatingToolbarLayoutParams.gravity = Gravity.CENTER
        toggleOverviewHeader(false)
        checkoutPresenter.checkoutTranslationObserver.subscribe { y ->
            val distance = -appBarLayout.totalScrollRange + (y / checkoutPresenter.height * appBarLayout.totalScrollRange).toInt()
            val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            val enable = y != 0f
            toggleCollapsingToolBar(enable)
            behavior.topAndBottomOffset = distance
        }
    }

    fun toggleOverviewHeader(show: Boolean) {
        toolbar.setBackgroundColor(ContextCompat.getColor(context, if (show) android.R.color.transparent else R.color.packages_primary_color))
        appBarLayout.setExpanded(show)
        toggleCollapsingToolBar(show)
        swapViews(show)
    }

    fun toggleCollapsingToolBar(enable: Boolean) {
        checkoutOverviewFloatingToolbar.visibility = if (enable) View.VISIBLE else View.GONE
        appBarLayout.isActivated = enable
        bundleWidget.isNestedScrollingEnabled = enable
        collapsingToolbarLayout.isTitleEnabled = enable
    }

    /** Swaps the bundle widget out of the coordinator
     * layout into the main layout to address scrolling/animation bugs**/
    fun swapViews(toCoordinatorLayout: Boolean) {
        var parent = bundleWidget.parent
        (parent as ViewGroup).removeView(bundleWidget)
        if (toCoordinatorLayout) {
            removeView(bundleWidget)
            coordinatorLayout.addView(bundleWidget, 2)
            val bundleWidgetLayoutParams = bundleWidget.layoutParams as CoordinatorLayout.LayoutParams
            bundleWidgetLayoutParams.behavior = AppBarLayout.ScrollingViewBehavior();
            bundleWidgetLayoutParams.gravity = Gravity.FILL_VERTICAL
            bundleWidget.setPadding(0, 0, 0, 0)
            bundleWidget.isFillViewport = true
        } else {
            coordinatorLayout.removeView(bundleWidget)
            addView(bundleWidget, 1)
            bundleWidget.setPadding(0, toolbarHeight, 0, 0)
        }
    }

    val defaultTransition = object : Presenter.DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
            toggleCollapsingToolBar(!forward)
        }
    }

    val checkoutTransition = object : Presenter.Transition(BundleDefault::class.java, PackageCheckoutPresenter::class.java) {
        var translationDistance = 0f

        override fun startTransition(forward: Boolean) {
            toggleCollapsingToolBar(true)
            translationDistance = checkoutPresenter.mainContent.translationY
            checkoutPresenter.checkoutButton.translationY = if (forward) 0f else checkoutPresenter.checkoutButton.height.toFloat()
            checkoutPresenter.chevron.rotation = 0f
            toolbar.menu.setGroupVisible(R.id.package_change_menu, !forward)
            toolbar.alpha = if (forward) 0f else 1f
            checkoutPresenter.mainContent.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            translateHeader(f, forward)
            translateCheckout(f, forward)
            translateBottomContainer(f, forward)
            toolbar.alpha = 1f * if (forward) f else (1 - f)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            toggleCollapsingToolBar(!forward)
            checkoutPresenter.checkoutButton.translationY = if (forward) checkoutPresenter.checkoutButton.height.toFloat() else 0f
            checkoutPresenter.mainContent.visibility = if (forward) View.VISIBLE else View.GONE
            checkoutPresenter.mainContent.translationY = 0f
            toolbar.alpha = 1f
        }

        private fun translateHeader(f: Float, forward: Boolean) {
            val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior

            val scrollY = if (forward) f * -appBarLayout.totalScrollRange else (f - 1) * appBarLayout.totalScrollRange
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
                (checkoutOverviewHeaderToolbar.destinationText.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.LEFT
                (checkoutOverviewHeaderToolbar.checkInOutDates.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.LEFT
                val distance = checkoutOverviewFloatingToolbar.destinationText.height * .1f
                checkoutOverviewHeaderToolbar.destinationText.pivotX = 0f
                checkoutOverviewHeaderToolbar.destinationText.pivotY = 0f
                checkoutOverviewHeaderToolbar.destinationText.scaleX = .9f
                checkoutOverviewHeaderToolbar.destinationText.scaleY = .9f
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

    class BundleDefault
}