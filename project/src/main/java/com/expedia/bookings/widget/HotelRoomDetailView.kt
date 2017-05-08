package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Paint
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelRoomDetailViewModel
import android.widget.ImageView
import android.widget.LinearLayout
import rx.subjects.PublishSubject

class HotelRoomDetailView(context: Context, val viewModel: HotelRoomDetailViewModel) : RelativeLayout(context) {

    private val optionTextView: TextView by bindView(R.id.option_text_view)
    private val cancellationTextView: TextView by bindView(R.id.cancellation_text_view)
    private val cancellationTimeTextView: TextView by bindView(R.id.cancellation_time_text_view)
    private val amenitiesContainer: LinearLayout by bindView(R.id.amenities_container)
    private val earnMessageTextView: TextView by bindView(R.id.earn_message_text_view)
    private val mandatoryFeeTextView: TextView by bindView(R.id.mandatory_fee_text_view)
    private val discountPercentageTextView: TextView by bindView(R.id.discount_percentage_text_view)
    private val payLaterPriceTextView: TextView by bindView(R.id.pay_later_price_text_view)
    private val depositTermsTextView: TextView by bindView(R.id.deposit_terms_text_view)
    private val strikeThroughTextView: TextView by bindView(R.id.strike_through_price_text_view)
    private val pricePerNightTextView: TextView by bindView(R.id.price_per_night_text_view)
    private val perNightTextView: TextView by bindView(R.id.per_night_text_view)
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

        pricePerNightTextView.setTextColor(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))
        perNightTextView.setTextColor(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))

        val urgencyIconDrawable = ContextCompat.getDrawable(context, R.drawable.urgency).mutate()
        urgencyIconDrawable.setColorFilter(ContextCompat.getColor(context, R.color.hotel_urgency_message_color), PorterDuff.Mode.SRC_IN)
        urgencyIcon.setImageDrawable(urgencyIconDrawable)

        bindViewModel(viewModel)
    }

    fun bindViewModel(viewModel: HotelRoomDetailViewModel) {
        setTextAndVisibility(optionTextView, viewModel.optionString)
        setTextAndVisibility(cancellationTextView, viewModel.cancellationString)
        setTextAndVisibility(cancellationTimeTextView, viewModel.cancellationTimeString)
        if (viewModel.amenityToShow.count() >= 0) {
            amenitiesContainer.visibility = View.VISIBLE
            viewModel.amenityToShow.forEach { amenity ->
                amenitiesContainer.addView(createAmenityTextView(amenity.first, amenity.second))
            }
        } else {
            amenitiesContainer.visibility = View.GONE
        }
        setTextAndVisibility(earnMessageTextView, viewModel.earnMessageString)
        setTextAndVisibility(mandatoryFeeTextView, viewModel.mandatoryFeeString)
        setTextAndVisibility(discountPercentageTextView, viewModel.discountPercentageString)
        setTextAndVisibility(payLaterPriceTextView, viewModel.payLaterPriceString)
        setVisibility(depositTermsTextView, viewModel.showDepositTerm)
        depositTermsTextView.subscribeOnClick(depositTermsClickedSubject)
        setTextAndVisibility(strikeThroughTextView, viewModel.strikeThroughString)
        setTextAndVisibility(pricePerNightTextView, viewModel.pricePerNightString)
        setVisibility(perNightTextView, viewModel.showPerNight)
        setTextAndVisibility(mandatoryFeeTextView, viewModel.mandatoryFeeString)

        hotelRoomRowButton.bookButtonClickedSubject.subscribe(hotelRoomRowClickedSubject)

        if (viewModel.isPackage) {
            hotelRoomRowButton.setSelectButtonText(context.getString(R.string.select))
            hotelRoomRowButton.showViewRoomButton()
        }

        viewModel.roomSoldOut.subscribe { soldOut ->
            if (soldOut) {
                hotelRoomRowButton.showSoldOutButton()
            } else {
                hotelRoomRowButton.hideSoldOutButton()
            }
        }

        roomLeftContainer.visibility = if (viewModel.roomLeftString.isNullOrBlank()) View.GONE else View.VISIBLE
        setTextAndVisibility(roomLeftTextView, viewModel.roomLeftString)
    }

    private fun setTextAndVisibility(textView: TextView, text: CharSequence?) {
        if (text.isNullOrBlank()) {
            textView.visibility = View.GONE
        } else {
            textView.text = text
            textView.visibility = View.VISIBLE
        }
    }

    private fun setVisibility(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun createAmenityTextView(description: String, iconId: Int) : TextView {
        val amenityView = TextView(context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, context.resources.getDimensionPixelSize(R.dimen.hotel_room_amenity_margin), 0, 0)
        amenityView.layoutParams = params

        amenityView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.toFloat())
        amenityView.setTextColor(ContextCompat.getColor(context, R.color.light_text_color))

        amenityView.compoundDrawablePadding = context.resources.getDimensionPixelSize(R.dimen.textview_drawable_padding)
        amenityView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0)

        amenityView.text = description

        return amenityView
    }
}
