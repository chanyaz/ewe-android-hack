package com.expedia.vm

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import com.expedia.bookings.R
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
    val benefitsObservable = BehaviorSubject.create<Spanned>()
    val programmaticToggleObservable = PublishSubject.create<Boolean>()
    val termsObservable = PublishSubject.create<SpannableStringBuilder>()
    val titleColorObservable = PublishSubject.create<Int>()
    val titleObservable = PublishSubject.create<SpannableStringBuilder>()
    val tripObservable = BehaviorSubject.create<FlightCreateTripResponse>()
    val updatedTripObservable = PublishSubject.create<FlightCreateTripResponse>()
    val userInitiatedToggleObservable = PublishSubject.create<Boolean>()
    val widgetVisibilityObservable = PublishSubject.create<Boolean>()

    val errorDialog: AlertDialog by lazy {
        AlertDialog.Builder(context).setPositiveButton(R.string.button_done, null).create()
    }

    val hasProduct: Boolean get() = product != null

    var product: InsuranceProduct? = null

    val updatingTripDialog: ProgressDialog by lazy {
        val dialog = ProgressDialog(context)
        dialog.isIndeterminate = true
        dialog.setCancelable(false)
        dialog
    }

    lateinit var lastAction: InsuranceAction
    lateinit var trip: FlightCreateTripResponse

    enum class InsuranceAction {
        ADD, REMOVE
    }

    init {
        tripObservable.subscribe { tripResponse ->
            product = tripResponse.selectedInsuranceProduct ?: tripResponse.availableInsuranceProducts.firstOrNull()
            trip = tripResponse
            trip.tripId = trip.newTrip.tripId!!

            if (hasProduct) {
                updateBenefits()
                updateTerms()
                updateTitle()
            }
            updateToggleSwitch()
            updateVisibility()
        }

        userInitiatedToggleObservable.subscribe { isSelected ->
            if (isSelected) {
                lastAction = InsuranceAction.ADD
                updatingTripDialog.setMessage(context.resources.getString(R.string.insurance_adding))
                updatingTripDialog.show()
                insuranceServices.addInsuranceToTrip(InsuranceTripParams(trip.tripId, product!!.productId))
                        .subscribe(updatedTripObserver)
            } else {
                lastAction = InsuranceAction.REMOVE
                updatingTripDialog.setMessage(context.resources.getString(R.string.insurance_removing))
                updatingTripDialog.show()
                insuranceServices.removeInsuranceFromTrip(InsuranceTripParams(trip.tripId))
                        .subscribe(updatedTripObserver)
            }
            FlightsV2Tracking.trackInsuranceUpdated(if (isSelected) InsuranceAction.ADD else InsuranceAction.REMOVE)
        }
    }

    val updatedTripObserver = object : Observer<FlightCreateTripResponse> {
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
            updateToggleSwitch()
            updatingTripDialog.hide()
            handleError(e.toString())
        }

        override fun onNext(response: FlightCreateTripResponse) {
            tripObservable.onNext(response)

            if (!response.hasErrors()) {
                updatedTripObservable.onNext(response)
            } else {
                handleError(response.errorsToString())
            }
        }
    }

    fun updateBenefits() {
        val benefitsId: Int
        if (trip.details.offer.isInternational) {
            benefitsId = R.string.insurance_benefits_international
        } else {
            benefitsId = R.string.insurance_benefits_domestic
        }
        benefitsObservable.onNext(Html.fromHtml(context.resources.getString(benefitsId)))
    }

    fun updateTerms() {
        val linkContent = context.resources.getString(R.string.textview_spannable_hyperlink_TEMPLATE,
                product!!.terms.url, context.resources.getString(R.string.insurance_terms))
        termsObservable.onNext(SpannableLinkBuilder().withColor(Ui.obtainThemeColor(context, R.attr.primary_color))
                .withContent(linkContent).bubbleUpClicks().build())
    }

    fun updateTitle() {
        val titleId: Int
        if (trip.selectedInsuranceProduct != null) {
            titleColorObservable.onNext(Ui.obtainThemeColor(context, R.attr.primary_color))
            titleId = R.string.insurance_title_protected_TEMPLATE
        } else {
            titleColorObservable.onNext(ContextCompat.getColor(context, R.color.defaultTextColor))
            titleId = R.string.insurance_title_unprotected_TEMPLATE
        }

        val title = when (product!!.displayPriceType) {
            InsurancePriceType.PRICE_PER_DAY -> Phrase.from(context.resources, titleId)
                    .put("price", product!!.displayPrice.formattedWholePrice)
                    .put("price_type", context.resources.getString(R.string.insurance_price_per_day)).format()
            InsurancePriceType.PRICE_PER_PERSON -> Phrase.from(context.resources, titleId)
                    .put("price", product!!.displayPrice.formattedWholePrice)
                    .put("price_type", context.resources.getString(R.string.insurance_price_per_person)).format()
            else -> Phrase.from(context.resources, titleId).put("price", "").put("price_type", "").format()
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

    fun updateToggleSwitch() {
        programmaticToggleObservable.onNext(trip.selectedInsuranceProduct != null)
    }

    fun updateVisibility() {
        widgetVisibilityObservable.onNext(hasProduct)
    }
}
