package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.hotel.widget.HotelFavoritesView

class HotelFavoritesActivity : AppCompatActivity() {

    private val toolbar by bindView<Toolbar>(R.id.hotel_favorites_toolbar)
    private val favoritesView by bindView<HotelFavoritesView>(R.id.hotel_favorites_view)
    private val loadingOverlay by bindView<LinearLayout>(R.id.hotel_favorite_loading_overlay)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_favorites_activity_layout)
        setTitle(R.string.hotel_favorites_page_toolbar_title)
        if (intent.hasExtra(Codes.INFOSITE_DEEPLINK_USE_SWP)) {
            favoritesView.setUseShopWithPoint(intent.getBooleanExtra(Codes.INFOSITE_DEEPLINK_USE_SWP, false))
        }

        if (favoritesView.viewModel.isUserAuthenticated()) {
            loadingOverlay.visibility = View.VISIBLE
        }

        favoritesView.viewModel.receivedResponseSubject.subscribe {
            loadingOverlay.visibility = View.GONE
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        favoritesView.onClear()
    }
}
