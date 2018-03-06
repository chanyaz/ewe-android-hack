package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelReviewsDataProvider
import com.expedia.bookings.presenter.hotel.HotelReviewsView
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.HotelReviewsViewModel
import javax.inject.Inject

class HotelReviewsActivity : AppCompatActivity() {

    lateinit var reviewServices: ReviewsServices
        @Inject set

    lateinit var hotelReviewsDataProvider: HotelReviewsDataProvider
        @Inject set

    private val reviewsWidget by bindView<HotelReviewsView>(R.id.hotel_reviews_widget)

    private val slidingTabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            HotelTracking.trackHotelReviewsCategories(tab.position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.hotel_reviews_activity)
        Ui.getApplication(this).hotelComponent().inject(this)
        Ui.showTransparentStatusBar(this)

        initReviewsView()
        hotelReviewsDataProvider.hotelOffersResponse?.let { offer ->
            reviewsWidget.viewModel.hotelOfferObserver.onNext(offer)
        }
    }

    override fun onStop() {
        super.onStop()
        reviewsWidget.hotelReviewsTabbar.slidingTabLayout.removeOnTabSelectedListener(slidingTabListener)
    }

    override fun onResume() {
        reviewsWidget.endTransition(true)
        super.onResume()
    }

    private fun initReviewsView() {
        reviewsWidget.viewModel = HotelReviewsViewModel(this, LineOfBusiness.HOTELS)
        reviewsWidget.viewModel.setTrackPageLoad(intent.getBooleanExtra(HotelExtras.EXTRA_HOTEL_TRACK_REVIEWS, true))
        reviewsWidget.hotelReviewsTabbar.slidingTabLayout.addOnTabSelectedListener(slidingTabListener)
        reviewsWidget.reviewServices = reviewServices
    }

    //TODO track hotel details on from Reviews to hotel infosite
}
