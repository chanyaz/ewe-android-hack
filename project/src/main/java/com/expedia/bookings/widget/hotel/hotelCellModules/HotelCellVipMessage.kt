package com.expedia.bookings.widget.hotel.hotelCellModules

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelViewModel
import rx.Observable

class HotelCellVipMessage(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val vipMessageTextView: TextView by bindView(R.id.vip_message)
    val vipLoyaltyMessageTextView: TextView by bindView(R.id.vip_loyalty_message)

    init {
        View.inflate(context, R.layout.hotel_cell_vip_message, this)

        val attrSet = context.obtainStyledAttributes(attrs, R.styleable.HotelCellVipMessage, 0, 0)
        val textColor = attrSet.getColor(R.styleable.HotelCellVipMessage_vip_text_color, vipMessageTextView.currentTextColor)
        vipMessageTextView.setTextColor(textColor)
        vipLoyaltyMessageTextView.setTextColor(textColor)

        attrSet.recycle()
    }

    fun bindHotelViewModel(viewModel: HotelViewModel) {
        viewModel.vipMessageVisibilityObservable.subscribeVisibility(vipMessageTextView)
        viewModel.vipLoyaltyMessageVisibilityObservable.subscribeVisibility(vipLoyaltyMessageTextView)
        Observable.combineLatest(viewModel.vipMessageVisibilityObservable, viewModel.vipLoyaltyMessageVisibilityObservable, { vipMessageVisibility, vipLoyaltyVisibility ->
            vipMessageVisibility || vipLoyaltyVisibility
        }).subscribeVisibility(this)
    }
}