package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.vm.AcceptTermsViewModel

class AcceptTermsWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val acceptButton: Button by bindView(R.id.i_accept_terms_button)
    var vm: AcceptTermsViewModel

    init {
        View.inflate(context, R.layout.accept_terms_layout, this)
        vm = AcceptTermsViewModel()
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
    }

    override fun onFinishInflate() {
        acceptButton.setOnClickListener {
            this.visibility = View.INVISIBLE
            vm.acceptedTermsObservable.onNext(true)
        }
    }
}
