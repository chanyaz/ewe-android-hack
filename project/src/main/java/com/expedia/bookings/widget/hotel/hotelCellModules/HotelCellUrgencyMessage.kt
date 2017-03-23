package com.expedia.bookings.widget.hotel.hotelCellModules

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeBackgroundColor
import com.expedia.util.subscribeImageDrawable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelViewModel

class HotelCellUrgencyMessage(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val urgencyIconImageView: ImageView by bindView(R.id.urgency_icon)
    val urgencyMessageTextView: TextView by bindView(R.id.urgency_message)

    init {
        View.inflate(context, R.layout.hotel_cell_urgency_message, this)
    }

    fun bindHotelViewModel(viewModel: HotelViewModel) {
        viewModel.urgencyIconObservable.subscribeImageDrawable(urgencyIconImageView)
        viewModel.urgencyIconVisibilityObservable.subscribeVisibility(urgencyIconImageView)

        viewModel.urgencyMessageBoxObservable.subscribeText(urgencyMessageTextView)
        viewModel.urgencyMessageTextColorObservable.subscribeTextColor(urgencyMessageTextView)

        viewModel.urgencyMessageVisibilityObservable.subscribeVisibility(this)
        viewModel.urgencyMessageBackgroundObservable.subscribeBackgroundColor(this)
    }
}
