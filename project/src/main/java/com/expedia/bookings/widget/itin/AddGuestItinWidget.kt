package com.expedia.bookings.presenter.trips

import android.app.Activity
import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.model.PointOfSaleStateModel
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeMaterialFormsError
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.itin.AddGuestItinViewModel
import com.mobiata.android.util.Ui
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AddGuestItinWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val findItinButton: Button by bindView(R.id.find_itinerary_button)
    val itinNumberEditText: TextInputEditText by bindView(R.id.itin_number_edit_text)
    val guestEmailEditText: EditText by bindView(R.id.email_edit_text)
    val loadingContainer: LinearLayout by bindView(R.id.loading_container)
    val addGuestFormFieldContainer: LinearLayout by bindView(R.id.outer_container)
    val unableToFindItinErrorText: TextView by bindView(R.id.unable_to_find_itin_error_message)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    lateinit var pointOfSaleStateModel: PointOfSaleStateModel
        @Inject set

    private var isResettingFields = false

    var viewModel: AddGuestItinViewModel by notNullAndObservable { vm ->
        vm.showSearchDialogObservable.subscribe { show ->
            if (show) {
                loadingContainer.visibility = View.VISIBLE
                addGuestFormFieldContainer.visibility = View.GONE
            } else {
                loadingContainer.visibility = View.GONE
                addGuestFormFieldContainer.visibility = View.VISIBLE
            }
            Ui.hideKeyboard(this)
        }
        vm.showErrorObservable.subscribeVisibility(unableToFindItinErrorText)
        vm.showErrorMessageObservable.subscribeText(unableToFindItinErrorText)
        vm.emailFieldFocusObservable.subscribe {
            if (!AccessibilityUtil.isTalkBackEnabled(getContext())) {
                guestEmailEditText.requestFocus()
                postDelayed({ Ui.showKeyboard(guestEmailEditText, null) }, 200L)
            }
        }
    }

    init {
        View.inflate(context, R.layout.add_guest_itin_widget, this)
        com.expedia.bookings.utils.Ui.getApplication(context).appComponent().inject(this)
        val tripComponent = com.expedia.bookings.utils.Ui.getApplication(context).tripComponent()
        val itinPageUsablePerformanceModel = tripComponent.itinPageUsableTracking()
        orientation = VERTICAL
        viewModel = AddGuestItinViewModel(context)
        viewModel.addItinSyncListener()
        findItinButton.setOnClickListener {
            if (!viewModel.isEmailValid(guestEmailEditText.text.toString()) ||
                    !viewModel.isItinNumberValid(itinNumberEditText.text.toString()) ) {
                validateField(viewModel.itinNumberValidateObservable, itinNumberEditText.text.toString())
                validateField(viewModel.emailValidateObservable, guestEmailEditText.text.toString())
            } else {
                Ui.hideKeyboard(this)
                viewModel.performGuestTripSearch.onNext(Pair(guestEmailEditText.text.toString(), itinNumberEditText.text.toString()))
                itinPageUsablePerformanceModel.markSuccessfulStartTime(System.currentTimeMillis())
            }
        }

        toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_close_cont_desc)

        itinNumberEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                validateField(viewModel.itinNumberValidateObservable, itinNumberEditText.text.toString())
            }
        }

        guestEmailEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                validateField(viewModel.emailValidateObservable, guestEmailEditText.text.toString())
            }
        }

        guestEmailEditText.subscribeMaterialFormsError(viewModel.hasEmailErrorObservable, R.string.email_validation_error_message)
        itinNumberEditText.subscribeMaterialFormsError(viewModel.hasItinErrorObservable, R.string.itinerary_number_error_message)

        toolbar.setNavigationOnClickListener {
            viewModel.toolBarVisibilityObservable.onNext(true)
            (context as Activity).onBackPressed()
        }

        pointOfSaleStateModel.pointOfSaleChangedSubject.subscribe {
            resetFields()
        }
    }

    private fun validateField(validationObservable: PublishSubject<String>, string: String) {
        if (!isResettingFields) {
            validationObservable.onNext(string)
        }
    }

    fun resetFields() {
        isResettingFields = true
        guestEmailEditText.setText("")
        itinNumberEditText.setText("")
        viewModel.hasItinErrorObservable.onNext(false)
        viewModel.hasEmailErrorObservable.onNext(false)
        isResettingFields = false
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.find_guest_itinerary_title)
    }
}