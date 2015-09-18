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
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribe
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.squareup.phrase.Phrase
import kotlin.properties.Delegates

public class HotelCheckoutSummaryWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), HeaderBitmapDrawable.CallbackListener {

    val hotelName: android.widget.TextView by bindView(R.id.hotel_name)
    val date: android.widget.TextView by bindView(R.id.check_in_out_dates)
    val address: android.widget.TextView by bindView(R.id.address_line_one)
    val cityState: android.widget.TextView by bindView(R.id.address_city_state)
    val selectedRoom: android.widget.TextView by bindView(R.id.selected_room)
    val selectedBed: android.widget.TextView by bindView(R.id.selected_bed)
    val numberNights: android.widget.TextView by bindView(R.id.number_nights)
    val numberGuests: android.widget.TextView by bindView(R.id.number_guests)
    val freeCancellationView: android.widget.TextView by bindView(R.id.free_cancellation_text)
    val totalPriceWithTax: android.widget.TextView by bindView(R.id.total_price_with_tax)
    val totalFees: android.widget.TextView by bindView(R.id.total_fees)
    val totalPriceWithTaxAndFees: android.widget.TextView by bindView(R.id.total_price_with_tax_and_fees)
    val amountDueTodayLabel: android.widget.TextView by bindView(R.id.amount_due_today_label)
    val bestPriceGuarantee: android.widget.TextView by bindView(R.id.best_price_guarantee)
    val saleMessage: android.widget.TextView by bindView(R.id.sale_text)
    val info: android.widget.TextView by bindView(R.id.total_tax_label)
    val costSummary: RelativeLayout by bindView(R.id.cost_summary)
    val priceChangeLayout: LinearLayout by bindView(R.id.price_change_container)
    val priceChange: android.widget.TextView by bindView(R.id.price_change_text)
    val breakdown = HotelBreakDownView(context, null)
    val dialog: AlertDialog by Delegates.lazy {
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

    var viewmodel: HotelCheckoutSummaryViewModel by notNullAndObservable { vm ->
        vm.hotelName.subscribe(hotelName)
        vm.checkinDates.subscribe(date)
        vm.address.subscribe(address)
        vm.city.subscribe(cityState)
        vm.hasFreeCancellation.subscribeVisibility(freeCancellationView)
        vm.roomDescriptions.subscribe(selectedRoom)
        vm.bedDescriptions.subscribe(selectedBed)
        vm.numNights.subscribe(numberNights)
        vm.numGuests.subscribe(numberGuests)
        vm.totalMandatoryPrices.subscribe(totalPriceWithTaxAndFees)
        vm.feePrices.subscribe(totalFees)
        vm.totalPrices.subscribe(totalPriceWithTax)
        vm.isBestPriceGuarantee.subscribeVisibility(bestPriceGuarantee)
        vm.isPriceChange.subscribeVisibility(priceChangeLayout)
        vm.priceChange.subscribe(priceChange)

        vm.discounts.map{ it != null }.subscribeVisibility(saleMessage)
        vm.discounts.map{ it?.toString() ?: "" }.subscribe(saleMessage)
    }

    init {
        setOrientation(LinearLayout.VERTICAL)
        View.inflate(getContext(), R.layout.hotel_checkout_summary_widget, this)
        breakdown.viewmodel = HotelBreakDownViewModel(context)
        val amountDueLabel = Phrase.from(getContext(), R.string.due_to_brand_today_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
        amountDueTodayLabel.setText(amountDueLabel)
        costSummary.setOnClickListener {
            dialog.show()
        }
    }

    override fun onBitmapLoaded() {
    }

    override fun onBitmapFailed() {
    }

    override fun onPrepareLoad() {
    }
}