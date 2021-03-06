package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelReviewsAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelReviewsAdapterViewModel
import com.expedia.vm.HotelReviewsViewModel
import kotlin.properties.Delegates

public class HotelReviewsView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val hotelReviewsToolbar: HotelReviewsToolbar by bindView(R.id.hotel_reviews_toolbar)
    val viewPager: ViewPager by bindView(R.id.viewpager)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val reviewsContainer: LinearLayout by bindView(R.id.reviews_container)

    var viewModel: HotelReviewsViewModel by notNullAndObservable { vm ->
        vm.toolbarTitleObservable.subscribe { hotelName ->
            toolbar.title = hotelName
        }
        vm.toolbarSubtitleObservable.subscribe { subtitle ->
            toolbar.subtitle = subtitle
        }
        vm.hotelReviewsObservable.subscribe() { hotelId ->
            hotelReviewsAdapterViewModel = HotelReviewsAdapterViewModel(hotelId, reviewServices, PointOfSale.getPointOfSale().localeIdentifier)
        }
    }

    val reviewServices: ReviewsServices by lazy() {
        Ui.getApplication(context).hotelComponent().reviewsServices()
    }

    var hotelReviewsAdapterViewModel: HotelReviewsAdapterViewModel by notNullAndObservable { vm ->
        adapter = HotelReviewsAdapter(context, viewPager, vm)
        hotelReviewsToolbar.slidingTabLayout.setViewPager(viewPager)
    }

    var adapter: HotelReviewsAdapter by Delegates.notNull()

    init {
        View.inflate(context, R.layout.widget_hotel_reviews, this)
        //Allow retaining of all the tab views
        viewPager.offscreenPageLimit = Integer.MAX_VALUE
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, reviewsContainer, color)
            addView(statusBar)
        }
        toolbar.setNavigationOnClickListener {
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
    }

    fun transitionFinished() {
        adapter.startDownloads()
    }
}
