package com.expedia.bookings.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.extensions.subscribeInverseVisibility
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseTotalPriceWidgetViewModel
import java.math.BigDecimal

class TotalPriceWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val ANIMATION_DURATION = 300L
    private val ANIMATION_DELAY = 500L

    val bundleChevron: ImageView by bindView(R.id.bundle_chevron)
    val bundleTotalPrice: TextView by bindView(R.id.bundle_total_price)
    val bundleReferenceTotalPrice: TextView by bindView(R.id.bundle_reference_total_price)
    val bundleTotalIncludes: TextView by bindView(R.id.bundle_total_includes_text)
    val bundleSavings: TextView by bindView(R.id.bundle_total_savings)
    val bundleTotalText: TextView by bindView(R.id.bundle_total_text)
    val perPersonText: TextView by bindView(R.id.per_person_text)
    val bundleTitle: TextView by bindView(R.id.bundle_title)
    val bundleSubtitle: TextView by bindView(R.id.bundle_subtitle)
    val priceProgressBar: ProgressBar by bindView(R.id.total_price_progress)
    val closeIcon: ImageView by bindView(R.id.bundle_close)

    val betterSavingContainer: View by bindView(R.id.better_saving_container)
    val betterSavingView: TextView by bindView(R.id.better_saving_view)
    val priceAndSavingContainer: View by bindView(R.id.price_and_saving_container)

    val eval: ArgbEvaluator = ArgbEvaluator()
    val titleTextFade = TransitionElement(ContextCompat.getColor(context, R.color.packages_bundle_overview_footer_primary_text), Color.WHITE)
    val subtitleTextFade = TransitionElement(ContextCompat.getColor(context, R.color.packages_bundle_overview_footer_secondary_text), Color.WHITE)
    val bgFade = TransitionElement(Color.WHITE, ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))

    private val resizeOpenAnimator: ResizeHeightAnimator by lazy {
        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        resizeAnimator.startDelay = ANIMATION_DELAY
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        betterSavingContainer.measure(widthMeasureSpec, heightMeasureSpec)
        resizeAnimator.addViewSpec(betterSavingContainer, betterSavingContainer.measuredHeight)
        resizeAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                betterSavingContainer.visibility = View.VISIBLE
            }
        })
        resizeAnimator
    }

    var viewModel: BaseTotalPriceWidgetViewModel by notNullAndObservable { vm ->
        vm.totalPriceObservable.subscribeTextAndVisibility(bundleTotalPrice)
        vm.pricePerPersonObservable.subscribeText(bundleTotalPrice)
        vm.savingsPriceObservable.subscribeTextAndVisibility(bundleSavings)
        vm.savings.map { it.formattedMoneyFromAmountAndCurrencyCode }.subscribeText(betterSavingView)
        vm.betterSavingsObservable.subscribeVisibility(bundleReferenceTotalPrice)
        vm.referenceTotalPrice.map { it.formattedMoneyFromAmountAndCurrencyCode }.subscribeText(bundleReferenceTotalPrice)
        vm.bundleTextLabelObservable.subscribeText(bundleTotalText)
        vm.perPersonTextLabelObservable.subscribeVisibility(perPersonText)
        vm.bundleTotalIncludesObservable.subscribeTextAndVisibility(bundleTotalIncludes)
        vm.contentDescriptionObservable.subscribe { description ->
            this.contentDescription = description
        }
        vm.priceAvailableObservable.subscribeInverseVisibility(priceProgressBar)
        vm.betterSavingsObservable.subscribe {
            if (it) {
                resizeOpenAnimator.start()
            } else {
                betterSavingContainer.setVisibility(false)
            }
        }
        vm.totalPriceContainerDescription.subscribe { it ->
            this.contentDescription = it
        }
        if (!vm.isSlidable) {
            setupClickListeners()
        }
    }

    val breakdown = CostSummaryBreakDownView(context, null)
    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setView(breakdown)
        builder.setTitle(R.string.price_summary)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, _ -> dialog.dismiss() })
        builder.create()
    }

    init {
        View.inflate(getContext(), R.layout.bundle_total_price_widget, this)
        orientation = VERTICAL
        rotateChevron(true)
    }

    private fun setupClickListeners() {
        betterSavingContainer.setOnClickListener {
            setupPriceWidgetEventAndShowCostSummary(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_STRIP_CLICK)
        }
        betterSavingView.setOnClickListener {
            setupPriceWidgetEventAndShowCostSummary(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_BUTTON_CLICK)
        }
        bundleTotalText.setOnClickListener {
            setupPriceWidgetEventAndShowCostSummary(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.INFO_ICON_CLICK)
        }
        bundleTotalIncludes.setOnClickListener {
            setupPriceWidgetEventAndShowCostSummary(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.INFO_ICON_CLICK)
        }
        priceAndSavingContainer.setOnClickListener {
            setupPriceWidgetEventAndShowCostSummary(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.BUNDLE_PRICE_CLICK)
        }
        setOnClickListener {
            setupPriceWidgetEventAndShowCostSummary(BaseTotalPriceWidgetViewModel.PriceWidgetEvent.BUNDLE_WIDGET_CLICK)
        }
    }

    private fun setupPriceWidgetEventAndShowCostSummary(event: BaseTotalPriceWidgetViewModel.PriceWidgetEvent) {
        viewModel.priceWidgetClick.onNext(event)

        // We want to show cost breakdown ONLY in checkout screen. We set the rightDrawable only when createTrip returns. So let's check
        if (bundleTotalText.compoundDrawables[2] != null) {
            dialog.show()
            breakdown.viewmodel.trackBreakDownClicked()
        }
    }

    fun rotateChevron(isCollapsed: Boolean) {
        if (isCollapsed) {
            AnimUtils.rotate(bundleChevron)
        } else {
            AnimUtils.reverseRotate(bundleChevron)
        }
    }

    fun toggleBundleTotalCompoundDrawable(show: Boolean) {
        if (show) {
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_checkout_info).mutate()
            bundleTotalText.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
        } else {
            bundleTotalText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    fun resetPriceWidget() {
        this.visibility = View.VISIBLE

        val countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
        val currencyCode = CurrencyUtils.currencyForLocale(countryCode)
        viewModel.total.onNext(Money(BigDecimal("0.00"), currencyCode))
        viewModel.savings.onNext(Money(BigDecimal("0.00"), currencyCode))
        viewModel.shouldShowSavings.onNext(false)
        viewModel.betterSavingsObservable.onNext(false)
        if (viewModel.shouldShowTotalPriceLoadingProgress() && AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightRateDetailsFromCache)) {
            viewModel.priceAvailableObservable.onNext(false)
        }
        toggleBundleTotalCompoundDrawable(false)
        viewModel.savingsPriceObservable.onNext("")
    }

    fun animateBundleWidget(f: Float, forward: Boolean) {
        val progress = if (forward) f else 1 - f
        val alpha = if (forward) (1 - f) else f
        setBackgroundColor(eval.evaluate(progress, bgFade.start, bgFade.end) as Int)
        val titleTextColor = eval.evaluate(progress, titleTextFade.start, titleTextFade.end) as Int
        val subTitleTextColor = eval.evaluate(progress, subtitleTextFade.start, subtitleTextFade.end) as Int
        bundleTotalText.setTextColor(titleTextColor)
        bundleTotalIncludes.setTextColor(subTitleTextColor)
        bundleTitle.setTextColor(titleTextColor)
        bundleSubtitle.setTextColor(subTitleTextColor)
        bundleChevron.drawable.setColorFilter(titleTextColor, PorterDuff.Mode.SRC_IN)
        bundleTotalPrice.alpha = alpha
        bundleSavings.alpha = alpha
        perPersonText.alpha = alpha
        bundleTotalText.alpha = alpha
        bundleTotalIncludes.alpha = alpha
        bundleTitle.alpha = progress
        bundleSubtitle.alpha = progress
        bundleChevron.pivotX = bundleChevron.width / 2f
        bundleChevron.pivotY = bundleChevron.height / 2f
        bundleChevron.rotation = if (forward) f * 180 else (1 - f) * 180
    }

    fun disable() {
        bundleChevron.alpha = .5f
        bundleTotalPrice.alpha = .5f
        bundleTotalIncludes.alpha = .5f
        bundleSavings.alpha = .5f
        bundleTotalText.alpha = .5f
        perPersonText.alpha = .5f
        bundleTitle.alpha = .5f
        bundleSubtitle.alpha = .5f
    }

    fun enable() {
        bundleChevron.alpha = 1f
        bundleTotalPrice.alpha = 1f
        bundleTotalIncludes.alpha = 1f
        bundleSavings.alpha = 1f
        bundleTotalText.alpha = 1f
        perPersonText.alpha = 1f
        bundleTitle.alpha = 1f
        bundleSubtitle.alpha = 1f
    }
}
