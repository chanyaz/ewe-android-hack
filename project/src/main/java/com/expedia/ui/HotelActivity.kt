package com.expedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.PaymentWidget
import com.google.android.gms.maps.MapView
import com.mobiata.android.Log
import kotlin.properties.Delegates

public class HotelActivity : AppCompatActivity() {

    val hotelPresenter: HotelPresenter by Delegates.lazy {
        findViewById(R.id.hotel_presenter) as HotelPresenter
    }

    val mapView: MapView by Delegates.lazy {
        hotelPresenter.findViewById(R.id.widget_hotel_results).findViewById(R.id.map_view) as MapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultHotelComponents()
        setContentView(R.layout.activity_hotel)
        Ui.showTransparentStatusBar(this)
        mapView.onCreate(savedInstanceState)
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

}

