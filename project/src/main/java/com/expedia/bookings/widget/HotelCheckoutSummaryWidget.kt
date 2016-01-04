package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.squareup.phrase.Phrase
import com.squareup.picasso.Picasso

public class HotelCheckoutSummaryWidget(context: Context, attrs: AttributeSet?, val viewModel: HotelCheckoutSummaryViewModel) : LinearLayout(context, attrs) {

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

    val totalFeesContainer : RelativeLayout by bindView(R.id.total_fees_container)
    val hotelFeesContainer : ViewGroup by bindView(R.id.hotel_fees_container)
    val amountDueTodayContainer : ViewGroup by bindView(R.id.amount_due_today_container)
    val dottedDivider : View by bindView(R.id.dotted_divider)

    val breakdown = HotelBreakDownView(context, null)
    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setView(breakdown)
        builder.setTitle(R.string.cost_summary)
        builder.setPositiveButton(context.getString(R.string.DONE), object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.dismiss()
            }
        })
        builder.create()
    }

    val isUserBucketedForPriceBreakDownTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelPriceBreakDownTest)
    val priceBreakDownTestVariate = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppHotelPriceBreakDownTest)

    init {
        setOrientation(LinearLayout.VERTICAL)
        View.inflate(getContext(), R.layout.hotel_checkout_summary_widget, this)
        costSummary.setOnClickListener {
            dialog.show()
            HotelV2Tracking().trackTripSummaryClick()
        }

        viewModel.hotelName.subscribeText(hotelName)
        viewModel.checkInOutDatesFormatted.subscribeText(date)
        viewModel.address.subscribeText(address)
        viewModel.city.subscribeText(cityState)
        viewModel.hasFreeCancellation.subscribeVisibility(freeCancellationView)
        viewModel.roomDescriptions.subscribeText(selectedRoom)
        viewModel.bedDescriptions.subscribeText(selectedBed)
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
        viewModel.newDataObservable.subscribe {
            resetViewsToInitialState()
            if (isUserBucketedForPriceBreakDownTest) {
                if ((it.isDepositV2.value || it.isResortCase.value)) {
                    if (priceBreakDownTestVariate == AbacusUtils.HotelPriceBreakDownVariate.DUE_TODAY.ordinal) {
                        FontCache.setTypeface(feesPaidLabel, FontCache.Font.ROBOTO_REGULAR)
                        amountDueTodayLabel.text = context.resources.getString(R.string.car_cost_breakdown_due_today)
                        changeTextSizeForTotalAndDueNow()
                    } else if (priceBreakDownTestVariate == AbacusUtils.HotelPriceBreakDownVariate.TRIP_TOTAL.ordinal) {
                        amountDueTodayLabel.text = context.resources.getString(R.string.old_room_rate_pay_later_due_now)
                        feesPaidLabel.text = context.resources.getString(R.string.fees_due_at_hotel)
                        costSummary.removeView(amountDueTodayContainer)
                        costSummary.addView(amountDueTodayContainer, 0)
                        costSummary.removeView(hotelFeesContainer)
                        costSummary.addView(hotelFeesContainer, 1)
                        costSummary.removeView(dottedDivider)
                        costSummary.addView(dottedDivider, 2)
                        costSummary.removeView(totalFeesContainer)
                        costSummary.addView(totalFeesContainer, 3)
                        dottedDivider.visibility = View.VISIBLE
                        feesPaidLabel.setCompoundDrawables(null, null, null, null)
                        amountDueTodayLabel.setTextColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
                        changeTextSizeForTotalAndDueNow()
                        totalWithTaxLabelWithInfoButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                        FontCache.setTypeface(amountDueTodayLabel, FontCache.Font.ROBOTO_REGULAR)
                        FontCache.setTypeface(totalPriceWithTaxAndFees, FontCache.Font.ROBOTO_REGULAR)
                        FontCache.setTypeface(feesPaidLabel, FontCache.Font.ROBOTO_REGULAR)

                    }
                } else {
                    if (it.isPayLaterOrResortCase.value) {
                        amountDueTodayLabel.text = Phrase.from(getContext(), R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format()
                    } else
                        amountDueTodayLabel.text = resources.getString(R.string.total_with_tax)
                }
            } else {
                if (it.isDepositV2.value)
                    amountDueTodayLabel.text = Phrase.from(getContext(), R.string.due_to_brand_today_today_TEMPLATE).put("brand", BuildConfig.brand).format()
                else if (it.isPayLaterOrResortCase.value) {
                    amountDueTodayLabel.text = Phrase.from(getContext(), R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format()
                } else
                    amountDueTodayLabel.text = resources.getString(R.string.total_with_tax)
            }
        }
        viewModel.roomHeaderImage.subscribe {
            PicassoHelper.Builder(context)
                    .setPlaceholder(R.drawable.room_fallback)
                    .setError(R.drawable.room_fallback)
                    .setTarget(picassoTarget).setTag(PICASSO_HOTEL_IMAGE)
                    .build()
                    .load(HotelMedia(Images.getMediaHost() + it).getBestUrls(width/2))
        }
        breakdown.viewmodel = HotelBreakDownViewModel(context, viewModel)

    }

    private fun changeTextSizeForTotalAndDueNow() {
        totalWithTaxLabelWithInfoButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        totalPriceWithTax.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        amountDueTodayLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        totalPriceWithTaxAndFees.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
    }

    private fun resetViewsToInitialState() {
        FontCache.setTypeface(feesPaidLabel, FontCache.Font.ROBOTO_MEDIUM)
        FontCache.setTypeface(amountDueTodayLabel, FontCache.Font.ROBOTO_MEDIUM)
        FontCache.setTypeface(totalPriceWithTaxAndFees, FontCache.Font.ROBOTO_MEDIUM)
        FontCache.setTypeface(feesPaidLabel, FontCache.Font.ROBOTO_MEDIUM)
        totalWithTaxLabelWithInfoButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        totalPriceWithTax.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        amountDueTodayLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        totalPriceWithTaxAndFees.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        feesPaidLabel.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.fees_paid), null, null, null)
        costSummary.removeView(amountDueTodayContainer)
        costSummary.addView(amountDueTodayContainer, 3)
        costSummary.removeView(dottedDivider)
        costSummary.addView(dottedDivider, 2)
        costSummary.removeView(hotelFeesContainer)
        costSummary.addView(hotelFeesContainer, 1)
        costSummary.removeView(totalFeesContainer)
        costSummary.addView(totalFeesContainer, 0)
        amountDueTodayLabel.setTextColor(ContextCompat.getColor(context, R.color.hotelsv2_checkout_text_color))
        dottedDivider.visibility = View.GONE
        feesPaidLabel.text = context.resources.getString(R.string.fees_paid_at_hotel)
    }

    val picassoTarget = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)

            val drawable = HeaderBitmapDrawable()
            drawable.setCornerRadius(resources.getDimensionPixelSize(R.dimen.hotel_checkout_image_corner_radius))
            drawable.setCornerMode(HeaderBitmapDrawable.CornerMode.TOP)
            drawable.setBitmap(bitmap)

            var textColor: Int
            if (!mIsFallbackImage) {
                // only apply gradient treatment to hotels with images #5647
                val palette = Palette.Builder(bitmap).generate()
                val color = palette.getDarkVibrantColor(R.color.transparent_dark)
                val fullColorBuilder = ColorBuilder(color).darkenBy(0.25f);
                val gradientColor = fullColorBuilder.setAlpha(154).build()
                val colorArrayBottom = intArrayOf(gradientColor, gradientColor)
                drawable.setGradient(colorArrayBottom, floatArrayOf(0f, 1f))
                textColor = ContextCompat.getColor(context, R.color.itin_white_text);
            }
            else {
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
