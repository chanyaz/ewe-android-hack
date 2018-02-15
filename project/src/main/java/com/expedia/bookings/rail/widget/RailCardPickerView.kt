package com.expedia.bookings.rail.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.RailCardPickerViewModel

class RailCardPickerView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val addButton: ImageButton by bindView(R.id.add_card)
    val removeButton: ImageButton by bindView(R.id.remove_card)
    val errorMessage: TextView by bindView(R.id.error_message_card)

    val enabledCardSelectorColor = ContextCompat.getColor(context, R.color.card_picker_add_button_color)
    val disabledCardSelectorColor = ContextCompat.getColor(context, R.color.card_picker_remove_button_color)

    init {
        View.inflate(context, R.layout.widget_rail_card_picker, this)
        orientation = VERTICAL

        enableButton(addButton)
        disableButton(removeButton)
    }

    var viewModel: RailCardPickerViewModel by notNullAndObservable { viewModel ->

        viewModel.railCardError.subscribe { errorString ->
            disableButton(removeButton)
            disableButton(addButton)
            errorMessage.visibility = View.VISIBLE
            errorMessage.text = errorString
        }

        addButton.subscribeOnClick(viewModel.addClickSubject)
        removeButton.subscribeOnClick(viewModel.removeClickSubject)

        viewModel.addView.subscribe { cardPickerRowView ->
            addView(cardPickerRowView)
        }

        viewModel.addClickSubject.onNext(Unit)

        viewModel.removeRow.subscribe {
            removeRow()
        }

        viewModel.removeButtonEnableState.subscribe { enabled ->
            if (enabled) enableButton(removeButton) else disableButton(removeButton)
        }

        viewModel.addButtonEnableState.subscribe { enabled ->
            if (enabled) enableButton(addButton) else disableButton(addButton)
        }

        viewModel.validationError.subscribe { message ->
            errorMessage.visibility = View.VISIBLE
            errorMessage.text = message
        }

        viewModel.validationSuccess.subscribe {
            if (viewModel.railCardError.value.isNullOrBlank()) {
                errorMessage.visibility = View.GONE
            }
        }
    }

    private fun removeRow() {
        removeViewAt(this.childCount - 1)
    }

    private fun enableButton(button: ImageButton) {
        button.isEnabled = true
        button.setColorFilter(enabledCardSelectorColor, PorterDuff.Mode.SRC_IN)
    }

    private fun disableButton(button: ImageButton) {
        button.isEnabled = false
        button.setColorFilter(disabledCardSelectorColor, PorterDuff.Mode.SRC_IN)
    }
}
