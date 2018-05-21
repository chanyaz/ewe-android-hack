package com.expedia.bookings.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.R
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.isRecentSearchesForFlightsEnabled
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.vm.TravelerPickerViewModel
import io.reactivex.subjects.BehaviorSubject

open class TravelerWidgetV2(context: Context, attrs: AttributeSet?) : SearchInputTextView(context, attrs) {
    private var oldTravelerData: TravelerParams? = null
    private var oldInfantPreferenceInLap: Boolean = true
    val travelersSubject = BehaviorSubject.create<TravelerParams>()

    init {
        setOnClickListener {
            travelerDialog.show()
        }
    }

    open val travelerDialogView: View by lazy {
        val view = getTravelerSearchView()
        view
    }

    @SuppressLint("InflateParams")
    private fun getTravelerSearchView(): View {
        return LayoutInflater.from(context).inflate(R.layout.widget_hotel_traveler_search, null)
    }

    open val traveler: BaseTravelerPickerView by lazy {
        val travelerView = travelerDialogView.findViewById<TravelerPickerView>(R.id.traveler_view)
        travelerView.viewmodel = TravelerPickerViewModel(context)
        travelerView.viewmodel.travelerParamsObservable.subscribe(travelersSubject)
        travelerView.viewmodel.guestsTextObservable.subscribeText(this)
        travelerView.viewmodel.showInfantErrorMessage.map { tooManyInfants ->
            Strings.isEmpty(tooManyInfants)
        }.subscribe { enable ->
            travelerDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = enable
        }
        travelerView
    }

    val travelerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
        traveler
        builder.setView(travelerDialogView)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, _ ->
            if (isRecentSearchesForFlightsEnabled(context)) {
                traveler.getViewModel().isTravelerSelectionChanged.onNext(!traveler.getViewModel().travelerParamsObservable.value.equalParams(oldTravelerData!!))
            }
            oldTravelerData = null
            traveler.getViewModel().isDefaultSelectionChangedObservable.onNext(traveler.getViewModel().travelerParamsObservable.value.getTravelerCount() != 1)
            dialog.dismiss()
        })
        val dialog: AlertDialog = builder.create()
        dialog.setOnShowListener {
            oldTravelerData = traveler.getViewModel().travelerParamsObservable.value
            oldInfantPreferenceInLap = traveler.getViewModel().isInfantInLapObservable.value
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog.setOnDismissListener {
            oldTravelerData?.let {
                //if it's not null, the user dismissed the dialog, otherwise we clear it on Done
                traveler.getViewModel().travelerParamsObservable.onNext(it)
                traveler.getViewModel().infantInSeatObservable.onNext(!oldInfantPreferenceInLap)
                traveler.getViewModel().isInfantInLapObservable.onNext(oldInfantPreferenceInLap)
                oldTravelerData = null
            }
            this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
        }
        dialog
    }
}
