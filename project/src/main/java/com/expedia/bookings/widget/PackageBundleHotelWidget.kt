package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
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
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.packages.BundleHotelViewModel
import com.squareup.phrase.Phrase

class PackageBundleHotelWidget(context: Context, attrs: AttributeSet?) : AccessibleCardView(context, attrs) {
    val hotelLoadingBar: ImageView by bindView(R.id.hotel_loading_bar)
    val hotelInfoContainer: ViewGroup by bindView(R.id.hotel_info_container)
    val hotelsText: TextView by bindView(R.id.hotels_card_view_text)
    val hotelsDatesGuestInfoText: TextView by bindView(R.id.hotels_dates_guest_info_text)
    val hotelDetailsIcon: ImageView by bindView(R.id.package_hotel_details_icon)
    val selectArrowIcon: ImageView by bindView(R.id.package_hotel_select_icon)
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
    val viewWidth = Ui.getScreenSize(context).x / 2
    var canExpand = false
    var isRowClickable = true

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
                        .load(hotelMedia.getBestUrls(viewWidth))
            }
        }

        viewModel.showLoadingStateObservable.subscribeVisibility(hotelLoadingBar)
        viewModel.showLoadingStateObservable.subscribeInverseVisibility(hotelsDatesGuestInfoText)
        viewModel.showLoadingStateObservable.subscribe { showLoading ->
            this.loadingStateObservable.onNext(showLoading)
            if (showLoading) {
                isRowClickable = false
                hotelInfoContainer.isEnabled = false
                AnimUtils.progressForward(hotelLoadingBar)
                selectArrowIcon.visibility = View.GONE
                hotelsText.setTextColor(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
                hotelLuggageIcon.setColorFilter(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            } else {
                isRowClickable = true
                canExpand = false
                hotelInfoContainer.isEnabled = true
                selectArrowIcon.visibility = View.VISIBLE
                hotelLoadingBar.clearAnimation()
                hotelsText.setTextColor(Ui.obtainThemeColor(context, R.attr.primary_color))
                hotelLuggageIcon.setColorFilter(Ui.obtainThemeColor(context, R.attr.primary_color))
                hotelsDatesGuestInfoText.setTextColor(Ui.obtainThemeColor(context, R.attr.primary_color))
            }
        }
        viewModel.hotelSelectIconObservable.subscribe { showing ->
            if (showing) {
            } else {
                selectArrowIcon.visibility = View.GONE
            }
        }

        viewModel.selectedHotelObservable.subscribe {
            this.selectedCardObservable.onNext(Unit)
            hotelsText.setTextColor(ContextCompat.getColor(context, R.color.packages_bundle_overview_widgets_primary_text))
            hotelLuggageIcon.setColorFilter(0)
            hotelsDatesGuestInfoText.setTextColor(ContextCompat.getColor(context, R.color.packages_bundle_overview_widgets_secondary_text))
            canExpand = true
        }
    }

    fun cancel() {
        hotelLoadingBar.clearAnimation()
        hotelLoadingBar.visibility = View.GONE
        hotelsDatesGuestInfoText.visibility = View.VISIBLE
        hotelsDatesGuestInfoText.setTextColor(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
    }

    init {
        View.inflate(getContext(), R.layout.bundle_hotel_widget, this)
        rowContainer.setOnClickListener {
            if (!isRowClickable) {
                return@setOnClickListener
            }
            if (canExpand) {
                if (mainContainer.visibility == Presenter.GONE) {
                    expandSelectedHotel()
                } else {
                    collapseSelectedHotel(true)
                }
            } else {
                openHotels()
            }
        }
    }

    fun openHotels() {
        Db.sharedInstance.clearPackageHotelRoomSelection()
        val intent = Intent(context, PackageHotelActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val activity = context as Activity
        activity.startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)
        activity.overridePendingTransition(0, 0)
    }

    fun expandSelectedHotel() {
        viewModel.hotelRowExpanded.onNext(Unit)
        mainContainer.visibility = Presenter.VISIBLE
        AnimUtils.rotate(hotelDetailsIcon)
        PackagesTracking().trackBundleOverviewHotelExpandClick(true)
        this.selectedCardObservable.onNext(Unit)
    }

    fun collapseSelectedHotel(trackClick: Boolean = false) {
        mainContainer.visibility = Presenter.GONE
        AnimUtils.reverseRotate(hotelDetailsIcon)
        hotelDetailsIcon.clearAnimation()
        if (trackClick) {
            PackagesTracking().trackBundleOverviewHotelExpandClick(false)
        }
        this.selectedCardObservable.onNext(Unit)
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

    override fun getRowInfoContainer(): ViewGroup {
        return rowContainer
    }

    override fun disabledContentDescription(): String {
        return ""
    }

    override fun loadingContentDescription(): String {
        val startDate = LocaleBasedDateFormatUtils.localDateToMMMd(Db.sharedInstance.packageParams.startDate)
        val endDate = LocaleBasedDateFormatUtils.localDateToMMMd(Db.sharedInstance.packageParams.endDate!!)
        val guests = StrUtils.formatGuestString(context, Db.sharedInstance.packageParams.guests)
        return Phrase.from(context, R.string.select_hotel_searching_cont_desc_TEMPLATE)
                .put("destination", StrUtils.formatCityName(Db.sharedInstance.packageParams.destination))
                .put("startdate", startDate)
                .put("enddate", endDate)
                .put("guests", guests)
                .format()
                .toString()
    }

    override fun contentDescription(): String {
        val startDate = LocaleBasedDateFormatUtils.localDateToMMMd(Db.sharedInstance.packageParams.startDate)
        val endDate = LocaleBasedDateFormatUtils.localDateToMMMd(Db.sharedInstance.packageParams.endDate!!)
        val guests = StrUtils.formatGuestString(context, Db.sharedInstance.packageParams.guests)
        return Phrase.from(context, R.string.select_hotel_cont_desc_TEMPLATE)
                .put("destination", StrUtils.formatCityName(Db.sharedInstance.packageParams.destination))
                .put("startdate", startDate)
                .put("enddate", endDate)
                .put("guests", guests)
                .format()
                .toString()
    }

    override fun selectedCardContentDescription(): String {
        val startDate = LocaleBasedDateFormatUtils.localDateToMMMd(Db.sharedInstance.packageParams.startDate)
        val endDate = LocaleBasedDateFormatUtils.localDateToMMMd(Db.sharedInstance.packageParams.endDate!!)
        val guests = StrUtils.formatGuestString(context, Db.sharedInstance.packageParams.guests)
        val expandState = if (mainContainer.visibility == Presenter.VISIBLE) context.getString(R.string.accessibility_cont_desc_role_button_collapse) else context.getString(R.string.accessibility_cont_desc_role_button_expand)
        return Phrase.from(context, R.string.select_hotel_selected_cont_desc_TEMPLATE)
                .put("hotel", Db.getPackageSelectedHotel()?.localizedName ?: "")
                .put("startdate", startDate)
                .put("enddate", endDate)
                .put("guests", guests)
                .put("expandstate", expandState)
                .format()
                .toString()
    }
}
