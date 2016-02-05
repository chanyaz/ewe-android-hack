package com.expedia.bookings.widget

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.*
import com.expedia.vm.BundleHotelViewModel

public class PackageBundleHotelWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val hotelLoadingBar: ImageView by bindView(R.id.hotel_loading_bar)
    val hotelInfoContainer: ViewGroup by bindView(R.id.hotel_info_container)
    val hotelsText: TextView by bindView(R.id.hotels_card_view_text)
    val hotelsRoomGuestInfoText: TextView by bindView(R.id.hotels_room_guest_info_text)
    val hotelDetailsIcon: ImageView by bindView(R.id.package_hotel_details_icon)
    val hotelSelectIcon: ImageView by bindView(R.id.package_hotel_select_icon)
    val hotelLuggageIcon: ImageView by bindView(R.id.package_hotel_luggage_icon)
    val hotelRoomImage: ImageView by bindView(R.id.selected_hotel_room_image)
    val hotelRoomInfo: TextView by bindView(R.id.hotel_room_info)
    val hotelRoomType: TextView by bindView(R.id.hotel_room_type)
    val hotelAddress: TextView by bindView(R.id.hotel_address)
    val hotelCity: TextView by bindView(R.id.hotel_city)
    val hotelFreeCancellation: TextView by bindView(R.id.hotel_free_cancellation)

    var viewModel: BundleHotelViewModel by notNullAndObservable { vm ->

        viewModel.hotelRoomGuestObservable.subscribeTextAndVisibility(hotelsRoomGuestInfoText)
        viewModel.hotelRoomInfoObservable.subscribeText(hotelRoomInfo)
        viewModel.hotelRoomTypeObservable.subscribeText(hotelRoomType)
        viewModel.hotelAddressObservable.subscribeText(hotelAddress)
        viewModel.hotelCityObservable.subscribeText(hotelCity)
        viewModel.hotelFreeCancellationObservable.subscribeText(hotelFreeCancellation)
        viewModel.hotelTextObservable.subscribeText(hotelsText)
        viewModel.hotelDetailsIconObservable.subscribeVisibility(hotelDetailsIcon)
        viewModel.hotelIconImageObservable.subscribe { hotelLuggageIcon.setImageResource(it) }

        viewModel.hotelRoomImageUrlObservable.subscribe { imageUrl ->
            if (imageUrl.isNotBlank()) {
                val hotelMedia = HotelMedia(imageUrl)
                PicassoHelper.Builder(hotelRoomImage)
                        .setPlaceholder(R.drawable.room_fallback)
                        .build()
                        .load(hotelMedia.getBestUrls(hotelRoomImage.width))
            }
        }

        viewModel.showLoadingStateObservable.subscribeVisibility(hotelLoadingBar)
        viewModel.showLoadingStateObservable.subscribeInverseVisibility(hotelsRoomGuestInfoText)
        viewModel.showLoadingStateObservable.subscribe { showLoading ->
            if (showLoading) {
                hotelInfoContainer.isEnabled = false
                AnimUtils.progressForward(hotelLoadingBar)
                hotelsText.setTextColor(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
                hotelLuggageIcon.setColorFilter(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            } else {
                hotelInfoContainer.isEnabled = true
                hotelLoadingBar.clearAnimation()
                hotelsText.setTextColor(Ui.obtainThemeColor(context, R.attr.primary_color))
                hotelLuggageIcon.setColorFilter(Ui.obtainThemeColor(context, R.attr.primary_color))
                hotelsRoomGuestInfoText.setTextColor(Ui.obtainThemeColor(context, R.attr.primary_color))
            }
        }
        viewModel.hotelSelectIconObservable.subscribe { showing ->
            if (showing) {
                hotelSelectIcon.visibility = View.VISIBLE
                AnimUtils.getFadeInRotateAnim(hotelSelectIcon).start()
            } else {
                hotelSelectIcon.clearAnimation()
                hotelSelectIcon.visibility = View.GONE
            }
        }
        viewModel.selectedHotelObservable.subscribe {
            hotelsText.setTextColor(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            hotelLuggageIcon.setColorFilter(0)
            hotelsRoomGuestInfoText.setTextColor(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        }
    }

    init {
        View.inflate(getContext(), R.layout.bundle_hotel_widget, this)
        hotelInfoContainer.setOnClickListener {
            openHotels()
        }
        hotelLuggageIcon.setOnClickListener {
            openHotels()
        }
        hotelDetailsIcon.setOnClickListener {
            if (hotelRoomImage.visibility == Presenter.GONE) {
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
        AnimUtils.rotate(hotelDetailsIcon)
    }

    public fun collapseSelectedHotel() {
        hotelCity.visibility = Presenter.GONE
        hotelRoomImage.visibility = Presenter.GONE
        hotelRoomInfo.visibility = Presenter.GONE
        hotelRoomType.visibility = Presenter.GONE
        hotelAddress.visibility = Presenter.GONE
        hotelFreeCancellation.visibility = Presenter.GONE
        AnimUtils.reverseRotate(hotelDetailsIcon)
        hotelDetailsIcon.clearAnimation()
    }

    public fun backButtonPressed() {
        if (hotelRoomImage.visibility == Presenter.VISIBLE) {
            collapseSelectedHotel()
        }
    }
}
