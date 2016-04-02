package com.expedia.bookings.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.packages.BundleWidget
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.BundlePriceViewModel

class SlidingBundleWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val SWIPE_MIN_DISTANCE = 10
    val SWIPE_THRESHOLD_VELOCITY = 300
    val FAST_ANIMATION_DURATION = 150
    val REGULAR_ANIMATION_DURATION = 400

    val bundlePriceWidget: TotalPriceWidget by bindView(R.id.bundle_price_widget)
    val bundleOverViewWidget: BundleWidget by bindView(R.id.bundle_widget)
    val bundlePriceFooter: TotalPriceWidget by bindView(R.id.total_price_widget)

    var translationDistance = 0f
    val statusBarHeight = Ui.getStatusBarHeight(context)

    var isMoving = false
    var canMove = false

    init {
        View.inflate(getContext(), R.layout.bundle_slide_widget, this)
        bundlePriceWidget.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (bundlePriceWidget.height != 0) {
                    bundlePriceWidget.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    translationY = height.toFloat() - bundlePriceWidget.height
                    bundlePriceFooter.translationY = - statusBarHeight.toFloat()
                }
            }
        })
    }

    fun startBundleTransition(forward: Boolean) {
        isMoving = true
        translationDistance = translationY
        bundlePriceWidget.bundleTitle.visibility = View.VISIBLE
        bundlePriceWidget.bundleSubtitle.visibility = View.VISIBLE
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
        bundlePriceWidget.setBackgroundColor(if (forward) ContextCompat.getColor(context, R.color.packages_primary_color) else Color.WHITE)
        translationY = if (forward) statusBarHeight.toFloat() else height.toFloat() - bundlePriceWidget.height
        isMoving = false
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
                isMoving = true
            }

            override fun onAnimationEnd(animator: Animator) {
                isMoving = false
            }
        })
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { anim ->
            bundlePriceWidget.animateBundleWidget((translationY / distanceMax), false)
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
        bundlePriceWidget.viewModel = BundlePriceViewModel(context)
        bundlePriceWidget.bundleChevron.visibility = View.VISIBLE
        bundleOverViewWidget.setPadding(0, Ui.getToolbarSize(context), 0, 0)
        val icon = ContextCompat.getDrawable(context, R.drawable.read_more).mutate()
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    }
}
