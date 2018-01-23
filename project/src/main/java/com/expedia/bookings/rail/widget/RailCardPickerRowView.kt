package com.expedia.bookings.rail.widget

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SpinnerAdapterWithHint
import com.expedia.util.notNullAndObservable
import com.expedia.vm.RailCardPickerRowViewModel

class RailCardPickerRowView(context: Context) : LinearLayout(context) {

    val cardTypeSpinner: Spinner by bindView(R.id.card_type_spinner)
    val cardQuantitySpinner: Spinner by bindView(R.id.card_quantity_spinner)
    private val cardTypeSpinnerHint = context.resources.getString(R.string.select_rail_card_hint)
    private val cardQuantitySpinnerHint = context.resources.getString(R.string.select_rail_card_quantity_hint)

    val railCardAdapter = SpinnerAdapterWithHint(context, cardTypeSpinnerHint, R.layout.rail_card_spinner_item_view,
            R.layout.rail_card_dropdown_item)
    val cardQuantityAdapter = SpinnerAdapterWithHint(context, cardQuantitySpinnerHint, R.layout.snippet_rail_card_quantity_view)

    var viewModel by notNullAndObservable<RailCardPickerRowViewModel> { vm ->
        vm.cardTypesList.subscribe { cardTypes ->
            val cardTypeOptions = cardTypes.map { cardType ->
                SpinnerAdapterWithHint.SpinnerItem(cardType.name, cardType)
            }
            railCardAdapter.dataSetChanged(cardTypeOptions)
            post({ cardTypeSpinner.setSelection(railCardAdapter.count) })
        }

        vm.resetRow.subscribe {
            cardTypeSpinner.setSelection(railCardAdapter.count)
            cardQuantitySpinner.setSelection(cardQuantityAdapter.count)
        }

        val cardQuantityOptions = IntRange(1, 8).map { SpinnerAdapterWithHint.SpinnerItem(it.toString(), it) }
        cardQuantityAdapter.dataSetChanged(cardQuantityOptions)
        post({ cardQuantitySpinner.setSelection(cardQuantityAdapter.count) })
    }

    init {
        View.inflate(context, R.layout.widget_rail_card_picker_row, this)

        cardTypeSpinner.adapter = railCardAdapter
        cardQuantitySpinner.adapter = cardQuantityAdapter

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
