package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelRoomHeaderViewModel
import android.view.ViewTreeObserver
import rx.subjects.PublishSubject

class HotelRoomHeaderView(context: Context, val viewModel: HotelRoomHeaderViewModel): RelativeLayout(context) {

    private val headerImageView: ImageView by bindView(R.id.header_image_view)
    private val roomTypeTextView: TextView by bindView(R.id.room_type_text_view)
    private val roomInfoIcon: ImageView by bindView(R.id.room_info_icon)
    private val bedTypeTextView: TextView by bindView(R.id.bed_type_text_view)

    val roomInfoClickedSubject = PublishSubject.create<Unit>()

    init {
        View.inflate(context, R.layout.hotel_room_header, this)

        headerImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.room_fallback))

        headerImageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                headerImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val imageUrl: String? = viewModel.imageUrl
                if (!imageUrl.isNullOrBlank()) {
                    val hotelMedia = HotelMedia(imageUrl)
                    PicassoHelper.Builder(headerImageView)
                            .setPlaceholder(R.drawable.room_fallback)
                            .build()
                            .load(hotelMedia.getBestUrls(headerImageView.width / 2))
                }
            }
        })

        bindViewModel(viewModel)
    }

    fun bindViewModel(viewModel: HotelRoomHeaderViewModel) {
        roomTypeTextView.text = viewModel.roomTypeString

        roomInfoIcon.subscribeOnClick(roomInfoClickedSubject)

        bedTypeTextView.text = viewModel.bedTypeString
    }

    fun recycleImageView() {
        headerImageView.drawable?.callback = null
        headerImageView.setImageDrawable(null)
    }
}
