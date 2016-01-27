package com.expedia.bookings.widget

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.BundleHotelViewModel

public class PackageBundleHotelWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val hotelLoadingBar: ProgressBar by bindView(R.id.hotel_loading_bar)
    val hotelTextContainer: ViewGroup by bindView(R.id.hotel_info_container)
    val hotelsText: TextView by bindView(R.id.hotels_card_view_text)
    val hotelsRoomGuestInfoText: TextView by bindView(R.id.hotels_room_guest_info_text)
    val hotelArrowIcon: ImageView by bindView(R.id.package_hotel_arrow_icon)
    val hotelLuggageIcon: ImageView by bindView(R.id.package_hotel__luggage_icon)
    val hotelRoomImage: ImageView by bindView(R.id.selected_hotel_room_image)
    val hotelRoomInfo: TextView by bindView(R.id.hotel_room_info)
    val hotelRoomType: TextView by bindView(R.id.hotel_room_type)
    val hotelAddress: TextView by bindView(R.id.hotel_address)
    val hotelCity: TextView by bindView(R.id.hotel_city)
    val hotelFreeCancellation: TextView by bindView(R.id.hotel_free_cancellation)

    var viewModel: BundleHotelViewModel by notNullAndObservable { vm ->

        viewModel.hotelArrowIconObservable.subscribeVisibility(hotelArrowIcon)
        viewModel.hotelRoomGuestObservable.subscribeTextAndVisibility(hotelsRoomGuestInfoText)
        viewModel.hotelRoomInfoObservable.subscribeText(hotelRoomInfo)
        viewModel.hotelRoomTypeObservable.subscribeText(hotelRoomType)
        viewModel.hotelAddressObservable.subscribeText(hotelAddress)
        viewModel.hotelCityObservable.subscribeText(hotelCity)
        viewModel.hotelFreeCancellationObservable.subscribeText(hotelFreeCancellation)
        viewModel.hotelTextObservable.subscribeText(hotelsText)

        viewModel.hotelRoomImageUrlObservable.subscribe { imageUrl ->
            if (imageUrl.isNotBlank()) {
                val hotelMedia = HotelMedia(imageUrl)
                PicassoHelper.Builder(hotelRoomImage)
                        .setPlaceholder(R.drawable.room_fallback)
                        .build()
                        .load(hotelMedia.getBestUrls(hotelRoomImage.width))
            }
        }

        viewModel.showLoadingStateObservable.subscribe { showLoading ->
            if (showLoading) {
                this.isEnabled = false
                hotelLoadingBar.visibility = View.VISIBLE
            } else {
                this.isEnabled = true
                hotelLoadingBar.visibility = View.GONE
            }
        }

    }

    init {
        View.inflate(getContext(), R.layout.bundle_hotel_widget, this)
        hotelTextContainer.setOnClickListener {
            openHotels()
        }
        hotelLuggageIcon.setOnClickListener {
            openHotels()
        }
        hotelArrowIcon.setOnClickListener {
            if (hotelRoomImage.visibility == View.GONE) {
                expandSelectedHotel()
            } else {
                collapseSelectedHotel()
            }
        }

    }

    fun openHotels() {
        val intent = Intent(context, PackageHotelActivity::class.java)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)
    }

    private fun expandSelectedHotel() {
        hotelRoomImage.visibility = Presenter.VISIBLE
        hotelRoomInfo.visibility = Presenter.VISIBLE
        hotelRoomType.visibility = Presenter.VISIBLE
        hotelCity.visibility = Presenter.VISIBLE
        hotelAddress.visibility = Presenter.VISIBLE
        hotelFreeCancellation.visibility = Presenter.VISIBLE
    }

    public fun collapseSelectedHotel() {
        hotelCity.visibility = Presenter.GONE
        hotelRoomImage.visibility = Presenter.GONE
        hotelRoomInfo.visibility = Presenter.GONE
        hotelRoomType.visibility = Presenter.GONE
        hotelAddress.visibility = Presenter.GONE
        hotelFreeCancellation.visibility = Presenter.GONE
    }
}
