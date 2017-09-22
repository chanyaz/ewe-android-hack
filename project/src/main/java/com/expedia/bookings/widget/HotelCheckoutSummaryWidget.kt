package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.expedia.util.safeSubscribe
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.squareup.picasso.Picasso

class HotelCheckoutSummaryWidget(context: Context, attrs: AttributeSet?, val viewModel: HotelCheckoutSummaryViewModel) : LinearLayout(context, attrs) {

    val isFreeCancellationTooltipEnabled by lazy {
        AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFreeCancellationTooltip)
    }

    val PICASSO_HOTEL_IMAGE = "HOTEL_CHECKOUT_IMAGE"

    val hotelName: android.widget.TextView by bindView(R.id.hotel_name)
    val hotelRoomImage: ImageView by bindView(R.id.hotel_checkout_room_image)
    val date: android.widget.TextView by bindView(R.id.check_in_out_dates)
    val address: android.widget.TextView by bindView(R.id.address_line_one)
    val cityState: android.widget.TextView by bindView(R.id.address_city_state)
    val selectedRoom: android.widget.TextView by bindView(R.id.selected_room)
    val selectedBed: android.widget.TextView by bindView(R.id.selected_bed)
    val numberNights: android.widget.TextView by bindView(R.id.number_nights)
    val numberGuests: android.widget.TextView by bindView(R.id.number_guests)
    val freeCancellationView: android.widget.TextView by bindView(R.id.free_cancellation_text)
    val freeCancellationTooltipView: android.widget.TextView by bindView(R.id.free_cancellation_tooltip_text)
    val valueAddsContainer: ValueAddsContainer by bindView(R.id.value_adds_container)
    val totalWithTaxLabelWithInfoButton: android.widget.TextView by bindView(R.id.total_tax_label)
    val totalPriceWithTax: android.widget.TextView by bindView(R.id.total_price_with_tax)
    val feesPaidLabel: android.widget.TextView by bindView(R.id.fees_paid_label)
    val totalFees: android.widget.TextView by bindView(R.id.total_fees)
    val totalPriceWithTaxAndFees: android.widget.TextView by bindView(R.id.total_price_with_tax_and_fees)
    val amountDueTodayLabel: android.widget.TextView by bindView(R.id.amount_due_today_label)
    val bestPriceGuarantee: android.widget.TextView by bindView(R.id.best_price_guarantee)
    val costSummary: LinearLayout by bindView(R.id.cost_summary)
    val priceChangeLayout: LinearLayout by bindView(R.id.price_change_container)
    val priceChange: android.widget.TextView by bindView(R.id.price_change_text)
    val checkinCheckoutDateContainer: LinearLayout by bindView(R.id.checkin_checkout_date_holder)
    val checkinDate: android.widget.TextView by bindView(R.id.checkin_date)
    val checkoutDate: android.widget.TextView by bindView(R.id.checkout_date)
    val hotelBookingSummaryContainer: android.widget.LinearLayout by bindView(R.id.hotel_booking_summary)

    val breakdown = HotelBreakDownView(context, null)
    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setView(breakdown)
        builder.setTitle(R.string.cost_summary)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which -> dialog.dismiss() })
        builder.create()
    }

    init {
        orientation = LinearLayout.VERTICAL
        View.inflate(getContext(), R.layout.hotel_checkout_summary_widget, this)

        costSummary.setOnClickListener {
            dialog.show()
            HotelTracking.trackTripSummaryClick()
        }

        viewModel.hotelName.subscribeText(hotelName)
        viewModel.address.subscribeText(address)
        viewModel.city.subscribeText(cityState)

        if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)) {
            date.visibility = View.GONE
            checkinCheckoutDateContainer.visibility = View.VISIBLE
            hotelBookingSummaryContainer.setPadding(hotelBookingSummaryContainer.paddingLeft, hotelBookingSummaryContainer.paddingTop, hotelBookingSummaryContainer.paddingRight, 6)
        } else {
            date.visibility = View.VISIBLE
            checkinCheckoutDateContainer.visibility = View.GONE
            hotelBookingSummaryContainer.setPadding(hotelBookingSummaryContainer.paddingLeft, hotelBookingSummaryContainer.paddingTop, hotelBookingSummaryContainer.paddingRight, 13)
        }

        setUpFreeCancellationSubscription()

        viewModel.valueAddsListObservable.safeSubscribe(valueAddsContainer.valueAddsSubject)
        viewModel.roomDescriptions.subscribeText(selectedRoom)
        viewModel.bedDescriptions.subscribeText(selectedBed)
        if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)) {
            viewModel.checkinDateFormattedByEEEMMDD.subscribeText(checkinDate)
            viewModel.checkoutDateFormattedByEEEMMDD.subscribeText(checkoutDate)
        } else {
            viewModel.checkInOutDatesFormatted.subscribeText(date)
        }
        viewModel.numNights.subscribeText(numberNights)
        viewModel.numGuests.subscribeText(numberGuests)
        viewModel.dueNowAmount.subscribeText(totalPriceWithTaxAndFees)
        viewModel.showFeesPaidAtHotel.subscribeVisibility(feesPaidLabel)
        viewModel.showFeesPaidAtHotel.subscribeVisibility(totalFees)
        viewModel.isPayLaterOrResortCase.subscribeVisibility(totalWithTaxLabelWithInfoButton)
        viewModel.isPayLaterOrResortCase.subscribeVisibility(totalPriceWithTax)
        viewModel.feesPaidAtHotel.subscribeText(totalFees)
        viewModel.tripTotalPrice.subscribeText(totalPriceWithTax)
        viewModel.isBestPriceGuarantee.subscribeVisibility(bestPriceGuarantee)
        viewModel.isPriceChange.subscribeVisibility(priceChangeLayout)
        viewModel.priceChangeMessage.subscribeText(priceChange)
        viewModel.priceChangeIconResourceId.subscribe { resourceId ->
            priceChange.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0)
        }
        viewModel.isPayLaterOrResortCase.subscribeVisibility(totalWithTaxLabelWithInfoButton.compoundDrawables[2], false)
        viewModel.isPayLaterOrResortCase.subscribeVisibility(amountDueTodayLabel.compoundDrawables[2], true)
        viewModel.amountDueTodayLabelObservable.subscribeText(amountDueTodayLabel)
        viewModel.costSummaryContentDescription.subscribe { contentDescription ->
            AccessibilityUtil.appendRoleContDesc(costSummary, contentDescription, R.string.accessibility_cost_summary_cont_desc_role_button)
        }
        viewModel.roomHeaderImage.subscribe {
            PicassoHelper.Builder(context)
                    .setPlaceholder(R.drawable.room_fallback)
                    .setError(R.drawable.room_fallback)
                    .setTarget(picassoTarget).setTag(PICASSO_HOTEL_IMAGE)
                    .build()
                    .load(HotelMedia(Images.getMediaHost() + it).getBestUrls(width / 2))
        }
        breakdown.viewmodel = HotelBreakDownViewModel(context, viewModel)

    }

    private fun setUpFreeCancellationSubscription() {
        val freeCancellationViewToDisplay = if (isFreeCancellationTooltipEnabled) freeCancellationTooltipView else freeCancellationView
        viewModel.hasFreeCancellation.subscribeVisibility(freeCancellationViewToDisplay)
        viewModel.freeCancellationText.subscribeText(freeCancellationViewToDisplay)
    }

    val picassoTarget = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)

            val drawable = HeaderBitmapDrawable()
            drawable.setCornerRadius(resources.getDimensionPixelSize(R.dimen.hotel_checkout_image_corner_radius))
            drawable.setCornerMode(HeaderBitmapDrawable.CornerMode.TOP)
            drawable.setBitmap(bitmap)

            val textColor: Int
            if (!mIsFallbackImage) {
                // only apply gradient treatment to hotels with images #5647
                val palette = Palette.Builder(bitmap).generate()
                val color = palette.getDarkVibrantColor(R.color.transparent_dark)
                val fullColorBuilder = ColorBuilder(color).darkenBy(0.25f)
                val gradientColor = fullColorBuilder.setAlpha(154).build()
                val colorArrayBottom = intArrayOf(gradientColor, gradientColor)
                drawable.setGradient(colorArrayBottom, floatArrayOf(0f, 1f))
                textColor = ContextCompat.getColor(context, R.color.itin_white_text)
            } else {
                textColor = ContextCompat.getColor(context, R.color.text_black)
            }
            hotelName.setTextColor(textColor)
            date.setTextColor(textColor)
            address.setTextColor(textColor)
            cityState.setTextColor(textColor)
            hotelRoomImage.setImageDrawable(drawable)
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            super.onBitmapFailed(errorDrawable)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            super.onPrepareLoad(placeHolderDrawable)

            if (placeHolderDrawable != null) {
                hotelRoomImage.setImageDrawable(placeHolderDrawable)

                val textColor = ContextCompat.getColor(context, R.color.text_black)
                hotelName.setTextColor(textColor)
                date.setTextColor(textColor)
                address.setTextColor(textColor)
                cityState.setTextColor(textColor)
            }
        }
    }
}
