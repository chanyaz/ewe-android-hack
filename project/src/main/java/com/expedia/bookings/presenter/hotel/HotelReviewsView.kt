package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.widget.HotelSelectARoomBar
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.hotel.widget.adapter.HotelReviewsAdapter
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelReviewsAdapterViewModel
import com.expedia.vm.HotelReviewsViewModel
import kotlin.properties.Delegates

class HotelReviewsView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    var reviewServices: ReviewsServices by Delegates.notNull()
    val hotelReviewsTabbar: HotelReviewsTabbar by bindView(R.id.hotel_reviews_tabbar)
    val viewPager: ViewPager by bindView(R.id.viewpager)
    val toolbar: Toolbar by bindView(R.id.hotel_reviews_toolbar)
    private val reviewsContainer: LinearLayout by bindView(R.id.reviews_container)
    private val selectARoomBar: HotelSelectARoomBar by bindView(R.id.hotel_reviews_select_a_room_bar)
    private val searchListContainer: LinearLayout by bindView(R.id.hotel_review_search_results_container)

    var viewModel: HotelReviewsViewModel by notNullAndObservable { vm ->
        vm.toolbarTitleObservable.subscribe { hotelName ->
            toolbar.title = hotelName
        }
        vm.toolbarSubtitleObservable.subscribe { subtitle ->
            toolbar.subtitle = subtitle
        }
        vm.hotelIdObservable.subscribe { hotelId ->
            hotelReviewsAdapterViewModel = HotelReviewsAdapterViewModel(hotelId, reviewServices, PointOfSale.getPointOfSale().localeIdentifier, null)
        }
        vm.hotelOfferObservable.subscribe { offer ->
            selectARoomBar.bindRoomOffer(offer)
        }
        vm.soldOutObservable.subscribe { soldOut ->
            val bucketedForSelectARoom = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelReviewSelectRoomCta)
            selectARoomBar.setVisibility(!soldOut && bucketedForSelectARoom)
        }
    }

    var hotelReviewsAdapterViewModel: HotelReviewsAdapterViewModel by notNullAndObservable { vm ->
        adapter = HotelReviewsAdapter(context, viewPager, vm)
        hotelReviewsTabbar.slidingTabLayout.setupWithViewPager(viewPager)
    }

    var adapter: HotelReviewsAdapter by Delegates.notNull()

    init {
        View.inflate(context, R.layout.widget_hotel_reviews, this)
        //Allow retaining of all the tab views
        viewPager.offscreenPageLimit = Integer.MAX_VALUE
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = Ui.obtainThemeColor(context, R.attr.primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, reviewsContainer, color)
            addView(statusBar)
        }
        toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_close_cont_desc)

        toolbar.setNavigationOnClickListener {
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        selectARoomBar.setOnClickListener {
            OmnitureTracking.trackHotelReviewSelectARoomClick()
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
            viewModel.scrollToRoomListener.onNext(Unit)
        }

        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelUGCSearch)) {
            setupSearchView()
        }
    }

    fun endTransition(forward: Boolean) {
        if (forward) {
            adapter.startDownloads()
            viewModel.trackReviewPageLoad()
        }
    }

    fun back(): Boolean {
        return toolbar.menu.size() > 0 && toolbar.menu.getItem(0).collapseActionView()
    }

    private fun setupSearchView() {
        toolbar.inflateMenu(R.menu.hotel_review_menu)
        toolbar.menu.getItem(0).setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                searchListContainer.setVisibility(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.app_primary))
                searchListContainer.setVisibility(false)
                return true
            }
        })

        val searchView = toolbar.menu.getItem(0).actionView as? SearchView
        searchView?.maxWidth = Integer.MAX_VALUE
    }
}
