package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class HotelDetailGalleryView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs),
        RecyclerGallery.GalleryItemListener, RecyclerGallery.GalleryItemScrollListener {
    val galleryClickedSubject = PublishSubject.create<Unit>()
    val galleryItemScrolledSubject = PublishSubject.create<Int>()

    private val galleryRecyclerView: HotelDetailGalleryRecyclerView by bindView(R.id.images_gallery)
    private val photoCountTextView: TextView by bindView(R.id.photo_count_textview)

    private val hotelGalleryDescriptionContainer: LinearLayout by bindView(R.id.hotel_gallery_description_container)
    private val hotelGalleryIndicator: View by bindView(R.id.hotel_gallery_indicator)
    private val hotelGalleryDescription: TextView by bindView(R.id.hotel_gallery_description)

    private val galleryAdapter = HotelDetailGalleryAdapter()

    private var mediaList: ArrayList<HotelMedia> = ArrayList()

    init {
        View.inflate(context, R.layout.hotel_detail_gallery_view, this)

        if (!isInEditMode) {
            galleryAdapter.galleryItemClickedSubject.subscribe(galleryClickedSubject)

            galleryRecyclerView.galleryScrolledSubject.subscribe { position ->
                hotelGalleryIndicator.animate().translationX((position * hotelGalleryIndicator.width).toFloat())
                        .setInterpolator(LinearInterpolator()).start()
                updateTextForPosition(position)
            }
        }
    }

    fun setGalleryItems(list: ArrayList<HotelMedia>) {
        this.mediaList = list
        galleryRecyclerView.adapter = galleryAdapter
        galleryAdapter.setMedia(list)

        if (list.isNotEmpty()) {
            galleryRecyclerView.scrollToPosition(0)
            updateTextForPosition(0)

            val galleryItemCount = galleryAdapter.itemCount
            if (galleryItemCount > 0) {
                val indicatorWidth = width / galleryItemCount
                val lp = hotelGalleryIndicator.layoutParams
                lp.width = indicatorWidth
                hotelGalleryIndicator.layoutParams = lp
            }
        }
    }

    override fun onGalleryItemClicked(item: Any) {
        galleryClickedSubject.onNext(Unit)
    }

    override fun onGalleryItemScrolled(position: Int) {
        galleryItemScrolledSubject.onNext(position)
    }

    fun getCurrentIndex(): Int {
        return galleryRecyclerView.getPosition()
    }

    fun scrollTo(position: Int) {
        galleryRecyclerView.scrollToPosition(position)
        updateTextForPosition(position)
    }

    fun expand() {
        galleryRecyclerView.updateAccessibility(collapsed = false)
        hotelGalleryDescriptionContainer.visibility = View.VISIBLE
    }

    fun collapse() {
        galleryRecyclerView.updateAccessibility(collapsed = true)
        hotelGalleryDescriptionContainer.visibility = View.GONE
    }

    fun updateSoldOut(soldOut: Boolean) {
        galleryAdapter.updateSoldOut(soldOut)
    }

    private fun updateTextForPosition(position: Int) {
        hotelGalleryDescription.text = mediaList[position].description
        photoCountTextView.text = Phrase.from(context, R.string.gallery_photo_count_TEMPLATE)
                .put("index", (position + 1).toString())
                .put("count", galleryAdapter.itemCount.toString())
                .format().toString()
    }
}
