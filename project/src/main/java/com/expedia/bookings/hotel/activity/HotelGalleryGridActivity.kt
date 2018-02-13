package com.expedia.bookings.hotel.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.hotel.data.HotelGalleryConfig
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelGalleryManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.expedia.bookings.hotel.widget.HotelGalleryGridAdapter

class HotelGalleryGridActivity : AppCompatActivity() {
    private val toolbar by bindView<Toolbar>(R.id.hotel_gallery_grid_toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.hotel_gallery_grid_recycler)

    private lateinit var galleryManager: HotelGalleryManager
    private lateinit var galleryConfig: HotelGalleryConfig

    private val adapter = HotelGalleryGridAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.hotel_gallery_grid_activity)

        galleryManager = Ui.getApplication(this).appComponent().hotelGalleryManager()
        galleryConfig = intent.getParcelableExtra<HotelGalleryConfig>(HotelExtras.GALLERY_CONFIG)

        initToolbar()
        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()

        val galleryItems = galleryManager.fetchMediaList(galleryConfig.roomCode)
        if (galleryItems.isEmpty()) {
            // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/8612
            finish()
        } else {
            adapter.setMedia(galleryItems)
        }
    }

    private fun initToolbar() {
        toolbar.title = galleryConfig.hotelName
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initRecyclerView() {
        val layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.gallery_grid_spacing)
        recyclerView.addItemDecoration(GalleryGridItemDecoration(spacingInPixels))

        adapter.imageAtPositionSelected.subscribe { position ->
            val intent = Intent(this, HotelGalleryActivity::class.java)
            intent.putExtra(HotelExtras.GALLERY_CONFIG, galleryConfig.copy(startIndex = position))
            startActivity(intent)
        }
    }

    class GalleryGridItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                    state: RecyclerView.State) {
            outRect.top = space
            outRect.left = space
            outRect.right = space
            outRect.bottom = space

            //todo pretty https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/10330
        }
    }
}
