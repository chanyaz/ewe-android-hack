package com.expedia.bookings.widget

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.RailCardPickerRowViewModel

class RailCardPickerRowView(context: Context): LinearLayout(context) {

    val cardTypeSpinner: Spinner by bindView(R.id.card_type_spinner)
    val cardQuantitySpinner: Spinner by bindView(R.id.card_quantity_spinner)
    private val cardTypeSpinnerHint = context.resources.getString(R.string.select_rail_card_hint)
    private val cardQuantitySpinnerHint = context.resources.getString(R.string.select_rail_card_quantity_hint)

    var viewModel by notNullAndObservable<RailCardPickerRowViewModel> { vm ->
        vm.cardTypesList.subscribe { cardTypes ->
            val railCardAdapter = SpinnerAdapterWithHint(context, cardTypes.map { cardType ->
                SpinnerAdapterWithHint.SpinnerItem(cardType.name, cardType)
            }, cardTypeSpinnerHint)
            cardTypeSpinner.adapter = railCardAdapter
            cardTypeSpinner.setSelection(railCardAdapter.count)
        }
    }

    init {
        View.inflate(context, R.layout.widget_rail_card_picker_row, this)

        val cardQuantityAdapter = SpinnerAdapterWithHint(context,
                IntRange(1,8).map { SpinnerAdapterWithHint.SpinnerItem(it.toString(), it) },
                cardQuantitySpinnerHint)
        cardQuantitySpinner.adapter = cardQuantityAdapter
        cardQuantitySpinner.setSelection(cardQuantityAdapter.count)

        cardTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Ignore
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = cardTypeSpinner.adapter.getItem(position) as SpinnerAdapterWithHint.SpinnerItem
                val isSelectedItemHint = selectedItem.value.equals(cardTypeSpinnerHint)
                val cardTypeSelected = if (isSelectedItemHint) RailCard("", "", "") else selectedItem.item as RailCard
                viewModel.cardTypeSelected.onNext(cardTypeSelected)
                // If a card type is selected, select quantity as 1.
                val selectedQuantity = cardQuantitySpinner.selectedItem as SpinnerAdapterWithHint.SpinnerItem
                if (!isSelectedItemHint && selectedQuantity.value.equals(cardQuantitySpinnerHint)) {
                    cardQuantitySpinner.setSelection(0)
                }
            }

        }

        cardQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Ignore
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = cardQuantitySpinner.adapter.getItem(position) as SpinnerAdapterWithHint.SpinnerItem
                val cardQuantity = if (selectedItem.value.equals(cardQuantitySpinnerHint)) 0 else selectedItem.item as Int
                viewModel.cardQuantitySelected.onNext(cardQuantity)
            }

        }
    }

}