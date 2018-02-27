package com.expedia.bookings.hotel.activity

import android.content.Intent
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
import com.expedia.bookings.hotel.widget.adapter.HotelGalleryGridAdapter
import android.app.ActivityOptions
import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.hotel.widget.GalleryGridItemDecoration
import com.squareup.picasso.Picasso

class HotelGalleryGridActivity : AppCompatActivity(), ComponentCallbacks2 {
    private val toolbar by bindView<Toolbar>(R.id.hotel_gallery_grid_toolbar)
    private val recyclerView by bindView<RecyclerView>(R.id.hotel_gallery_grid_recycler)

    private lateinit var galleryManager: HotelGalleryManager
    private lateinit var galleryConfig: HotelGalleryConfig
    private lateinit var adapter: HotelGalleryGridAdapter

    private var columnCount = 3
    private val activityManager by lazy { getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager }

    private val HD_IMAGE_MEMORY_THRESHOLD = 256

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

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                if (!adapter.lowMemoryMode) {
                    val galleryItems = galleryManager.fetchMediaList(galleryConfig.roomCode)
                    val picasso = Picasso.with(this)
                    galleryItems.forEach { media ->
                        picasso.invalidate(media.getUrl(HotelMedia.Size.getIdealGridSize()))
                    }
                    adapter.forceLowMemory()
                }
            }
        }
    }

    private fun initToolbar() {
        toolbar.title = galleryConfig.hotelName
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initRecyclerView() {
        val layoutManager = GridLayoutManager(this, columnCount)
        recyclerView.layoutManager = layoutManager

        adapter = HotelGalleryGridAdapter(isLowMemory() || isBelowMemoryThreshold())
        recyclerView.adapter = adapter
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.gallery_grid_spacing)
        recyclerView.addItemDecoration(GalleryGridItemDecoration(spacingInPixels, columnCount))

        adapter.selectedImagePosition.subscribe { position ->
            val intent = Intent(this, HotelGalleryActivity::class.java)
            intent.putExtra(HotelExtras.GALLERY_CONFIG, galleryConfig.copy(startIndex = position))
            val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            startActivity(intent, bundle)
        }
    }

    private fun isLowMemory(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }

    private fun isBelowMemoryThreshold(): Boolean {
        return activityManager.memoryClass < HD_IMAGE_MEMORY_THRESHOLD
    }
}
