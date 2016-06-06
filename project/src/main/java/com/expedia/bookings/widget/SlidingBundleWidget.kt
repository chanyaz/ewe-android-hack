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
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.packages.BundleWidget
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.BundlePriceViewModel
import com.expedia.vm.packages.PackageSearchType
import rx.subjects.PublishSubject

class SlidingBundleWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val REGULAR_ANIMATION_DURATION = 400

    val bundlePriceWidget: TotalPriceWidget by bindView(R.id.bundle_price_widget)
    val bundleOverViewWidget: BundleWidget by bindView(R.id.bundle_widget)
    val bundlePriceFooter: TotalPriceWidget by bindView(R.id.total_price_widget)

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
                    if (!activity.intent.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
                        bundlePriceWidget.animateBundleWidget(1f, true)
                        finalizeBundleTransition(true)
                        bundlePriceFooter.translationY = - statusBarHeight.toFloat()
                        post({
                            closeBundleOverview()
                        })
                    } else {
                        bundlePriceFooter.translationY = - statusBarHeight.toFloat()
                        translationY = height.toFloat() - bundlePriceWidget.height
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
        bundlePriceWidget.bundleTotalText.visibility =  View.VISIBLE
        bundlePriceWidget.bundleTotalIncludes.visibility =  View.VISIBLE
        bundlePriceWidget.bundleTotalPrice.visibility =  View.VISIBLE
        bundlePriceWidget.setBackgroundColor(if (forward) Color.WHITE else ContextCompat.getColor(context, R.color.packages_primary_color))
    }

    fun updateBundleTransition(f: Float, forward: Boolean) {
        var distance = height.toFloat() - bundlePriceWidget.height - translationDistance
        val pos = if (forward) Math.max(statusBarHeight.toFloat(), (1 - f) * translationDistance) else translationDistance + (f * distance)
        translateBundleOverview(pos)
    }

    fun finalizeBundleTransition(forward: Boolean) {
        bundlePriceWidget.bundleTitle.visibility = if (forward) View.VISIBLE else View.GONE
        bundlePriceWidget.bundleSubtitle.visibility = if (forward) View.VISIBLE else View.GONE
        bundlePriceWidget.bundleTotalText.visibility = if (forward) View.GONE else View.VISIBLE
        bundlePriceWidget.bundleTotalIncludes.visibility = if (forward) View.GONE else View.VISIBLE
        bundlePriceWidget.bundleTotalPrice.visibility = if (forward) View.GONE else View.VISIBLE
        bundlePriceWidget.bundleChevron.rotation = if (forward) 180f else 0f
        bundlePriceWidget.setBackgroundColor(if (forward) ContextCompat.getColor(context, R.color.packages_primary_color) else Color.WHITE)
        translationY = if (forward) statusBarHeight.toFloat() else height.toFloat() - bundlePriceWidget.height
        isMoving = false
        bundlePriceWidget.contentDescription = bundlePriceWidget.viewModel.getAccessibleContentDescription(true, forward)
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

    fun setupBundleViews() {
        bundleOverViewWidget.viewModel = BundleOverviewViewModel(context, null)
        bundleOverViewWidget.viewModel.hotelParamsObservable.onNext(Db.getPackageParams())
        bundleOverViewWidget.viewModel.hotelResultsObservable.onNext(Unit)
        bundleOverViewWidget.viewModel.toolbarTitleObservable.subscribeText(bundlePriceWidget.bundleTitle)
        bundleOverViewWidget.viewModel.toolbarSubtitleObservable.subscribeText(bundlePriceWidget.bundleSubtitle)
        bundlePriceFooter.viewModel = BundlePriceViewModel(context)
        bundlePriceWidget.viewModel = BundlePriceViewModel(context, true)
        bundlePriceWidget.bundleChevron.visibility = View.VISIBLE
        bundleOverViewWidget.setPadding(0, Ui.getToolbarSize(context), 0, 0)
        val icon = ContextCompat.getDrawable(context, R.drawable.read_more).mutate()
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        //if change package hotel search
        if (Db.getPackageParams()?.pageType == Constants.PACKAGE_CHANGE_HOTEL) {
            bundleOverViewWidget.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
            bundleOverViewWidget.outboundFlightWidget.viewModel.flight.onNext(Db.getPackageSelectedOutboundFlight())
            bundleOverViewWidget.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            bundleOverViewWidget.inboundFlightWidget.viewModel.flight.onNext(Db.getPackageSelectedInboundFlight())
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
}
