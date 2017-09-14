package com.expedia.bookings.hotel.widget

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.setTextAndVisibility
import com.expedia.util.updateVisibility
import com.expedia.vm.BaseHotelDetailPriceViewModel

class HotelDetailPriceView(context: Context, attrs: AttributeSet): RelativeLayout(context, attrs) {

    private val soldOutTextView: TextView by bindView(R.id.sold_out_text_view)
    private val strikeThroughPriceTextView: TextView by bindView(R.id.strike_through_price_text_view)
    private val priceContainer: LinearLayout by bindView(R.id.price_per_container)
    private val priceTextView: TextView by bindView(R.id.price_text_view)
    private val pricePerDescriptorTextView: TextView by bindView(R.id.price_per_descriptor_text_view)
    private val taxFeeDescriptorTextView: TextView by bindView(R.id.tax_fee_descriptor_text_view)
    private val earnMessageTextView: TextView by bindView(R.id.earn_message_text_view)
    private val searchInfoTextView: TextView by bindView(R.id.search_info_text_view)

    private lateinit var viewModel: BaseHotelDetailPriceViewModel

    private var soldOut: Boolean? = null

    init {
        View.inflate(context, R.layout.hotel_detail_price_search_info, this)

        strikeThroughPriceTextView.paintFlags = strikeThroughPriceTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun bindViewModel(viewModel: BaseHotelDetailPriceViewModel) {
        this.viewModel = viewModel

        searchInfoTextView.setTextAndVisibility(viewModel.getSearchInfoString())

        viewModel.isSoldOut.subscribe { soldOut ->
            if (this.soldOut != soldOut) {
                setupViews()
                handleSoldOut(soldOut)
                this.soldOut = soldOut
            }
        }

        priceContainer.contentDescription = viewModel.getPriceContainerContentDescriptionString()
    }

    private fun setupViews() {
        strikeThroughPriceTextView.setTextAndVisibility(viewModel.getStrikeThroughPriceString())
        priceTextView.setTextAndVisibility(viewModel.getPriceString())
        pricePerDescriptorTextView.setTextAndVisibility(viewModel.getPerDescriptorString())
        taxFeeDescriptorTextView.setTextAndVisibility(viewModel.getTaxFeeDescriptorString())
        earnMessageTextView.setTextAndVisibility(viewModel.getEarnMessageString())
        searchInfoTextView.setTextColor(viewModel.getSearchInfoTextColor())
    }

    private fun handleSoldOut(soldOut: Boolean) {
        soldOutTextView.updateVisibility(soldOut)
        priceContainer.updateVisibility(!soldOut)
    }
}
