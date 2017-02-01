package com.expedia.bookings.presenter.trips

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeOnClick
import com.expedia.vm.itin.AddGuestItinViewModel

class AddGuestItinWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val viewModel= AddGuestItinViewModel(context)
    val findItinButton: Button by bindView(R.id.find_itinerary_button)

    init {
        View.inflate(context, R.layout.add_guest_itin_widget, this)
        findItinButton.subscribeOnClick(viewModel.searchClickSubject)
    }
}