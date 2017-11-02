package com.expedia.bookings.hotel.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.hotel.data.HotelGalleryParcel
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelGalleryManager
import com.expedia.bookings.hotel.widget.HotelDetailGalleryView
import com.expedia.bookings.hotel.widget.HotelGalleryToolbar
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.HotelInfoToolbarViewModel
import rx.Observer
import java.util.ArrayList

class HotelGalleryActivity : AppCompatActivity() {
    private val toolbar by bindView<HotelGalleryToolbar>(R.id.hotel_gallery_toolbar)
    private val galleryView by bindView<HotelDetailGalleryView>(R.id.fullscreen_hotel_gallery)

    private lateinit var galleryManager: HotelGalleryManager
    private lateinit var galleryParcel: HotelGalleryParcel
    private var scrollPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_gallery_activity)

        galleryManager = Ui.getApplication(this).appComponent().hotelGalleryManager()
        galleryParcel = intent.getParcelableExtra<HotelGalleryParcel>(HotelExtras.GALLERY_PARCELABLE)

        initToolbar()

        if (savedInstanceState != null) {
            scrollPosition = savedInstanceState.getInt(HotelExtras.GALLERY_SCROLL_POSITION)
        } else {
            scrollPosition = galleryParcel.startIndex
        }
    }

    override fun onStart() {
        super.onStart()
        galleryManager.fetchMediaList(galleryParcel.roomCode, galleryImagesObserver)

        if (galleryParcel.showDescription) {
            galleryView.expand()
        } else {
            galleryView.collapse()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(HotelExtras.GALLERY_SCROLL_POSITION, galleryView.getCurrentIndex())
    }

    private fun initToolbar() {
        val viewModel = HotelInfoToolbarViewModel(this, galleryParcel.hotelName,
                galleryParcel.hotelStarRating, soldOut = false)
        toolbar.setViewModel(viewModel)
        toolbar.navClickedSubject.subscribe {
            onBackPressed()
        }
    }

    private val galleryImagesObserver = object: Observer<ArrayList<HotelMedia>> {
        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
        }

        override fun onNext(list: java.util.ArrayList<HotelMedia>) {
            galleryView.setGalleryItems(list)
            galleryView.scrollTo(scrollPosition)
        }
    }
}
