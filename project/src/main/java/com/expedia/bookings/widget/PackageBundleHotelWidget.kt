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
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.BundleHotelViewModel

class PackageBundleHotelWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val hotelLoadingBar: ImageView by bindView(R.id.hotel_loading_bar)
    val hotelInfoContainer: ViewGroup by bindView(R.id.hotel_info_container)
    val hotelsText: TextView by bindView(R.id.hotels_card_view_text)
    val hotelsDatesGuestInfoText: TextView by bindView(R.id.hotels_dates_guest_info_text)
    val hotelDetailsIcon: ImageView by bindView(R.id.package_hotel_details_icon)
    val hotelSelectIcon: ImageView by bindView(R.id.package_hotel_select_icon)
    val hotelLuggageIcon: ImageView by bindView(R.id.package_hotel_luggage_icon)
    val hotelRoomImage: ImageView by bindView(R.id.selected_hotel_room_image)
    val hotelRoomInfo: TextView by bindView(R.id.hotel_room_info)
    val hotelRoomType: TextView by bindView(R.id.hotel_room_type)
    val hotelAddress: TextView by bindView(R.id.hotel_address)
    val hotelCity: TextView by bindView(R.id.hotel_city)
    val hotelFreeCancellation: TextView by bindView(R.id.hotel_free_cancellation)
    val hotelNotRefundable: TextView by bindView(R.id.hotel_non_refundable)
    val hotelPromoText: TextView by bindView(R.id.hotel_promo_text)
    val mainContainer: LinearLayout by bindView(R.id.main_container)
    val rowContainer: LinearLayout by bindView(R.id.row_container)

    var viewModel: BundleHotelViewModel by notNullAndObservable { vm ->
        viewModel.hotelDatesGuestObservable.subscribeTextAndVisibility(hotelsDatesGuestInfoText)
        viewModel.hotelRoomInfoObservable.subscribeText(hotelRoomInfo)
        viewModel.hotelRoomTypeObservable.subscribeText(hotelRoomType)
        viewModel.hotelAddressObservable.subscribeText(hotelAddress)
        viewModel.hotelCityObservable.subscribeText(hotelCity)
        viewModel.hotelFreeCancellationObservable.subscribeTextAndVisibility(hotelFreeCancellation)
        viewModel.hotelNonRefundableObservable.subscribeTextAndVisibility(hotelNotRefundable)
        viewModel.hotelPromoTextObservable.subscribeTextAndVisibility(hotelPromoText)
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
        viewModel.showLoadingStateObservable.subscribeInverseVisibility(hotelsDatesGuestInfoText)
        viewModel.showLoadingStateObservable.subscribe { showLoading ->
            if (showLoading) {
                rowContainer.setOnClickListener { null }
                hotelInfoContainer.isEnabled = false
                AnimUtils.progressForward(hotelLoadingBar)
                hotelsText.setTextColor(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
                hotelLuggageIcon.setColorFilter(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            } else {
                hotelInfoContainer.isEnabled = true
                rowContainer.setOnClickListener {
                    openHotels()
                }
                hotelLoadingBar.clearAnimation()
                hotelsText.setTextColor(Ui.obtainThemeColor(context, R.attr.primary_color))
                hotelLuggageIcon.setColorFilter(Ui.obtainThemeColor(context, R.attr.primary_color))
                hotelsDatesGuestInfoText.setTextColor(Ui.obtainThemeColor(context, R.attr.primary_color))
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
            hotelsText.setTextColor(ContextCompat.getColor(context, R.color.packages_bundle_overview_widgets_primary_text))
            hotelLuggageIcon.setColorFilter(0)
            hotelsDatesGuestInfoText.setTextColor(ContextCompat.getColor(context, R.color.packages_bundle_overview_widgets_secondary_text))
            rowContainer.setOnClickListener {
                if (mainContainer.visibility == Presenter.GONE) {
                    expandSelectedHotel()
                } else {
                    collapseSelectedHotel()
                }
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.bundle_hotel_widget, this)
    }

    fun openHotels() {
        Db.clearPackageHotelRoomSelection()
        val intent = Intent(context, PackageHotelActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)
        (context as AppCompatActivity).overridePendingTransition(0, 0);
    }

    private fun expandSelectedHotel() {
        mainContainer.visibility = Presenter.VISIBLE
        AnimUtils.rotate(hotelDetailsIcon)
        PackagesTracking().trackBundleOverviewHotelExpandClick()
    }

    fun collapseSelectedHotel() {
        mainContainer.visibility = Presenter.GONE
        AnimUtils.reverseRotate(hotelDetailsIcon)
        hotelDetailsIcon.clearAnimation()
    }

    fun backButtonPressed() {
        if (mainContainer.visibility == Presenter.VISIBLE) {
            collapseSelectedHotel()
        }
    }

    fun toggleHotelWidget(alpha: Float, isEnabled: Boolean) {
        hotelsText.alpha = alpha
        hotelsDatesGuestInfoText.alpha = alpha
        hotelLuggageIcon.alpha = alpha
        hotelDetailsIcon.alpha = alpha
        this.isEnabled = isEnabled
        hotelDetailsIcon.isEnabled = isEnabled
        rowContainer.isEnabled = isEnabled
    }
}
