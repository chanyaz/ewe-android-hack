package com.expedia.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.content.ContextCompat
import android.transition.ChangeBounds
import com.expedia.bookings.R
import com.expedia.bookings.dagger.HotelComponentInjector
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.deeplink.HotelLandingPage
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.utils.AddToCalendarUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Ui
import com.expedia.util.PermissionsUtils.requestLocationPermission
import com.google.android.gms.maps.MapView
import com.google.android.gms.wallet.*
import rx.subjects.PublishSubject

class HotelActivity : AbstractAppCompatActivity() {
    val hotelPresenter: HotelPresenter by lazy {
        findViewById(R.id.hotel_presenter) as HotelPresenter
    }

    val resultsMapView: MapView by lazy {
        hotelPresenter.findViewById<MapView>(R.id.map_view)
    }

    val detailsMapView: MapView by lazy {
        hotelPresenter.findViewById<MapView>(R.id.details_map_view)
    }

    val hotelComponentInjector = HotelComponentInjector()
    var mPaymentsClient: PaymentsClient? = null
    val paymentDataObserver = PublishSubject.create<PaymentData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hotelComponentInjector.inject(this)
        setContentView(R.layout.activity_hotel)
        Ui.showTransparentStatusBar(this)
        val mapState = savedInstanceState?.getBundle(Constants.HOTELS_MAP_STATE)
        resultsMapView.onCreate(mapState)
        detailsMapView.onCreate(mapState)

        mPaymentsClient = Wallet.getPaymentsClient(this, Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .build())

        if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            val locationPermission = ContextCompat.checkSelfPermission(this.baseContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (locationPermission == PackageManager.PERMISSION_DENIED) {
                requestLocationPermission(this)
            }
            else {
                handleDeepLink(intent)
            }
        } else {
            hotelPresenter.setDefaultTransition(Screen.SEARCH)
        }

        setUpAnimations()
    }

    private fun handleDeepLink(intent: Intent) {
        val searchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(intent.getStringExtra(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS))
        if (intent.hasExtra(Codes.MEMBER_ONLY_DEALS) && searchParams != null) {
            searchParams.sortType = HotelSearchParams.SortType.MOBILE_DEALS.sortName
            searchParams.shopWithPoints = false
        }
        val landingPage = intent.getStringExtra(HotelExtras.LANDING_PAGE)
        hotelPresenter.handleDeepLink(searchParams, HotelLandingPage.fromId(landingPage))
    }

    override fun onBackPressed() {
        if (!hotelPresenter.back()) {
            Db.setTemporarilySavedCard(null)
            super.onBackPressed()
        }
    }

    override fun onPause() {
        resultsMapView.onPause()
        detailsMapView.onPause()
        super.onPause()

        if (isFinishing) {
            clearCCNumber()
            clearStoredCard()
        }
        else {
            Ui.hideKeyboard(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            handleDeepLink(intent)
        }
    }

    override fun onResume() {
        resultsMapView.onResume()
        detailsMapView.onResume()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AddToCalendarUtils.requestCodeAddCheckInToCalendarActivity -> {
                // show add to calendar for checkOut date
                hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.showAddToCalendarIntent(false, this)
            }
            Constants.LOAD_PAYMENT_DATA_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val paymentData = PaymentData.getFromIntent(data!!)
                    paymentDataObserver.onNext(paymentData)
//                    val token = paymentData!!.paymentMethodToken.token
                }
                Activity.RESULT_CANCELED -> {
                }
                AutoResolveHelper.RESULT_ERROR -> {
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                }// Log the status for debugging.
            // Generally, there is no need to show an error to
            // the user as the Google Payment API will do that.
            // Do nothing.
            }// Do nothing.
        }
    }

    override fun onDestroy() {
        hotelPresenter.searchPresenter.shopWithPointsWidget.subscription.unsubscribe()
        hotelPresenter.searchPresenter.shopWithPointsWidget.shopWithPointsViewModel.subscription.unsubscribe()
        resultsMapView.onDestroy()
        detailsMapView.onDestroy()
        hotelComponentInjector.clear(this)
        super.onDestroy()
    }

    override fun onLowMemory() {
        resultsMapView.onLowMemory()
        detailsMapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        val mapState = Bundle()
        resultsMapView.onSaveInstanceState(mapState)
        detailsMapView.onSaveInstanceState(mapState)
        outState!!.putBundle(Constants.HOTELS_MAP_STATE, mapState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleDeepLink(intent)
    }

    // Showing different presenter based on deeplink
    enum class Screen {
        SEARCH,
        DETAILS,
        RESULTS
    }

    private fun setUpAnimations() {
        val res = this.resources
        val sharedEnterTransition = ChangeBounds()
        sharedEnterTransition.duration = res.getInteger(R.integer.pro_wizard_shared_enter_duration).toLong()
        window.sharedElementEnterTransition = sharedEnterTransition

        val sharedReturnTransition = ChangeBounds()
        sharedReturnTransition.duration = res.getInteger(R.integer.pro_wizard_shared_return_duration).toLong()
        window.sharedElementReturnTransition = sharedReturnTransition

    }

    fun getPaymentClient() : PaymentsClient {
        return mPaymentsClient!!
    }
}

