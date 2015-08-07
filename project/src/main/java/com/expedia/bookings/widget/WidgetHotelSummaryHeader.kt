package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

public class WidgetHotelSummaryHeader(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), HeaderBitmapDrawable.CallbackListener {

    val hotelImage: ImageView by bindView(R.id.hotel_room_background)

    init {
        View.inflate(getContext(), R.layout.widget_hotel_summary_header, this)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        setPadding(getPaddingLeft(), getPaddingTop() + statusBarHeight, getPaddingRight(), getPaddingBottom())
    }

    fun setHotelImage(imageUrl: HotelOffersResponse.HotelRoomResponse?) {
        val drawable = Images.makeHotelBitmapDrawable(getContext(), this, getWidth(), Images.getMediaHost() + imageUrl!!.roomThumbnailUrl, null)
        hotelImage.setImageDrawable(drawable)
    }

    override fun onBitmapLoaded() {
    }

    override fun onBitmapFailed() {
    }

    override fun onPrepareLoad() {
    }

}
