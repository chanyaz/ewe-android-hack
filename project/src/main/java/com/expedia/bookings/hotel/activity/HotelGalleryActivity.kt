package com.expedia.bookings.hotel.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.expedia.bookings.R
import com.expedia.bookings.hotel.data.HotelGalleryConfig
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelGalleryManager
import com.expedia.bookings.hotel.widget.HotelDetailGalleryView
import com.expedia.bookings.hotel.widget.HotelGalleryToolbar
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.HotelInfoToolbarViewModel

class HotelGalleryActivity : AppCompatActivity() {
    private val toolbar by bindView<HotelGalleryToolbar>(R.id.hotel_gallery_toolbar)
    private val galleryView by bindView<HotelDetailGalleryView>(R.id.fullscreen_hotel_gallery)

    private lateinit var galleryManager: HotelGalleryManager
    private lateinit var galleryConfig: HotelGalleryConfig
    private var scrollPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_gallery_activity)

        galleryManager = Ui.getApplication(this).appComponent().hotelGalleryManager()
        galleryConfig = intent.getParcelableExtra<HotelGalleryConfig>(HotelExtras.GALLERY_CONFIG)

        initToolbar()

        if (savedInstanceState != null) {
            scrollPosition = savedInstanceState.getInt(HotelExtras.GALLERY_SCROLL_POSITION)
        } else {
            scrollPosition = galleryConfig.startIndex
        }
    }

    override fun onStart() {
        super.onStart()

        val galleryItems = galleryManager.fetchMediaList(galleryConfig.roomCode)
        if (galleryItems.isEmpty()) {
            // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/8612
            finish()
        } else {
            galleryView.setGalleryItems(galleryManager.fetchMediaList(galleryConfig.roomCode))
            galleryView.scrollTo(scrollPosition)
            if (galleryConfig.showDescription) {
                galleryView.expand()
            } else {
                galleryView.collapse()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(HotelExtras.GALLERY_SCROLL_POSITION, galleryView.getCurrentIndex())
    }

    private fun initToolbar() {
        val viewModel = HotelInfoToolbarViewModel(this)
        viewModel.bind(galleryConfig.hotelName, galleryConfig.hotelStarRating, soldOut = false)
        toolbar.setViewModel(viewModel)
        toolbar.navClickedSubject.subscribe {
            onBackPressed()
        }
    }
}
