package com.expedia.bookings.presenter.trips

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeOnClick
import com.expedia.vm.itin.AddGuestItinViewModel
import com.expedia.vm.itin.ItinSignInViewModel

class AddGuestItinWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val viewModel= AddGuestItinViewModel(context)
    val findItinButton: Button by bindView(R.id.find_itinerary_button)

    init {
        View.inflate(context, R.layout.add_guest_itin_widget, this)
        println("Supreeth AddGuestItinWidget init")
        findItinButton.subscribeOnClick(viewModel.searchClickSubject)
    }
}