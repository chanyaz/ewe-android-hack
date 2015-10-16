package com.expedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.vm.HotelTravelerParams
import com.google.android.gms.maps.MapView
import com.mobiata.android.Log

public class HotelActivity : AppCompatActivity() {

    val hotelPresenter: HotelPresenter by lazy {
        findViewById(R.id.hotel_presenter) as HotelPresenter
    }

    val mapView: MapView by lazy {
        hotelPresenter.findViewById(R.id.widget_hotel_results).findViewById(R.id.map_view) as MapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultHotelComponents()
        setContentView(R.layout.activity_hotel)
        Ui.showTransparentStatusBar(this)
        mapView.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            OmnitureTracking.trackHotelsABTest()
        }

        if (getIntent().hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            handleNavigationViaDeepLink()
        }
    }

    override fun onBackPressed() {
        if (!hotelPresenter.back()) {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()

        if (isFinishing()) {
            clearCCNumber()
        }
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PaymentWidget.REQUEST_CODE_GOOGLE_WALLET_ACTIVITY -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (Db.getBillingInfo() != null) {
                        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.bind(Db.getBillingInfo())
                        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.setExpanded(false)
                        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.mainContactInfoCardView.bindGoogleWalletTraveler(Db.getGoogleWalletTraveler())
                        hotelPresenter.checkoutPresenter.hotelCheckoutWidget.mainContactInfoCardView.setExpanded(false)
                    }
                    return
                }
            }
        }
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    public fun clearCCNumber() {
        try {
            Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setNumber(null)
            Db.getBillingInfo().setNumber(null)
        } catch (ex: Exception) {
            Log.e("Error clearing billingInfo card number", ex)
        }

    }

    fun handleNavigationViaDeepLink() {
        val hotelSearchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(getIntent().getStringExtra("hotelSearchParams"))
        val isCurrentLocationSearch = "MY_LOCATION".equals(hotelSearchParams?.suggestion?.type)
        if (isCurrentLocationSearch) {
            hotelSearchParams?.suggestion?.regionNames?.displayName = resources.getString(R.string.current_location)
            hotelSearchParams?.suggestion?.regionNames?.shortName = resources.getString(R.string.current_location)
        }
        hotelPresenter.searchPresenter.searchViewModel.suggestionObserver.onNext(hotelSearchParams?.suggestion)
        hotelPresenter.searchPresenter.searchViewModel.enableDateObserver.onNext(Unit)
        hotelPresenter.searchPresenter.traveler.viewmodel.travelerParamsObservable.onNext(HotelTravelerParams(hotelSearchParams?.adults ?: 1, hotelSearchParams?.children ?: emptyList()))
        val dates = Pair (hotelSearchParams?.checkIn, hotelSearchParams?.checkOut)
        hotelPresenter.searchPresenter.searchViewModel.datesObserver.onNext(dates)
        hotelPresenter.searchPresenter.calendar.setSelectedDates(hotelSearchParams?.checkIn, hotelSearchParams?.checkOut)
        if (isCurrentLocationSearch || "HOTEL".equals(hotelSearchParams?.suggestion?.type)) {
            hotelPresenter.searchObserver.onNext(hotelSearchParams)
        }
    }

}

