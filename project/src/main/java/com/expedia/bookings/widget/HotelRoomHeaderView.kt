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
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.util.setInverseVisibility
import com.expedia.util.updateVisibility
import io.reactivex.subjects.PublishSubject

class HotelRoomHeaderView(context: Context, val viewModel: HotelRoomHeaderViewModel) : RelativeLayout(context) {
    val roomImageClickedSubject = PublishSubject.create<Unit>()
    val roomInfoClickedSubject = PublishSubject.create<Unit>()

    private val headerImageView: ImageView by bindView(R.id.header_image_view)
    private val roomPhotoCountView: ImageView by bindView(R.id.room_photo_indicator)
    private val roomTypeTextView: TextView by bindView(R.id.room_type_text_view)
    private val roomInfoIcon: ImageView by bindView(R.id.room_info_icon)
    private val bedTypeTextView: TextView by bindView(R.id.bed_type_text_view)

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
        roomInfoIcon.setInverseVisibility(viewModel.roomDescriptionString.isNullOrEmpty())

        bedTypeTextView.text = viewModel.bedTypeString

        if (AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.HotelRoomImageGallery)) {
            roomPhotoCountView.updateVisibility(viewModel.hasRoomImages())
            if (viewModel.hasRoomImages()) {
                headerImageView.subscribeOnClick(roomImageClickedSubject)
            }
        }
    }

    fun recycleImageView() {
        headerImageView.drawable?.callback = null
        headerImageView.setImageDrawable(null)
    }
}
