package com.expedia.vm

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.insurance.InsurancePriceType
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.data.insurance.InsuranceTripParams
import com.expedia.bookings.services.InsuranceServices
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.SpannableLinkBuilder
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class InsuranceViewModel(private val context: Context, private val insuranceServices: InsuranceServices) {
    // inputs
    val tripObservable = BehaviorSubject.create<FlightTripResponse>()
    val userInitiatedToggleObservable = PublishSubject.create<Boolean>()
    val widgetVisibilityAllowedObservable = BehaviorSubject.create<Boolean>()

    // outputs
    val benefitsObservable = BehaviorSubject.create<Spanned>()
    val programmaticToggleObservable = PublishSubject.create<Boolean>()
    val termsObservable = PublishSubject.create<SpannableStringBuilder>()
    val titleColorObservable = PublishSubject.create<Int>()
    val titleObservable = PublishSubject.create<SpannableStringBuilder>()
    val updatedTripObservable = PublishSubject.create<FlightCreateTripResponse>()
    val widgetVisibilityObservable = PublishSubject.create<Boolean>()
    val toggleSwitchEnabledObservable = PublishSubject.create<Boolean>()

    private var canWidgetBeDisplayed: Boolean = true
    private val haveProduct: Boolean get() = product != null
    private var product: InsuranceProduct? = null

    lateinit private var lastAction: InsuranceAction
    lateinit private var trip: FlightTripResponse
    lateinit var tripId: String

    enum class InsuranceAction {
        ADD, REMOVE
    }

    private val errorDialog: AlertDialog by lazy {
        AlertDialog.Builder(context).setPositiveButton(R.string.button_done, null).create()
    }

    private val updatingTripDialog: ProgressDialog by lazy {
        val dialog = ProgressDialog(context)
        dialog.isIndeterminate = true
        dialog.setCancelable(false)
        dialog
    }

    init {
        tripObservable.subscribe { tripResponse ->
            trip = tripResponse
            product = tripResponse.getSelectedInsuranceProduct() ?: tripResponse.getAvailableInsuranceProducts().firstOrNull()
            trip.newTrip?.tripId?.let { tripId = it }

            if (haveProduct) {
                updateBenefits()
                updateTerms()
                updateTitle()
            }
            updateToggleSwitch()
            updateVisibility()
        }

        userInitiatedToggleObservable.subscribe { isSelected ->
            toggleSwitchEnabledObservable.onNext(false)
            if (isSelected) {
                lastAction = InsuranceAction.ADD
                updatingTripDialog.setMessage(context.resources.getString(R.string.insurance_adding))
                updatingTripDialog.show()
                insuranceServices.addInsuranceToTrip(InsuranceTripParams(tripId, product!!.productId))
                        .subscribe(insuranceSelectionUpdatedObserver)
            } else {
                lastAction = InsuranceAction.REMOVE
                updatingTripDialog.setMessage(context.resources.getString(R.string.insurance_removing))
                updatingTripDialog.show()
                insuranceServices.removeInsuranceFromTrip(InsuranceTripParams(tripId))
                        .subscribe(insuranceSelectionUpdatedObserver)
            }
            FlightsV2Tracking.trackInsuranceUpdated(if (isSelected) InsuranceAction.ADD else InsuranceAction.REMOVE)
        }

        widgetVisibilityAllowedObservable.subscribe {
            canWidgetBeDisplayed = it
            updateVisibility()
        }
    }

    val insuranceSelectionUpdatedObserver = object : Observer<FlightCreateTripResponse> {
        override fun onSubscribe(d: Disposable) {
            //ignore
        }

        private fun handleError(message: String) {
            FlightsV2Tracking.trackInsuranceError(message)

            val displayMessage = context.resources.getString(when (lastAction) {
                InsuranceAction.ADD    -> R.string.insurance_add_error
                InsuranceAction.REMOVE -> R.string.insurance_remove_error
            })

            errorDialog.setMessage(displayMessage)
            errorDialog.show()
        }

        override fun onComplete() {
            toggleSwitchEnabledObservable.onNext(true)
            updatingTripDialog.dismiss()
        }

        override fun onError(e: Throwable) {
            toggleSwitchEnabledObservable.onNext(true)
            updateToggleSwitch()
            updatingTripDialog.dismiss()
            handleError(e.toString())
        }

        override fun onNext(response: FlightCreateTripResponse) {
            tripObservable.onNext(response)

            if (!response.hasErrors()) {

                // NOTE: Populating details.offer totalPrice with correct totalPrice (including insurance) from top level of response
                // API incorrectly returning details.offer totalPrice without insurance when it is added.
                // We point to details.offer totalPrice in order to correctly support subPub fares.
                // Until flights API team fixes createTrip/insurance endpoint we will need to do this
                response.details.offer.totalPrice = response.totalPrice.copy()

                updatedTripObservable.onNext(response)
            } else {
                handleError(response.errorsToString())
            }
        }
    }

    fun updateBenefits() {
        val benefitsId: Int
        if (trip.getOffer().isInternational) {
            benefitsId = R.string.insurance_benefits_international
        } else {
            benefitsId = R.string.insurance_benefits_domestic
        }
        benefitsObservable.onNext(HtmlCompat.fromHtml(context.resources.getString(benefitsId)))
    }

    fun updateTerms() {
        val linkContent = context.resources.getString(R.string.textview_spannable_hyperlink_TEMPLATE,
                product!!.terms.url, context.resources.getString(R.string.insurance_terms))
        termsObservable.onNext(SpannableLinkBuilder().withColor(Ui.obtainThemeColor(context, R.attr.primary_color))
                .withContent(linkContent).bubbleUpClicks().build())
    }

    fun updateTitle() {
        val titleId: Int
        if (trip.getOffer().selectedInsuranceProduct != null) {
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
                    .put("price_type", context.resources.getString(R.string.price_per_person)).format()
            else -> Phrase.from(context.resources, titleId).put("price", "").put("price_type", "").format()
        }

        // use a lightweight base font, and convert any bold spans to medium
        val spannedTitleBuilder = SpannableStringBuilder(HtmlCompat.fromHtml(title.toString()))
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
        programmaticToggleObservable.onNext(trip.getSelectedInsuranceProduct() != null)
    }

    fun updateVisibility() {
        widgetVisibilityObservable.onNext(canWidgetBeDisplayed && haveProduct)
    }
}
