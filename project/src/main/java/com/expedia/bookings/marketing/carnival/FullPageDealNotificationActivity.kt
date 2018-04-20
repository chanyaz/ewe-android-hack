package com.expedia.bookings.marketing.carnival

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.marketing.carnival.model.CarnivalMessage
import com.expedia.bookings.marketing.carnival.view.FullPageDealScrollContentWidget
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.squareup.picasso.Picasso

class FullPageDealNotificationActivity : AppCompatActivity() {

    private val collapsingToolbarLayout: CollapsingToolbarLayout by bindView(R.id.collapsing_toolbar)
    private val scrollView: NestedScrollView by bindView(R.id.scroll_view)
    private val scrollContentWidget: FullPageDealScrollContentWidget by bindView(R.id.scroll_content)
    private val toolbar: android.support.v7.widget.Toolbar by bindView(R.id.toolbar)
    private val imageView: ImageView by bindView(R.id.in_app_dialog_image)
    lateinit var navIcon: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_page_deal_notification)

        val fullPageDealViewModel = createViewModelWithCarnivalData()

        setSupportActionBar(toolbar)
        makeNavIcon()
        setupToolbar(fullPageDealViewModel.dealTitle)
        setupCollapsingToolbarLayout(fullPageDealViewModel.imageUrl)
        loadImage(fullPageDealViewModel.imageUrl)
    }

    private fun createViewModelWithCarnivalData(): FullPageDealViewModel {
        val carnivalMessage = intent.getParcelableExtra(Constants.CARNIVAL_MESSAGE_DATA) as CarnivalMessage
        return FullPageDealViewModel(carnivalMessage)
    }

    private fun makeNavIcon() {
        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(this@FullPageDealNotificationActivity, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    }

    private fun loadImage(imageUrl: String?) {
        if (imageUrl != null) {
            Picasso.with(this).load(Uri.parse(imageUrl)).into(imageView)
        }
    }

    private fun setupToolbar(dealTitle: String?) {
        toolbar.title = dealTitle ?: ""
        toolbar.navigationIcon = ContextCompat.getDrawable(this@FullPageDealNotificationActivity, R.drawable.ic_close_white_24dp)
        toolbar.navigationContentDescription = this@FullPageDealNotificationActivity.getString(R.string.full_page_deal_toolbar_nav_icon_close_cont_desc)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCollapsingToolbarLayout(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            handleCollapsingToolbarWithImage()
        } else {
            handleCollapsingToolbarWithoutImage()
        }
    }

    private fun handleCollapsingToolbarWithoutImage() {
        toolbar.setBackgroundColor(ContextCompat.getColor(this@FullPageDealNotificationActivity, R.color.brand_primary))
        window.statusBarColor = ContextCompat.getColor(this@FullPageDealNotificationActivity, R.color.brand_primary_dark)
        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ -> showTitleInToolbarOnScrollPastTitleView(scrollY) })
    }

    private fun handleCollapsingToolbarWithImage() {
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this@FullPageDealNotificationActivity, android.R.color.transparent))
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this@FullPageDealNotificationActivity, R.color.brand_primary))
        collapsingToolbarLayout.setStatusBarScrimColor(ContextCompat.getColor(this@FullPageDealNotificationActivity, R.color.brand_primary_dark))
    }

    private fun showTitleInToolbarOnScrollPastTitleView(scrollY: Int) {
        collapsingToolbarLayout.isTitleEnabled = scrollY <= scrollContentWidget.titleView.bottom
    }
}
