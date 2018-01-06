package com.expedia.vm.traveler

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject

open class TravelerSelectItemViewModel(val context: Context, val index: Int, val age: Int, val category: PassengerCategory) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set
    val resources = context.resources
    val emptyText = if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)) {
        Phrase.from(resources.getString(R.string.checkout_traveler_title_TEMPLATE))
                .put("travelernumber", index + 1)
                .put("passengerycategory", getPassengerAgeRangeString(context, category))
                .format().toString()
    } else {
        Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
                .put("travelernumber", index + 1)
                .put("passengerage", getPassengerString(age))
                .format().toString()
    }

    val iconStatusObservable = BehaviorSubject.create<ContactDetailsCompletenessStatus>()
    val titleObservable = BehaviorSubject.create<String>()
    val subtitleObservable = BehaviorSubject.create<String>()
    val subtitleTextColorObservable = BehaviorSubject.create<Int>()
    val titleFontObservable = BehaviorSubject.create<FontCache.Font>()
    val refreshStatusObservable= PublishSubject.create<Unit>()
    var passportRequired = BehaviorSubject.create<Boolean>(false)
    var currentStatusObservable = BehaviorSubject.create<TravelerCheckoutStatus>(TravelerCheckoutStatus.CLEAN)

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
        setTravelerSummaryInfo(emptyText, "", ContactDetailsCompletenessStatus.DEFAULT, FontCache.Font.ROBOTO_REGULAR)
        subtitleTextColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_default_card_text_color))
        passportRequired.map { Unit }.subscribe(refreshStatusObservable)
        refreshStatusObservable.subscribe{
            refreshStatus()
        }
    }

    private fun refreshStatus() {
        val traveler = getTraveler()
        val validForBooking = travelerValidator.isValidForFlightBooking(traveler, index, passportRequired.value)
        if (!validForBooking) {
            if (travelerValidator.isTravelerEmpty(traveler)) {
                currentStatusObservable.onNext(TravelerCheckoutStatus.CLEAN)
                setTravelerSummaryInfo(getTitle(traveler), "", ContactDetailsCompletenessStatus.DEFAULT, FontCache.Font.ROBOTO_REGULAR)
            } else {
                currentStatusObservable.onNext(TravelerCheckoutStatus.DIRTY)
                setTravelerSummaryInfo(getTitle(traveler), getErrorSubtitle(),
                        ContactDetailsCompletenessStatus.INCOMPLETE, getFont(traveler))
            }
        } else {
            currentStatusObservable.onNext(TravelerCheckoutStatus.COMPLETE)
            setTravelerSummaryInfo(getTitle(traveler), getCompletedFormSubtitle(traveler),
                    ContactDetailsCompletenessStatus.COMPLETE, getFont(traveler))
        }
    }

    private fun getTitle(traveler : Traveler) : String {
        return if (isNameEmpty(traveler)) emptyText else traveler.fullNameBasedOnPos
    }

    private fun getErrorSubtitle() : String? {
        subtitleTextColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_incomplete_text_color))
        return resources.getString(R.string.enter_missing_traveler_details)
    }

    private fun getCompletedFormSubtitle(traveler : Traveler): String? {
        subtitleTextColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_default_card_text_color))
        return traveler.birthDate?.toString("MM/dd/yyyy")
    }

    private fun getFont(traveler : Traveler): FontCache.Font {
        return if (isNameEmpty(traveler)) FontCache.Font.ROBOTO_REGULAR else FontCache.Font.ROBOTO_MEDIUM
    }

    private fun isNameEmpty(traveler: Traveler): Boolean{
        return traveler.fullName.isNullOrEmpty()
    }

    private fun isPhoneEmpty(traveler: Traveler): Boolean {
        return traveler.primaryPhoneNumber?.number.isNullOrEmpty()
    }

    private fun setTravelerSummaryInfo(title: String, subTitle: String?, completenessStatus: ContactDetailsCompletenessStatus, font: FontCache.Font) {
        titleObservable.onNext(title)
        subtitleObservable.onNext(subTitle)
        iconStatusObservable.onNext(completenessStatus)
        titleFontObservable.onNext(font)
    }

    open fun getTraveler() : Traveler {
        return Db.sharedInstance.travelers[index]
    }

    private fun getPassengerString(age: Int): String {
        if (age == -1) {
            return context.getString(R.string.ticket_type_adult)
        }
        val ageText = Phrase.from(resources.getString(R.string.traveler_child_age_TEMPLATE))
                .put("age", age)
                .format().toString()
        return ageText
    }

    private fun getPassengerAgeRangeString(context: Context, category: PassengerCategory) : String {
        return Phrase.from(context.getString(R.string.traveler_age_range_TEMPLATE))
                .put("category", category.getBucketedCategoryString(context))
                .put("range", category.getBucketedAgeString(context))
                .format().toString()
    }

}
