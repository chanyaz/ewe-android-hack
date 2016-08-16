package com.expedia.bookings.widget

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
import android.view.animation.AccelerateDecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.packages.BundleWidget
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.BundlePriceViewModel
import com.expedia.vm.packages.PackageSearchType
import rx.subjects.PublishSubject
import java.math.BigDecimal

class SlidingBundleWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val REGULAR_ANIMATION_DURATION = 400

    val bundlePriceWidget: TotalPriceWidget by bindView(R.id.bundle_price_widget)
    val bundleOverViewWidget: BundleWidget by bindView(R.id.bundle_widget)
    val bundlePriceFooter: TotalPriceWidget by bindView(R.id.total_price_widget)
    val bundleWidgetShadow: View by bindView(R.id.bundle_price_widget_shadow)

    var translationDistance = 0f
    val statusBarHeight = Ui.getStatusBarHeight(context)

    var isMoving = false
    var canMove = false
    var isFirstLaunch = true
    var animationFinished = PublishSubject.create<Unit>()

    init {
        View.inflate(getContext(), R.layout.bundle_slide_widget, this)
        bundlePriceWidget.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (bundlePriceWidget.height != 0) {
                    bundlePriceWidget.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val activity = context as Activity
                    if (activity.intent.extras == null) {
                        bundlePriceWidget.animateBundleWidget(1f, true)
                        finalizeBundleTransition(true, false)
                        bundlePriceFooter.translationY = -statusBarHeight.toFloat()
                        post({
                            closeBundleOverview()
                        })
                    } else {
                        bundlePriceFooter.translationY = -statusBarHeight.toFloat()
                        translationY = height.toFloat() - bundlePriceWidget.height

                        if (activity.intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
                            visibility = View.GONE
                        }
                    }
                }
            }
        })
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
            bundleWidgetShadow.visibility = View.GONE
        } else {
            bundleWidgetShadow.visibility = View.VISIBLE
        }
    }

    fun updateBundleTransition(f: Float, forward: Boolean) {
        val distance = height.toFloat() - bundlePriceWidget.height - translationDistance
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
        translationY = if (forward) statusBarHeight.toFloat() else height.toFloat() - bundlePriceWidget.height
        isMoving = false
        bundlePriceWidget.contentDescription = bundlePriceWidget.viewModel.getAccessibleContentDescription(false, true, forward)
        if (forward && trackLoad) {
            PackagesTracking().trackViewBundlePageLoad()
        }
        else {
            bundlePriceWidget.enable()
        }
    }

    fun translateBundleOverview(distance: Float) {
        val distanceMax = height.toFloat() - bundlePriceWidget.height
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
        val distanceMax = height.toFloat() - bundlePriceWidget.height
        val end = if (open) statusBarHeight.toFloat() else height.toFloat() - bundlePriceWidget.height
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
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { anim ->
            bundlePriceWidget.animateBundleWidget((translationY / distanceMax), open)
        })
        animator.start()
    }

    fun updateBundleViews(product: String) {
        bundleOverViewWidget.viewModel.hotelParamsObservable.onNext(Db.getPackageParams())
        bundleOverViewWidget.viewModel.hotelResultsObservable.onNext(Unit)

        if (product == Constants.PRODUCT_FLIGHT) {
            bundleOverViewWidget.viewModel.flightParamsObservable.onNext(Db.getPackageParams())
            val type = if (Db.getPackageParams().isOutboundSearch()) PackageSearchType.OUTBOUND_FLIGHT else PackageSearchType.INBOUND_FLIGHT
            bundleOverViewWidget.viewModel.flightResultsObservable.onNext(type)

            if (!Db.getPackageParams().isOutboundSearch() && Db.getPackageSelectedOutboundFlight() != null) {
                bundleOverViewWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
                bundleOverViewWidget.outboundFlightWidget.viewModel.flight.onNext(Db.getPackageSelectedOutboundFlight())
                bundleOverViewWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
            }
            if (Db.getPackageSelectedHotel() != null) {
                bundleOverViewWidget.bundleHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
            }
            updateBundlePricing()
        }

        if (Db.getPackageParams()?.pageType == Constants.PACKAGE_CHANGE_HOTEL) {
            bundleOverViewWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
            bundleOverViewWidget.outboundFlightWidget.viewModel.flight.onNext(Db.getPackageSelectedOutboundFlight())
            bundleOverViewWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            bundleOverViewWidget.inboundFlightWidget.viewModel.flight.onNext(Db.getPackageFlightBundle().second)
        }
    }

    fun setupBundleViews(product: String) {
        bundleOverViewWidget.viewModel = BundleOverviewViewModel(context, null)
        bundleOverViewWidget.viewModel.toolbarTitleObservable.subscribeText(bundlePriceWidget.bundleTitle)
        bundleOverViewWidget.viewModel.toolbarSubtitleObservable.subscribeText(bundlePriceWidget.bundleSubtitle)
        bundlePriceFooter.viewModel = BundlePriceViewModel(context)
        bundlePriceWidget.viewModel = BundlePriceViewModel(context, true)
        bundlePriceWidget.bundleChevron.visibility = View.VISIBLE
        bundleOverViewWidget.setPadding(0, Ui.getToolbarSize(context), 0, 0)
        val icon = ContextCompat.getDrawable(context, R.drawable.read_more).mutate()
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        updateBundleViews(product)
    }

    fun addBundleTransitionFrom(fromClass: String): Presenter.Transition {
        val transition = object : Presenter.Transition(fromClass, SlidingBundleWidget::class.java.name, AccelerateDecelerateInterpolator(), REGULAR_ANIMATION_DURATION) {
            override fun startTransition(forward: Boolean) {
                startBundleTransition(forward)
            }

            override fun updateTransition(f: Float, forward: Boolean) {
                updateBundleTransition(f, forward)
            }

            override fun endTransition(forward: Boolean) {
                finalizeBundleTransition(forward)
            }
        }
        return transition
    }

    private fun updateBundlePricing() {
        val currentOffer: PackageOfferModel = Db.getPackageResponse().packageResult.currentSelectedOffer
        val packagePrice: PackageOfferModel.PackagePrice = currentOffer.price
        bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text))
        val packageSavings = Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                packagePrice.tripSavings.currencyCode)
        bundlePriceWidget.viewModel.pricePerPerson.onNext(Money(BigDecimal(packagePrice.pricePerPerson.amount.toDouble()),
                packagePrice.packageTotalPrice.currencyCode))
        bundlePriceFooter.viewModel.total.onNext(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                packagePrice.packageTotalPrice.currencyCode))
        bundlePriceFooter.viewModel.savings.onNext(packageSavings)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
}
