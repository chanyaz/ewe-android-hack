package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.expedia.vm.launch.GenericViewModel
import com.squareup.phrase.Phrase

abstract class AbstractGenericPlaceholderCard(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView) {
    val firstLineTextView: TextView by bindView(R.id.first_line)
    val secondLineTextView: TextView by bindView(R.id.second_line)
    val button_one: TextView by bindView(R.id.button_one)
    val button_two: TextView by bindView(R.id.button_two)

    init {
        setupFonts()
    }

    fun bind(vm: GenericViewModel) {
        firstLineTextView.text = vm.firstLine
        secondLineTextView.text = vm.secondLine
        button_one.text = vm.buttonOneLabel
        button_one.contentDescription = Phrase.from(context, R.string.accessibility_cont_desc_role_button_TEMPLATE)
                .put("button_label", vm.buttonOneLabel)
                .format().toString()
        button_one.visibility = if (vm.buttonOneLabel.isNotBlank()) View.VISIBLE else View.GONE
        button_two.text = vm.buttonTwoLabel
        button_two.contentDescription = Phrase.from(context, R.string.accessibility_cont_desc_role_button_TEMPLATE)
                .put("button_label", vm.buttonTwoLabel)
                .format().toString()
        button_two.visibility = if (vm.buttonTwoLabel.isNotBlank()) View.VISIBLE else View.GONE
        button_one.letterSpacing = .05f
        button_two.letterSpacing = .05f
    }

    private fun setupFonts() {
        FontCache.setTypeface(firstLineTextView, FontCache.Font.ROBOTO_MEDIUM)
        FontCache.setTypeface(secondLineTextView, FontCache.Font.ROBOTO_REGULAR)
        FontCache.setTypeface(button_one, FontCache.Font.ROBOTO_MEDIUM)
        FontCache.setTypeface(button_two, FontCache.Font.ROBOTO_MEDIUM)
    }
}
