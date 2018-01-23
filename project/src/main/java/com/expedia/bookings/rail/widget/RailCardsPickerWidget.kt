package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.R
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.subscribeText
import com.expedia.vm.RailCardPickerViewModel
import com.squareup.phrase.Phrase
import javax.inject.Inject
import kotlin.properties.Delegates

class RailCardsPickerWidget(context: Context, attrs: AttributeSet?) : SearchInputTextView(context, attrs) {

    lateinit var railServices: RailServices
        @Inject set

    var railCardPickerViewModel by Delegates.notNull<RailCardPickerViewModel>()

    init {
        Ui.getApplication(context).railComponent().inject(this)
        railCardPickerViewModel = RailCardPickerViewModel(railServices, context)

        contentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.search_rail_card_cont_desc_TEMPLATE, 0))
                .put("count", 0)
                .format().toString()

        setOnClickListener {
            cardsPickerDialog.show()
        }

        railCardPickerViewModel.validationSuccess.subscribe {
            cardsPickerDialog.setCancelable(true)
            cardsPickerDialog.dismiss()
        }

        railCardPickerViewModel.cardsListForSearchParams.subscribe { cardList ->
            val count = cardList.size
            contentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.search_rail_card_cont_desc_TEMPLATE, count))
                    .put("count", count)
                    .format().toString()
        }

        railCardPickerViewModel.cardsSelectedTextObservable.subscribeText(this)
    }

    val cardPickerDialogView: RailCardPickerView by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_rail_card_search, null) as RailCardPickerView
        view.viewModel = railCardPickerViewModel
        view
    }

    val cardsPickerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)

        builder.setView(cardPickerDialogView)
        builder.setPositiveButton(context.getString(R.string.DONE), null)
        val dialog: AlertDialog = builder.create()
        dialog.setOnShowListener {
            cardsPickerDialog.setCancelable(false)
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                railCardPickerViewModel.doneClickedSubject.onNext(Unit)
                this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
            }
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog.setOnDismissListener {
            this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
        }
        dialog
    }
}
