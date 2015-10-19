package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.squareup.phrase.Phrase

public class HotelCheckoutSummaryWidget(context: Context, attrs: AttributeSet?, val viewModel: HotelCheckoutSummaryViewModel) : LinearLayout(context, attrs), HeaderBitmapDrawable.CallbackListener {

    val hotelName: android.widget.TextView by bindView(R.id.hotel_name)
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
        viewModel.priceChange.subscribeText(priceChange)
        viewModel.isPayLaterOrResortCase.subscribeVisibility(totalWithTaxLabelWithInfoButton.compoundDrawables[2], false)
        viewModel.isPayLaterOrResortCase.subscribeVisibility(amountDueTodayLabel.compoundDrawables[2], true)
        viewModel.newDataObservable.subscribe {
            amountDueTodayLabel.text = if (it.isPayLaterOrResortCase.value)
                                            Phrase.from(getContext(), R.string.due_to_brand_today_TEMPLATE).put("brand", BuildConfig.brand).format()
                                        else
                                            resources.getString(R.string.total_with_tax)
        }
        breakdown.viewmodel = HotelBreakDownViewModel(context, viewModel)
    }

    override fun onBitmapLoaded() {
    }

    override fun onBitmapFailed() {
    }

    override fun onPrepareLoad() {
    }
}
