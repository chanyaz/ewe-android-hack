package com.expedia.bookings.widget

import android.content.Context
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelRoomDetailViewModel
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.data.hotel.HotelValueAdd
import com.expedia.util.LoyaltyUtil
import com.expedia.util.setInverseVisibility
import com.expedia.util.setTextAndVisibility
import com.expedia.util.updateVisibility
import rx.subjects.PublishSubject

class HotelRoomDetailView(context: Context, val viewModel: HotelRoomDetailViewModel) : RelativeLayout(context) {

    private val optionTextView: TextView by bindView(R.id.option_text_view)
    private val cancellationTextView: TextView by bindView(R.id.cancellation_text_view)
    private val cancellationTimeTextView: TextView by bindView(R.id.cancellation_time_text_view)
    private val valueAddsContainer: LinearLayout by bindView(R.id.value_adds_container)
    private val earnMessageTextView: TextView by bindView(R.id.earn_message_text_view)
    private val mandatoryFeeTextView: TextView by bindView(R.id.mandatory_fee_text_view)
    private val memberOnlyDealTag: ImageView by bindView(R.id.member_only_deal_tag)
    private val discountPercentageTextView: TextView by bindView(R.id.discount_percentage_text_view)
    private val payLaterPriceTextView: TextView by bindView(R.id.pay_later_price_text_view)
    private val depositTermsTextView: TextView by bindView(R.id.deposit_terms_text_view)
    private val strikeThroughTextView: TextView by bindView(R.id.strike_through_price_text_view)
    private val priceTextView: TextView by bindView(R.id.price_text_view)
    private val pricePerDescriptorTextView: TextView by bindView(R.id.price_per_descriptor_text_view)
    private val hotelRoomRowButton: HotelRoomRateActionButton by bindView(R.id.hotel_room_row_button)
    private val roomLeftContainer: LinearLayout by bindView(R.id.room_left_container)
    private val urgencyIcon: ImageView by bindView(R.id.urgency_icon)
    private val roomLeftTextView: TextView by bindView(R.id.room_left_text_view)

    val depositTermsClickedSubject = PublishSubject.create<Unit>()
    val hotelRoomRowClickedSubject = PublishSubject.create<Unit>()

    init {
        View.inflate(context, R.layout.hotel_room_detail, this)

        hotelRoomRowButton.showBookButton()

        val infoIcon = ContextCompat.getDrawable(context, R.drawable.details_info).mutate()
        infoIcon.setColorFilter(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)), PorterDuff.Mode.SRC_IN)
        depositTermsTextView.setCompoundDrawablesWithIntrinsicBounds(infoIcon, null, null, null)

        strikeThroughTextView.paintFlags = strikeThroughTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        val urgencyIconDrawable = ContextCompat.getDrawable(context, R.drawable.urgency).mutate()
        urgencyIconDrawable.setColorFilter(ContextCompat.getColor(context, R.color.hotel_urgency_message_color), PorterDuff.Mode.SRC_IN)
        urgencyIcon.setImageDrawable(urgencyIconDrawable)

        bindViewModel(viewModel)
    }

    fun bindViewModel(viewModel: HotelRoomDetailViewModel) {
        optionTextView.setTextAndVisibility(viewModel.optionString)
        cancellationTextView.setTextAndVisibility(viewModel.cancellationString)
        cancellationTimeTextView.setTextAndVisibility(viewModel.cancellationTimeString)
        val valueAddsToShow = viewModel.getValueAdds()
        if (valueAddsToShow.count() >= 0) {
            val srcColor = ContextCompat.getColor(context, R.color.hotelsv2_amenity_icon_color)
            val filter = PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP)

            valueAddsContainer.visibility = View.VISIBLE
            valueAddsToShow.forEach { valueAdd ->
                createValueAddTextView(valueAdd, filter, valueAddsContainer)
            }
        } else {
            valueAddsContainer.visibility = View.GONE
        }
        earnMessageTextView.setTextAndVisibility(viewModel.earnMessageString)
        mandatoryFeeTextView.setTextAndVisibility(viewModel.mandatoryFeeString)
        memberOnlyDealTag.updateVisibility(viewModel.showMemberOnlyDealTag)
        discountPercentageTextView.setTextAndVisibility(viewModel.discountPercentageString)
        discountPercentageTextView.setBackground(viewModel.discountPercentageBackground)
        discountPercentageTextView.setTextColor(viewModel.discountPercentageTextColor)
        payLaterPriceTextView.setTextAndVisibility(viewModel.payLaterPriceString)
        depositTermsTextView.updateVisibility(viewModel.showDepositTerm)
        depositTermsTextView.subscribeOnClick(depositTermsClickedSubject)

        val isShopWithPoints = LoyaltyUtil.isShopWithPoints(viewModel.hotelRoomResponse.rateInfo.chargeableRateInfo)
        val isAirAttached = viewModel.hotelRoomResponse.rateInfo.chargeableRateInfo.airAttached

        if (isShopWithPoints || !isAirAttached) {
            strikeThroughTextView.setTextAndVisibility(viewModel.strikeThroughString)
        }

        priceTextView.setTextAndVisibility(viewModel.priceString)
        pricePerDescriptorTextView.updateVisibility(viewModel.showPerNight)
        mandatoryFeeTextView.setTextAndVisibility(viewModel.mandatoryFeeString)

        hotelRoomRowButton.bookButtonClickedSubject.subscribe(hotelRoomRowClickedSubject)

        hotelRoomRowButton.setBookButtonText(viewModel.hotelRoomRowButtonString)
        hotelRoomRowButton.bookButton.contentDescription = viewModel.bookButtonContentDescriptionString

        viewModel.roomSoldOut.subscribe { soldOut ->
            if (soldOut) {
                hotelRoomRowButton.showSoldOutButton()
            } else {
                hotelRoomRowButton.hideSoldOutButton()
            }
        }

        roomLeftContainer.setInverseVisibility(viewModel.roomLeftString.isNullOrBlank())
        roomLeftTextView.setTextAndVisibility(viewModel.roomLeftString)
    }

    private fun createValueAddTextView(valueAdd: HotelValueAdd, filter: ColorFilter, viewGroup: ViewGroup) {
        val valueAddLayout = Ui.inflate<LinearLayout>(R.layout.room_value_add_row, viewGroup, false)
        val valueAddTextView = valueAddLayout.findViewById<android.widget.TextView>(R.id.value_add_label)
        val valueAddIconView = valueAddLayout.findViewById<ImageView>(R.id.value_add_icon)

        val icon = ContextCompat.getDrawable(context, valueAdd.iconId)
        icon.colorFilter = filter

        valueAddTextView.text = valueAdd.apiDescription
        valueAddIconView.setImageDrawable(icon)

        viewGroup.addView(valueAddLayout)
    }
}
