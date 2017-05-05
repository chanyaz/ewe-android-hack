package com.expedia.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelDetailsToolbar
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.bookings.widget.TextView
import com.expedia.vm.HotelDetailToolbarViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiata.android.util.AndroidUtils

class GalleryActivity : Activity(), RecyclerGallery.GalleryItemScrollListener {
    val gallery: RecyclerGallery by lazy {
        findViewById(R.id.images_gallery) as RecyclerGallery
    }
    val galleryIndicator: View by lazy {
        findViewById(R.id.hotel_gallery_indicator)
    }
    val galleryDescription: TextView by lazy {
        findViewById(R.id.hotel_gallery_description) as TextView
    }
    val toolbar: HotelDetailsToolbar by lazy {
        findViewById(R.id.hotel_details_toolbar) as HotelDetailsToolbar
    }

    var mediaList = emptyList<HotelMedia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FeatureToggleUtil.isUserBucketedAndFeatureEnabled(this@GalleryActivity, AbacusUtils.EBAndroidAppItinCrystalSkin, R.string.preference_itin_crystal_theme)) {
            setTheme(R.style.Theme_Hotels_Default_Crystal)
        }
        setContentView(R.layout.itin_gallery)
        Ui.showTransparentStatusBar(this)
        setUpGallery()
        setUpToolbar()
        OmnitureTracking.trackHotelItinGalleryOpen()
    }

    private fun setUpGallery() {
        val token = object : TypeToken<List<HotelMedia>>() {}
        val json = intent.getStringExtra("Urls")
        mediaList = Gson().fromJson<List<HotelMedia>>(json, token.type)
        gallery.setDataSource(mediaList)

        val position = intent.getIntExtra("Position", 0)
        gallery.setOnItemChangeListener(this)
        onGalleryItemScrolled(position)
        gallery.scrollToPosition(position)

        val galleryItemCount = gallery.adapter.itemCount
        if (galleryItemCount > 0) {
            val indicatorWidth = AndroidUtils.getScreenSize(this@GalleryActivity).x / galleryItemCount
            val lp = galleryIndicator.layoutParams
            lp.width = indicatorWidth
            galleryIndicator.layoutParams = lp
        }

        gallery.addImageViewCreatedListener({ index -> updateGalleryChildrenHeights(index) })
    }

    private fun updateGalleryChildrenHeights(index: Int) {
        resizeImageViews(index)
        resizeImageViews(index - 1)
        resizeImageViews(index + 1)
    }

    private fun resizeImageViews(index: Int) {
        if (index >= 0 && index < gallery.adapter.itemCount) {
            val height  = gallery.height
            val galleryHeight = resources.getDimensionPixelSize(R.dimen.gallery_height)
            var holder = gallery.findViewHolderForAdapterPosition(index)
            if (holder != null) {
                holder = holder as RecyclerGallery.RecyclerAdapter.ViewHolder
                holder.mImageView?.setIntermediateValue(height - galleryHeight, height,
                        0f / galleryHeight)
            }
        }
    }

    private fun setUpToolbar() {
        val hotelName = intent.getStringExtra("Name")
        val hotelRating = intent.getFloatExtra("Rating", 0f)
        val vm = HotelDetailToolbarViewModel(this@GalleryActivity, hotelName, hotelRating, false)
        toolbar.setHotelDetailViewModel(vm)
        toolbar.toolBarBackground.alpha = 0f
        toolbar.toolbar.setNavigationOnClickListener { view ->
            onBackPressed()
        }
        if (FeatureToggleUtil.isUserBucketedAndFeatureEnabled(this@GalleryActivity, AbacusUtils.EBAndroidAppItinCrystalSkin, R.string.preference_itin_crystal_theme)) {
            toolbar.refreshForCystalTheme()
        }
    }

    override fun onGalleryItemScrolled(position: Int) {
        galleryIndicator.animate().translationX((position * galleryIndicator.width).toFloat()).
                setInterpolator(LinearInterpolator()).start()
        galleryDescription.text = mediaList[position].mDescription
    }
}
