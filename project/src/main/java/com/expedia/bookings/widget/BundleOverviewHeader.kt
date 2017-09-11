package com.expedia.bookings.widget

import android.content.Context
import android.support.annotation.ColorRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.LayoutUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isSecureIconEnabled
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.packages.CheckoutOverviewHeader
import com.expedia.util.subscribeText
import com.expedia.vm.CheckoutToolbarViewModel
import com.larvalabs.svgandroid.widget.SVGView

class BundleOverviewHeader(context : Context, attrs : AttributeSet) : CoordinatorLayout(context, attrs), AppBarLayout.OnOffsetChangedListener  {
    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val collapsingToolbarLayout: CollapsingToolbarLayout by bindView(R.id.collapsing_toolbar)
    val appBarLayout: AppBarLayout by bindView(R.id.app_bar)
    val imageHeader: ImageView by bindView(R.id.overview_image)
    val checkoutOverviewHeaderToolbar: CheckoutOverviewHeader by bindView(R.id.checkout_overview_header_toolbar)
    val nestedScrollView: NestedScrollView by bindView(R.id.nested_scrollview)
    val checkoutOverviewFloatingToolbar: CheckoutOverviewHeader by bindView(R.id.checkout_overview_floating_toolbar)
    val secureIcon: SVGView by bindView(R.id.secure_lock_icon)
    var customTitle: TextView? = null
    val isSecureIconActive = isSecureIconEnabled(context)

    var isHideToolbarView = false
    var isDisabled = false
    var isFullyExpanded = true
    var isExpandable = true
    @ColorRes val primaryColorId = Ui.obtainThemeResID(context, R.attr.primary_color)

    init {
        View.inflate(context, R.layout.bundle_overview_header, this)
        toolbar.viewModel = CheckoutToolbarViewModel(context)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(context, R.style.ToolbarSubtitleTextAppearance)
        checkoutOverviewHeaderToolbar.checkoutHeaderImage = imageHeader
        checkoutOverviewFloatingToolbar.checkoutHeaderImage = imageHeader
        toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_cont_desc)
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
            toolbar.viewModel.menuVisibility.onNext(false)
        }
        checkoutOverviewFloatingToolbar.destinationText.ellipsize = null
        checkoutOverviewFloatingToolbar.destinationText.setSingleLine(false)
        checkoutOverviewFloatingToolbar.destinationText.maxLines = 2
        if (isSecureIconActive) {
            LayoutUtils.setSVG(secureIcon, R.raw.lock_icon)
            customTitle = findViewById<TextView>(R.id.checkout_custom_title)
            toolbar.viewModel.toolbarCustomTitle.subscribeText(customTitle)
        }
    }

    /** Collapsing Toolbar **/
    fun setUpCollapsingToolbar() {
        //we need to set this empty space inorder to remove the title string
        collapsingToolbarLayout.title = " "
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(context, primaryColorId))
        collapsingToolbarLayout.setStatusBarScrimColor(ContextCompat.getColor(context, primaryColorId))

        checkoutOverviewHeaderToolbar.travelers.visibility = View.GONE
        appBarLayout.addOnOffsetChangedListener(this)
        val floatingToolbarLayoutParams = checkoutOverviewFloatingToolbar.destinationText.layoutParams as LinearLayout.LayoutParams
        floatingToolbarLayoutParams.gravity = Gravity.CENTER
        toggleOverviewHeader(false)

        (checkoutOverviewHeaderToolbar.destinationText.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.LEFT
        (checkoutOverviewHeaderToolbar.checkInOutDates.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.LEFT
        checkoutOverviewHeaderToolbar.destinationText.pivotX = 0f
        checkoutOverviewHeaderToolbar.destinationText.pivotY = 0f
        checkoutOverviewHeaderToolbar.destinationText.scaleX = .75f
        checkoutOverviewHeaderToolbar.destinationText.scaleY = .75f
    }

    fun translateDatesTitleForHeaderToolbar() {
        val destinationTextView = checkoutOverviewFloatingToolbar.destinationText
        if (Strings.isNotEmpty(destinationTextView.text) && destinationTextView.lineCount > 0) {
            checkoutOverviewHeaderToolbar.checkInOutDates.translationY = -((destinationTextView.height
                    / destinationTextView.lineCount) * .25f)
        }
    }

    fun toggleOverviewHeader(show: Boolean) {
        toolbar.setBackgroundColor(ContextCompat.getColor(context, if (show) android.R.color.transparent else primaryColorId))
        appBarLayout.setExpanded(show)
        toggleCollapsingToolBar(show)
    }

    fun toggleCollapsingToolBar(enable: Boolean) {
        checkoutOverviewFloatingToolbar.visibility = if (enable && isExpandable) View.VISIBLE else View.GONE
        appBarLayout.isActivated = enable
        nestedScrollView.isNestedScrollingEnabled = enable
        collapsingToolbarLayout.isTitleEnabled = enable
    }

    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean {
        return isExpandable && super.onStartNestedScroll(child, target, nestedScrollAxes)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        if (maxScroll != 0) {
            val percentage = Math.abs(offset) / maxScroll.toFloat()
            isFullyExpanded = percentage == 0f

            if (isHideToolbarView) {
                if (percentage == 1f) {
                    if (!isDisabled) {
                        checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                    }
                    checkoutOverviewHeaderToolbar.destinationText.visibility = View.VISIBLE
                    checkoutOverviewHeaderToolbar.checkInOutDates.alpha = 1f
                    translateDatesTitleForHeaderToolbar()
                    isHideToolbarView = !isHideToolbarView
                } else if (percentage >= .7f) {
                    checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                    val alpha = (percentage - .7f) / .3f
                    checkoutOverviewHeaderToolbar.checkInOutDates.alpha = alpha
                    translateDatesTitleForHeaderToolbar()
                } else {
                    checkoutOverviewHeaderToolbar.visibility = View.GONE
                    checkoutOverviewHeaderToolbar.checkInOutDates.alpha = 0f
                }
            } else {
                if (percentage < 1f) {
                    checkoutOverviewHeaderToolbar.destinationText.visibility = View.INVISIBLE
                    isHideToolbarView = !isHideToolbarView
                }
            }
        }
    }
}
