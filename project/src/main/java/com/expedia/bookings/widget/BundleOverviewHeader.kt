package com.expedia.bookings.widget

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.packages.CheckoutOverviewHeader
import com.expedia.vm.CheckoutToolbarViewModel

class BundleOverviewHeader(context : Context, attrs : AttributeSet) : CoordinatorLayout(context, attrs), AppBarLayout.OnOffsetChangedListener  {
    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val collapsingToolbarLayout: CollapsingToolbarLayout by bindView(R.id.collapsing_toolbar)
    val coordinatorLayout: CoordinatorLayout by bindView(R.id.coordinator_layout)
    val appBarLayout: AppBarLayout by bindView(R.id.app_bar)
    val imageHeader: ImageView by bindView(R.id.overview_image)
    val checkoutOverviewHeaderToolbar: CheckoutOverviewHeader by bindView(R.id.checkout_overview_header_toolbar)
    val nestedScrollView: NestedScrollView by bindView(R.id.nested_scrollview)
    val checkoutOverviewFloatingToolbar: CheckoutOverviewHeader by bindView(R.id.checkout_overview_floating_toolbar)

    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)
    var isHideToolbarView = false;
    var isDisabled = false;

    init {
        View.inflate(context, R.layout.bundle_overview_header, this)
        toolbar.viewModel = CheckoutToolbarViewModel(context)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(context, R.style.ToolbarSubtitleTextAppearance)
        checkoutOverviewHeaderToolbar.checkoutHeaderImage = imageHeader
        checkoutOverviewFloatingToolbar.checkoutHeaderImage = imageHeader

        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }
    }

    /** Collapsing Toolbar **/
    fun setUpCollapsingToolbar() {
        //we need to set this empty space inorder to remove the title string
        collapsingToolbarLayout.title = " ";
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        collapsingToolbarLayout.setStatusBarScrimColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        checkoutOverviewHeaderToolbar.travelers.visibility = View.GONE
        appBarLayout.addOnOffsetChangedListener(this);
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

    fun toggleOverviewHeader(show: Boolean) {
        toolbar.setBackgroundColor(ContextCompat.getColor(context, if (show) android.R.color.transparent else R.color.packages_primary_color))
        appBarLayout.setExpanded(show)
        toggleCollapsingToolBar(show)
        swapViews(show)
    }

    fun toggleCollapsingToolBar(enable: Boolean) {
        checkoutOverviewFloatingToolbar.visibility = if (enable) View.VISIBLE else View.GONE
        appBarLayout.isActivated = enable
        nestedScrollView.isNestedScrollingEnabled = enable
        collapsingToolbarLayout.isTitleEnabled = enable
    }

    /** Swaps the bundle widget out of the coordinator
     * layout into the main layout to address scrolling/animation bugs**/
    fun swapViews(toCoordinatorLayout: Boolean) {
        val parent = nestedScrollView.parent  as ViewGroup
        parent.removeView(nestedScrollView)
        if (toCoordinatorLayout) {
            removeView(nestedScrollView)
            coordinatorLayout.addView(nestedScrollView, 2)
            val bundleWidgetLayoutParams = nestedScrollView.layoutParams as CoordinatorLayout.LayoutParams
            bundleWidgetLayoutParams.behavior = AppBarLayout.ScrollingViewBehavior();
            bundleWidgetLayoutParams.gravity = Gravity.FILL_VERTICAL
            nestedScrollView.setPadding(0, 0, 0, 0)
            nestedScrollView.isFillViewport = true
        } else {
            val root = this.parent as ViewGroup
            coordinatorLayout.removeView(nestedScrollView)
            root.addView(nestedScrollView, 1)
            nestedScrollView.setPadding(0, toolbarHeight, 0, 0)
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        var maxScroll = appBarLayout.totalScrollRange;
        if (maxScroll != 0) {
            var percentage = Math.abs(offset) / maxScroll.toFloat()

            if (isHideToolbarView) {
                if (percentage == 1f) {
                    if (!isDisabled) {
                        checkoutOverviewHeaderToolbar.visibility = View.VISIBLE;
                    }
                    checkoutOverviewHeaderToolbar.destinationText.visibility = View.VISIBLE
                    checkoutOverviewHeaderToolbar.checkInOutDates.alpha = 1f
                    val distance = checkoutOverviewFloatingToolbar.destinationText.height * .25f
                    checkoutOverviewHeaderToolbar.checkInOutDates.translationY = -distance
                    isHideToolbarView = !isHideToolbarView;
                } else if (percentage >= .7f) {
                    checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                    val alpha = (percentage - .7f) / .3f
                    checkoutOverviewHeaderToolbar.checkInOutDates.alpha = alpha
                    val distance = checkoutOverviewFloatingToolbar.destinationText.height * .25f
                    checkoutOverviewHeaderToolbar.checkInOutDates.translationY = -distance
                } else {
                    checkoutOverviewHeaderToolbar.visibility = View.GONE
                    checkoutOverviewHeaderToolbar.checkInOutDates.alpha = 0f
                }
            } else {
                if (percentage < 1f) {
                    checkoutOverviewHeaderToolbar.destinationText.visibility = View.INVISIBLE;
                    isHideToolbarView = !isHideToolbarView;
                }
            }
        }
    }
}
