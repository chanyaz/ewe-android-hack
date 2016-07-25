package com.expedia.vm

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.insurance.InsurancePriceType
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.data.insurance.InsuranceTripParams
import com.expedia.bookings.services.InsuranceServices
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.SpannableLinkBuilder
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class InsuranceViewModel(val context: Context, val insuranceServices: InsuranceServices) {
    var lastResponse: FlightCreateTripResponse? = null
    val newTripObservable = BehaviorSubject.create<TripDetails>()
    val productObservable = BehaviorSubject.create<List<InsuranceProduct>>()
    val programmaticToggleObservable = PublishSubject.create<Boolean>()
    val termsObservable = PublishSubject.create<SpannableStringBuilder>()
    val titleObservable = PublishSubject.create<SpannableStringBuilder>()
    val titleColorObservable = PublishSubject.create<Int>()
    val userInitiatedToggleObservable = PublishSubject.create<Boolean>()
    val updatedTripObservable = PublishSubject.create<FlightCreateTripResponse>()

    val errorDialog: AlertDialog by lazy {
        AlertDialog.Builder(context).setPositiveButton(R.string.button_done, null).create()
    }

    val updatingTripDialog: ProgressDialog by lazy {
        val dialog = ProgressDialog(context)
        dialog.isIndeterminate = true
        dialog.setCancelable(false)
        dialog
    }

    lateinit var lastAction: InsuranceAction
    lateinit var product: InsuranceProduct
    lateinit var tripId: String

    enum class InsuranceAction {
        ADD, REMOVE
    }

    init {
        newTripObservable.subscribe { newTrip -> tripId = newTrip.tripId!! }

        productObservable.filter({ it.isNotEmpty() }).subscribe { products ->
            product = products.first()
            updateTerms()
            updateTitle()
        }

        userInitiatedToggleObservable.subscribe { isSelected ->
            if (isSelected) {
                lastAction = InsuranceAction.ADD
                FlightsV2Tracking.trackInsuranceAdd()
                updatingTripDialog.setMessage(context.resources.getString(R.string.insurance_adding))
                updatingTripDialog.show()
                insuranceServices.addInsuranceToTrip(InsuranceTripParams(tripId, product.productId)).subscribe(tripResponseObserver)
            } else {
                lastAction = InsuranceAction.REMOVE
                FlightsV2Tracking.trackInsuranceRemove()
                updatingTripDialog.setMessage(context.resources.getString(R.string.insurance_removing))
                updatingTripDialog.show()
                insuranceServices.removeInsuranceFromTrip(InsuranceTripParams(tripId)).subscribe(tripResponseObserver)
            }
        }
    }

    val tripResponseObserver = object : Observer<FlightCreateTripResponse> {
        fun handleError(message: String) {
            val messageId: Int
            when (lastAction) {
                InsuranceAction.ADD    -> messageId = R.string.insurance_add_error
                InsuranceAction.REMOVE -> messageId = R.string.insurance_remove_error
            }
            FlightsV2Tracking.trackInsuranceError(message)
            errorDialog.setMessage(context.resources.getString(messageId))
            errorDialog.show()
        }

        override fun onCompleted() {
            updatingTripDialog.hide()
        }

        override fun onError(e: Throwable) {
            updatingTripDialog.hide()
            programmaticToggleObservable.onNext(lastResponse?.selectedInsuranceProduct != null)
            handleError(e.toString())
        }

        override fun onNext(response: FlightCreateTripResponse) {
            lastResponse = response

            if (!response.hasErrors()) {
                updatedTripObservable.onNext(response)
            } else {
                handleError(response.errorsToString())
            }

            if (response.selectedInsuranceProduct != null) {
                programmaticToggleObservable.onNext(true)
                titleColorObservable.onNext(Ui.obtainThemeColor(context, R.attr.primary_color))
                updateTitle(true)
            } else {
                programmaticToggleObservable.onNext(false)
                titleColorObservable.onNext(ContextCompat.getColor(context, R.color.defaultTextColor))
                updateTitle(false)
            }
        }
    }

    fun updateTerms() {
        val linkContent = context.resources.getString(R.string.textview_spannable_hyperlink_TEMPLATE,
                product.terms.url, context.resources.getString(R.string.insurance_terms))
        termsObservable.onNext(SpannableLinkBuilder().withColor(Ui.obtainThemeColor(context, R.attr.primary_color))
                .withContent(linkContent).bubbleUpClicks().build())
    }

    fun updateTitle(isInsured: Boolean = false) {
        val titleId: Int
        if (isInsured) {
            titleColorObservable.onNext(Ui.obtainThemeColor(context, R.attr.primary_color))
            titleId = R.string.insurance_title_protected_TEMPLATE
        } else {
            titleColorObservable.onNext(ContextCompat.getColor(context, R.color.gray7))
            titleId = R.string.insurance_title_unprotected_TEMPLATE
        }

        val title = when (product.displayPriceType) {
            InsurancePriceType.PRICE_PER_DAY -> Phrase.from(context.resources, titleId)
                    .put("price", product.displayPrice.amount.toInt())
                    .put("price_type", context.resources.getString(R.string.insurance_price_per_day)).format()
            InsurancePriceType.PRICE_PER_PERSON -> Phrase.from(context.resources, titleId)
                    .put("price", product.displayPrice.amount.toInt())
                    .put("price_type", context.resources.getString(R.string.insurance_price_per_person)).format()
            else -> Phrase.from(context.resources, titleId).put("price", "").format()
        }

        // use a lightweight base font, and convert any bold spans to medium
        val spannedTitleBuilder = SpannableStringBuilder(Html.fromHtml(title.toString()))
        spannedTitleBuilder.setSpan(FontCache.getSpan(FontCache.Font.ROBOTO_LIGHT), 0, spannedTitleBuilder.length, 0)
        spannedTitleBuilder.getSpans(0, spannedTitleBuilder.length, StyleSpan::class.java).forEach { span ->
            if (span.style == Typeface.BOLD) {
                val start = spannedTitleBuilder.getSpanStart(span)
                val end = spannedTitleBuilder.getSpanEnd(span)
                spannedTitleBuilder.removeSpan(span)
                spannedTitleBuilder.setSpan(FontCache.getSpan(FontCache.Font.ROBOTO_MEDIUM), start, end, 0)
            }
        }

        titleObservable.onNext(spannedTitleBuilder)
    }
}
