package com.expedia.bookings.packages.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.packages.vm.BundleOverviewViewModel
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.packages.vm.PackageTotalPriceViewModel
import com.expedia.bookings.widget.TotalPriceWidget
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class SlidingBundleWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val REGULAR_ANIMATION_DURATION = 400

    val bundlePriceWidget: TotalPriceWidget by bindView(R.id.bundle_price_widget)
    val bundleOverViewWidget: BundleWidget by bindView(R.id.bundle_widget)
    val bundlePriceFooter: TotalPriceWidget by bindView(R.id.total_price_widget)
    val bundleWidgetTopShadow: View by bindView(R.id.bundle_price_widget_shadow)
    val bundlePriceWidgetContainer: View by bindView(R.id.bundle_price_widget_container)
    val bundleFooterContainer: View by bindView(R.id.bundle_price_footer_container)

    var translationDistance = 0f
    val statusBarHeight = Ui.getStatusBarHeight(context)

    var isMoving = false
    var canMove = false
    var isFirstLaunch = true
    var animationFinished = PublishSubject.create<Unit>()
    val overviewLayoutListener = OverviewLayoutListener()

    init {
        orientation = VERTICAL
        View.inflate(getContext(), R.layout.bundle_slide_widget, this)
        bundlePriceWidget.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (bundlePriceWidget.height != 0) {
                    bundlePriceWidget.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val activity = context as Activity
                    if (activity.intent.extras == null) {
                        bundlePriceWidget.animateBundleWidget(1f, true)
                        finalizeBundleTransition(true, false)
                        bundleFooterContainer.translationY = -statusBarHeight.toFloat()
                        post({
                            closeBundleOverview()
                        })
                    } else {
                        bundleFooterContainer.translationY = -statusBarHeight.toFloat()
                        translationY = height.toFloat() - getBottomOffsetForClosing()

                        if (activity.intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
                            visibility = View.GONE
                        }
                    }
                }
            }
        })

        if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.JAPAN) {
            val bundleTotalText = StrUtils.bundleTotalWithTaxesString(context)
            bundlePriceFooter.bundleTotalText.text = bundleTotalText
            bundlePriceWidget.bundleTotalText.text = bundleTotalText
        }
    }

    fun startBundleTransition(forward: Boolean) {
        isMoving = true
        translationDistance = translationY
        bundlePriceWidget.bundleTitle.visibility = View.VISIBLE
        bundlePriceWidget.bundleSubtitle.visibility = View.VISIBLE
        bundlePriceWidget.bundleTotalText.visibility = View.VISIBLE
        bundlePriceWidget.bundleTotalIncludes.visibility = View.VISIBLE
        bundlePriceWidget.bundleTotalPrice.visibility = View.VISIBLE
        bundlePriceWidget.setBackgroundColor(if (forward) Color.WHITE else ContextCompat.getColor(context, R.color.packages_primary_color))
        if (forward) {
            bundleWidgetTopShadow.visibility = View.GONE
            bundleOverViewWidget.scrollSpaceView.viewTreeObserver?.addOnGlobalLayoutListener(overviewLayoutListener)
        } else {
            bundleWidgetTopShadow.visibility = View.VISIBLE
            bundleOverViewWidget.scrollSpaceView.viewTreeObserver?.removeOnGlobalLayoutListener(overviewLayoutListener)
        }
    }

    inner class OverviewLayoutListener : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout () {
            updateScrollingSpace(bundleOverViewWidget.scrollSpaceView)
        }
    }

    private fun updateScrollingSpace(scrollSpaceView: View) {
        val footerViewLocation = IntArray(2)
        val bundleViewLocation = IntArray(2)

        bundlePriceFooter.getLocationOnScreen(footerViewLocation)
        bundleOverViewWidget.getLocationOnScreen(bundleViewLocation)
        val scrollSpaceHeight = Math.max(0, (bundleViewLocation[1] + bundleOverViewWidget.height - Ui.getToolbarSize(context)) - footerViewLocation[1])

        scrollSpaceView.layoutParams.height = scrollSpaceHeight
    }

    fun updateBundleTransition(f: Float, forward: Boolean) {
        val distance = height.toFloat() - getBottomOffsetForClosing() - translationDistance
        val pos = if (forward) Math.max(statusBarHeight.toFloat(), (1 - f) * translationDistance) else translationDistance + (f * distance)
        translateBundleOverview(pos)
    }

    fun finalizeBundleTransition(forward: Boolean, trackLoad: Boolean = true) {
        bundlePriceWidget.bundleTitle.visibility = if (forward) View.VISIBLE else View.GONE
        bundlePriceWidget.bundleSubtitle.visibility = if (forward) View.VISIBLE else View.GONE
        bundlePriceWidget.bundleTotalText.visibility = if (forward) View.GONE else View.VISIBLE
        bundlePriceWidget.bundleTotalIncludes.visibility = if (forward) View.GONE else View.VISIBLE
        bundlePriceWidget.bundleTotalPrice.visibility = if (forward) View.GONE else View.VISIBLE
        bundlePriceWidget.bundleChevron.rotation = if (forward) 180f else 0f
        bundlePriceWidget.setBackgroundColor(if (forward) ContextCompat.getColor(context, R.color.packages_primary_color) else Color.WHITE)
        translationY = if (forward) statusBarHeight.toFloat() else height.toFloat() - getBottomOffsetForClosing()
        isMoving = false
        bundlePriceWidget.contentDescription = bundlePriceWidget.viewModel.getAccessibleContentDescription(false, true, forward)
        if (forward && trackLoad) {
            PackagesTracking().trackViewBundlePageLoad()
        } else {
            bundlePriceWidget.enable()
        }
    }

    fun translateBundleOverview(distance: Float) {
        val distanceMax = height.toFloat() - getBottomOffsetForClosing()
        val f = (distanceMax - distance) / distanceMax
        if (distance <= distanceMax && distance >= 0) {
            translationY = distance
            bundlePriceWidget.bundleTitle.visibility = View.VISIBLE
            bundlePriceWidget.bundleSubtitle.visibility = View.VISIBLE
            bundlePriceWidget.bundleTotalText.visibility = View.VISIBLE
            bundlePriceWidget.bundleTotalIncludes.visibility = View.VISIBLE
            bundlePriceWidget.animateBundleWidget(f, true)
        }
    }

    fun openBundleOverview() {
        animateBundleOverview(REGULAR_ANIMATION_DURATION, true)
    }

    fun closeBundleOverview() {
        animateBundleOverview(REGULAR_ANIMATION_DURATION, false)
    }

    private fun animateBundleOverview(animDuration: Int, open: Boolean) {
        val distanceMax = height.toFloat() - getBottomOffsetForClosing()
        val end = if (open) statusBarHeight.toFloat() else distanceMax
        val animator = ObjectAnimator.ofFloat(this, "translationY", translationY, end)
        animator.duration = animDuration.toLong()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(animator: Animator?) {
            }

            override fun onAnimationRepeat(animator: Animator?) {
            }

            override fun onAnimationStart(animator: Animator?) {
                startBundleTransition(open)
            }

            override fun onAnimationEnd(animator: Animator) {
                if (isFirstLaunch) {
                    animationFinished.onNext(Unit)
                }
                isFirstLaunch = false
                finalizeBundleTransition(open)
                isMoving = false
            }
        })
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener {
            bundlePriceWidget.animateBundleWidget((translationY / distanceMax), false)
        })
        animator.start()
    }

    fun updateBundleViews(product: String) {
        bundleOverViewWidget.viewModel.hotelParamsObservable.onNext(Db.sharedInstance.packageParams)
        bundleOverViewWidget.viewModel.hotelResultsObservable.onNext(Unit)

        if (product == Constants.PRODUCT_FLIGHT) {
            bundleOverViewWidget.viewModel.flightParamsObservable.onNext(Db.sharedInstance.packageParams)
            val type = if (Db.sharedInstance.packageParams.isOutboundSearch(isMidAPIEnabled())) PackageProductSearchType.MultiItemOutboundFlights else PackageProductSearchType.MultiItemInboundFlights
            bundleOverViewWidget.viewModel.flightResultsObservable.onNext(type)

            if (!Db.sharedInstance.packageParams.isOutboundSearch(isMidAPIEnabled()) && Db.sharedInstance.packageSelectedOutboundFlight != null) {
                bundleOverViewWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageProductSearchType.MultiItemOutboundFlights)
                bundleOverViewWidget.outboundFlightWidget.viewModel.flight.onNext(Db.sharedInstance.packageSelectedOutboundFlight)
                bundleOverViewWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
            }
            if (Db.getPackageSelectedHotel() != null) {
                bundleOverViewWidget.bundleHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
            }
            updateBundlePricing()
        }

        if (Db.sharedInstance.packageParams?.pageType == Constants.PACKAGE_CHANGE_HOTEL) {
            bundleOverViewWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageProductSearchType.MultiItemOutboundFlights)
            bundleOverViewWidget.outboundFlightWidget.viewModel.flight.onNext(Db.sharedInstance.packageSelectedOutboundFlight)
            bundleOverViewWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageProductSearchType.MultiItemInboundFlights)
            bundleOverViewWidget.inboundFlightWidget.viewModel.flight.onNext(Db.getPackageFlightBundle().second)
        }
    }

    fun setupBundleViews(product: String) {
        bundleOverViewWidget.viewModel = BundleOverviewViewModel(context, null)
        bundleOverViewWidget.viewModel.toolbarTitleObservable.subscribeText(bundlePriceWidget.bundleTitle)
        bundleOverViewWidget.viewModel.toolbarSubtitleObservable.subscribeText(bundlePriceWidget.bundleSubtitle)
        bundlePriceFooter.viewModel = PackageTotalPriceViewModel(context)
        bundlePriceWidget.viewModel = PackageTotalPriceViewModel(context, true)
        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            bundlePriceWidget.bundleChevron.visibility = View.GONE
            bundlePriceWidget.closeIcon.visibility = View.VISIBLE
        } else {
            bundlePriceWidget.bundleChevron.visibility = View.VISIBLE
            bundlePriceWidget.closeIcon.visibility = View.GONE
        }
        val icon = ContextCompat.getDrawable(context, R.drawable.read_more).mutate()
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        if (PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.JAPAN) {
            bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text))
        }
        bundlePriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))

        updateBundleViews(product)
    }

    private fun updateBundlePricing() {
        val packagePrice = Db.getPackageResponse().getCurrentOfferPrice()
        if (packagePrice != null) {
            if (PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.JAPAN) {
                bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text))
            }
            val packageSavings = Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                    packagePrice.tripSavings.currencyCode)
            bundlePriceWidget.viewModel.pricePerPerson.onNext(Money(BigDecimal(packagePrice.pricePerPerson.amount.toDouble()),
                    packagePrice.pricePerPerson.currencyCode))
            bundlePriceFooter.viewModel.total.onNext(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                    packagePrice.packageTotalPrice.currencyCode))
            bundlePriceFooter.viewModel.savings.onNext(packageSavings)
        }
    }

    private fun getBottomOffsetForClosing(): Int {
        return if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) 0 else bundlePriceWidgetContainer.height
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
}
