package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.common.ItinViewReceiptWidget
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepo
import com.expedia.bookings.itin.scopes.HotelItinPricingSummaryScope
import com.expedia.bookings.itin.scopes.HotelItinRewardsScope
import com.expedia.bookings.itin.scopes.HotelItinToolbarScope
import com.expedia.bookings.itin.scopes.HotelItinViewReceiptScope
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.FeatureProvider
import com.expedia.bookings.itin.utils.FeatureSource
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.itin.utils.WebViewLauncher
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import javax.inject.Inject

class HotelItinPricingRewardsActivity : AppCompatActivity() {

    companion object : Intentable {
        private const val ID_EXTRA = "ITINID"

        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinPricingRewardsActivity::class.java)
            i.putExtra(ID_EXTRA, id)
            return i
        }
    }

    val toolbar: ItinToolbar by bindView(R.id.widget_itin_toolbar)
    val pricingSummaryView: HotelItinPricingSummaryView by bindView(R.id.hotel_itin_pricing_summary_view)
    val receiptButton: ItinViewReceiptWidget by bindView(R.id.widget_itin_view_receipt)
    val rewardsView: HotelItinPricingSummaryRewardsView by bindView(R.id.hotel_itin_pricing_summary_rewards_view)

    lateinit var jsonUtil: IJsonToItinUtil
        @Inject set
    lateinit var stringProvider: StringSource
        @Inject set
    lateinit var endpointProvider: EndpointProvider
        @Inject set

    lateinit var hotelRepo: ItinHotelRepo
    lateinit var activityLauncher: IActivityLauncher
    lateinit var webViewLauncher: IWebViewLauncher

    var toolbarViewModel: HotelItinPricingRewardsToolbarViewModel<HotelItinToolbarScope> by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finish()
        }
    }
    lateinit var summaryViewModel: HotelItinPricingSummaryViewModel<HotelItinPricingSummaryScope>
    lateinit var rewardsViewModel: HotelItinPricingSummaryRewardsViewModel<HotelItinRewardsScope>

    val itineraryManager: ItineraryManager = ItineraryManager.getInstance()
    var tripsTracking: ITripsTracking = TripsTracking
    var features: FeatureSource = FeatureProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_itin_pricing_reward)
        Ui.getApplication(this).defaultTripComponents()
        Ui.getApplication(this).tripComponent().inject(this)

        hotelRepo = ItinHotelRepo(intent.getStringExtra(ID_EXTRA), jsonUtil, itineraryManager.syncFinishObservable)
        activityLauncher = ActivityLauncher(this)
        webViewLauncher = WebViewLauncher(this)

        val toolbarScope = HotelItinToolbarScope(stringProvider, hotelRepo, this)
        toolbarViewModel = HotelItinPricingRewardsToolbarViewModel(toolbarScope)
        toolbar.viewModel = toolbarViewModel

        val summaryScope = HotelItinPricingSummaryScope(hotelRepo, stringProvider, activityLauncher, this)
        summaryViewModel = HotelItinPricingSummaryViewModel(summaryScope)
        pricingSummaryView.viewModel = summaryViewModel

        val viewReceiptScope = HotelItinViewReceiptScope(stringProvider, hotelRepo, this, tripsTracking, webViewLauncher, features)
        val viewReceiptViewModel = HotelItinViewReceiptViewModel(viewReceiptScope)
        receiptButton.viewModel = viewReceiptViewModel

        val rewardsScope = HotelItinRewardsScope(stringProvider, hotelRepo, this, tripsTracking, webViewLauncher, endpointProvider.e3EndpointUrl)
        rewardsViewModel = HotelItinPricingSummaryRewardsViewModel(rewardsScope)
        rewardsView.viewModel = rewardsViewModel

        setUpOmnitureValues()

        hotelRepo.liveDataInvalidItin.observe(this, LiveDataObserver {
            finish()
        })
    }

    override fun finish() {
        super.finish()
        hotelRepo.dispose()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }

    fun setUpOmnitureValues() {
        hotelRepo.liveDataItin.observe(this, object : Observer<Itin> {
            override fun onChanged(t: Itin?) {
                t?.let {
                    val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(it, ItinOmnitureUtils.LOB.HOTEL)
                    tripsTracking.trackHotelItinPricingRewardsPageLoad(omnitureValues)
                }
                hotelRepo.liveDataItin.removeObserver(this)
            }
        })
    }
}
