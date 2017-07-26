package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FareFamilyViewModel
import com.mobiata.android.Log


class FareFamilyCardView(context: Context, attrs: AttributeSet) : CardView(context, attrs), View.OnClickListener {
    override fun onClick(view: View) {
        Log.d("its been clicked")
        viewModel.doneObservable.onNext(Unit)
    }

    val selectedClassTextView: TextView by bindView(R.id.selected_classes)
    val deltaPriceView: TextView by bindView(R.id.upgrade_delta_price)
    val fareFamilyTitle: TextView by bindView(R.id.fare_family_title)
    val fromLabel: TextView by bindView(R.id.fare_family_from_label)

    var viewModel: FareFamilyViewModel by notNullAndObservable { vm ->
        vm.selectedClassObservable.subscribeText(selectedClassTextView)
        vm.deltaPriceObservable.subscribeTextAndVisibility(deltaPriceView)
        vm.fromLabelVisibility.subscribeVisibility(fromLabel)
        vm.fareFamilyTitleObservable.subscribeText(fareFamilyTitle)
        vm.widgetVisibilityObservable.subscribeVisibility(this)
    }

    init {
        View.inflate(context, R.layout.fare_family_card_view, this)
        this.setOnClickListener(this)
    }
}
