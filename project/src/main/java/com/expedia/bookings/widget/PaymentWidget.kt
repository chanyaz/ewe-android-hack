package com.expedia.bookings.widget

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.extensions.isMaterialFormEnabled
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.section.ISectionEditable
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.bookings.section.SectionBillingInfo
import com.expedia.bookings.section.SectionLocation
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindOptionalView
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isHideApacBillingFieldsEnabled
import com.expedia.bookings.utils.isMaterialFormsEnabled
import com.expedia.bookings.utils.isPopulateCardholderNameEnabled
import com.expedia.bookings.utils.setFocusForView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.accessibility.AccessibleEditTextForSpinner
import com.expedia.util.Optional
import com.expedia.util.endlessObserver
import com.expedia.util.getCheckoutToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeImageDrawable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextChange
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.PaymentViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

open class PaymentWidget(context: Context, attr: AttributeSet) : Presenter(context, attr), View.OnFocusChangeListener {
    val cardInfoContainer: ViewGroup by bindView(R.id.card_info_container)
    val paymentOptionsContainer: ViewGroup by bindView(R.id.section_payment_options_container)
    val billingInfoContainer: ViewGroup by bindView(R.id.section_billing_info_container)
    val paymentOptionCreditDebitCard: TextView by bindView(R.id.payment_option_credit_debit)
    val sectionBillingInfo: SectionBillingInfo by bindView(R.id.section_billing_info)
    val billingAddressTitle: TextView by bindView(R.id.billing_address_title)
    val sectionLocation: SectionLocation by bindView(R.id.section_location_address)
    val creditCardNumber: NumberMaskEditText by bindView(R.id.edit_creditcard_number)
    val creditCardName: AccessibleEditText by bindView(R.id.edit_name_on_card)
    val creditCardPostalCode: AccessibleEditText by bindView(R.id.edit_address_postal_code)
    val cardInfoIcon: ImageView by bindView(R.id.card_info_icon)
    val cardInfoName: TextView by bindView(R.id.card_info_name)
    val cardInfoExpiration: TextView by bindView(R.id.card_info_expiration)
    val expirationDate: AccessibleEditTextForSpinner by bindView(R.id.edit_creditcard_exp_text_btn)
    val paymentStatusIcon: ContactDetailsCompletenessStatusImageView by bindView(R.id.card_info_status_icon)
    val storedCreditCardList: StoredCreditCardList by bindView(R.id.stored_creditcard_list)
    val invalidPaymentContainer: ViewGroup by bindView(R.id.invalid_payment_container)
    val invalidPaymentText: TextView by bindView(R.id.invalid_payment_text)
    val sectionCreditCardContainer: ViewGroup by bindView(R.id.section_credit_card_container)
    val filledInCardDetailsMiniContainer: LinearLayout by bindView(R.id.filled_in_card_details_mini_container)
    val filledInCardDetailsMiniView: TextView by bindView(R.id.filled_in_card_details_mini_view)
    val filledInCardStatus: ContactDetailsCompletenessStatusImageView by bindView(R.id.filled_in_card_status)
    val spacerAboveFilledInCardDetailsMiniView: View by bindView(R.id.spacer_above_filled_in_card_details_mini_view)
    val pwpSmallIcon: ImageView? by bindOptionalView(R.id.pwp_small_icon)
    val materialFormTestEnabled = isMaterialFormsEnabled()
    val filledIn = PublishSubject.create<Boolean>()
    val visibleMenuWithTitleDone = PublishSubject.create<Unit>()
    val toolbarTitle = PublishSubject.create<String>()
    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    val doneClicked = PublishSubject.create<Unit>()
    val focusedView = PublishSubject.create<View>()
    val enableToolbarMenuButton = PublishSubject.create<Boolean>()
    val populateCardholderNameTestEnabled = isPopulateCardholderNameEnabled(context)
    val hideApacBillingFieldsEnabled = isHideApacBillingFieldsEnabled()

    private val userStateManager: UserStateManager = Ui.getApplication(context).appComponent().userStateManager()

    val formFilledSubscriber = endlessObserver<String> {
        filledIn.onNext(isCompletelyFilled())
    }

    var viewmodel: PaymentViewModel by notNullAndObservable { vm ->
        init(vm)
    }

    open protected fun init(vm: PaymentViewModel) {
        vm.cardTitle.subscribeText(cardInfoName)
        vm.cardSubtitle.subscribeTextAndVisibility(cardInfoExpiration)
        vm.subtitleColorObservable.subscribeTextColor(cardInfoExpiration)
        vm.paymentType.subscribeImageDrawable(cardInfoIcon)

        vm.tempCard.subscribe { it ->
            filledInCardDetailsMiniView.text = it.first
            filledInCardDetailsMiniView.setCompoundDrawablesWithIntrinsicBounds(it.second, null, null, null)
            filledInCardStatus.status = ContactDetailsCompletenessStatus.COMPLETE
            filledInCardDetailsMiniContainer.visibility = if (it.first.isNullOrBlank()) GONE else VISIBLE
            spacerAboveFilledInCardDetailsMiniView.visibility = if (it.first.isNullOrBlank()) GONE else VISIBLE
        }

        vm.pwpSmallIcon.subscribeVisibility(pwpSmallIcon)

        vm.isCreditCardRequired.subscribeVisibility(this)

        vm.iconStatus.subscribe {
            paymentStatusIcon.status = it
        }
        vm.invalidPaymentTypeWarning.subscribeText(invalidPaymentText)
        vm.showInvalidPaymentWarning.subscribeVisibility(invalidPaymentContainer)

        vm.lineOfBusiness.subscribe { lob ->
            sectionBillingInfo.setLineOfBusiness(lob)
            sectionLocation.setLineOfBusiness(lob)
            storedCreditCardList.setLineOfBusiness(lob)
            sectionBillingInfo.setErrorStrings()
            sectionLocation.setErrorStrings()
            if (lob.isMaterialFormEnabled(context)) {
                sectionBillingInfo.setMaterialDropdownResources()
            }
        }

        doneClicked.subscribe {
            if (currentState == PaymentDetails::class.java.name) {
                Ui.hideKeyboard(this@PaymentWidget)
                val hasStoredCard = hasStoredCard()
                val billingIsValid = !hasStoredCard && sectionBillingInfo.performValidation()
                val postalIsValid = !hasStoredCard && (!isZipValidationRequired() || sectionLocation.performValidation())
                if (hasStoredCard || (billingIsValid && postalIsValid)) {
                    if (shouldShowSaveDialog()) {
                        showSaveBillingInfoDialog()
                    } else {
                        userChoosesNotToSaveCard()
                    }
                }
                else {
                    announceErrorsOnForm()
                    if (vm.newCheckoutIsEnabled.value) {
                        sectionBillingInfo.requestFocus()
                    } else {
                        goToFirstInvalidField()
                    }
                }
            } else {
                close()
            }
        }

        vm.selectCorrectCardObservable.subscribe { isLoggedIn ->
            if (isLoggedIn && !isAtLeastPartiallyFilled()) {
                val user = userStateManager.userSource.user
                val numberOfSavedCards = user?.storedCreditCards?.size ?: 0
                val tempSavedCard = Db.getTemporarilySavedCard()
                if (numberOfSavedCards >= 1 && tempSavedCard == null) {
                    selectFirstAvailableCard()
                } else if (numberOfSavedCards == 0 && tempSavedCard != null) {
                    storedCreditCardListener.onTemporarySavedCreditCardChosen(Db.getTemporarilySavedCard())
                }
            }
            storedCreditCardList.updateAdapter()
        }

        vm.userLogin.subscribe { isLoggedIn ->
            if (isLoggedIn) {
                clearPaymentInfo()
                vm.selectCorrectCardObservable.onNext(isLoggedIn)
            }
        }

        vm.emptyBillingInfo.subscribe {
            reset()
        }

        vm.userHasAtleastOneStoredCard.subscribe { hasCard ->
            if (hasCard) {
                paymentOptionCreditDebitCard.setTextColor(ContextCompat.getColor(context, R.color.hotelsv2_checkout_text_color))
                paymentOptionCreditDebitCard.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.add_new_credit_card_icon_with_padding), null, null, null)
            } else {
                paymentOptionCreditDebitCard.setTextColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
                paymentOptionCreditDebitCard.setCompoundDrawablesWithIntrinsicBounds(getCreditCardIcon(R.drawable.add_new_credit_card_icon_with_padding), null, ContextCompat.getDrawable(context, R.drawable.enter_new_credit_card_arrow), null)
            }
        }

        viewmodel.isZipValidationRequired.subscribeVisibility(sectionLocation)

        vm.moveFocusToPostalCodeSubject.subscribe {
            creditCardPostalCode.requestFocus()
        }

        vm.clearHiddenBillingAddress.subscribe {
            if (!isCompletelyFilled()) {
                val location = Location()
                sectionBillingInfo.billingInfo.location = location
                sectionLocation.bind(location)
                sectionLocation.resetValidation()
            }
        }
    }

    override fun addVisibilitySubscriptions() {
        super.addVisibilitySubscriptions()
        if (!viewmodel.newCheckoutIsEnabled.value) {
            addVisibilitySubscription(creditCardNumber.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(creditCardName.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(creditCardPostalCode.subscribeTextChange(formFilledSubscriber))
            addVisibilitySubscription(expirationDate.subscribeTextChange(formFilledSubscriber))
        }
    }


    open val storedCreditCardListener = object : StoredCreditCardList.IStoredCreditCardListener {

        override fun onStoredCreditCardChosen(card: StoredCreditCard) {
            clearCCAndCVV()
            reset()
            sectionBillingInfo.billingInfo.storedCard = card
            temporarilySavedCardIsSelected(false, Db.getTemporarilySavedCard())
            viewmodel.billingInfoAndStatusUpdate.onNext(Pair(sectionBillingInfo.billingInfo, ContactDetailsCompletenessStatus.COMPLETE))
            viewmodel.onStoredCardChosen.onNext(Unit)
            viewmodel.cardTypeSubject.onNext(Optional(card.type))
            if (card.id != null && !card.id.equals(viewmodel.cardBIN.value)) {
                viewmodel.cardBIN.onNext(card.id)
            }
            trackPaymentStoredCCSelect()
        }

        override fun onTemporarySavedCreditCardChosen(info: BillingInfo) {
            reset()
            removeStoredCard()
            selectTemporaryCard()
            viewmodel.cardBIN.onNext(info.number.replace(" ", "").substring(0, 6))
        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToOptions)
        addTransition(optionsToDetails)
        addTransition(defaultToDetails)
        creditCardNumber.onFocusChangeListener = this
        creditCardName.onFocusChangeListener = this
        creditCardPostalCode.onFocusChangeListener = this
        if (!AccessibilityUtil.isTalkBackEnabled(context)) {
            expirationDate.onFocusChangeListener = this
            expirationDate.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    Ui.hideKeyboard(this)
                    expirationDate.performClick()
                }
                onFocusChange(view, hasFocus)
            }
        }
        sectionBillingInfo.addInvalidCharacterListener { text, mode ->
            val activity = context as AppCompatActivity
            InvalidCharacterHelper.showInvalidCharacterPopup(activity.supportFragmentManager, mode)
        }
        sectionLocation.addInvalidCharacterListener { text, mode ->
            val activity = context as AppCompatActivity
            InvalidCharacterHelper.showInvalidCharacterPopup(activity.supportFragmentManager, mode)
        }
        sectionBillingInfo.addChangeListener(billingInfoChangedListener)
        filledInCardDetailsMiniView.setCompoundDrawablesWithIntrinsicBounds(getCreditCardIcon(R.drawable.ic_hotel_credit_card), null, null, null)
        storedCreditCardList.setStoredCreditCardListener(storedCreditCardListener)

        cardInfoContainer.setOnClickListener {
            showPaymentForm(fromPaymentError = false)
        }

        filledInCardDetailsMiniView.setOnClickListener {
            show(PaymentDetails())
        }

        paymentOptionCreditDebitCard.setOnClickListener {
            viewmodel.resetCardFees.onNext(Unit)
            viewmodel.resetCardList.onNext(Unit)
            if (shouldShowPaymentOptions()) {
                reset()
                show(PaymentDetails())
            } else {
                show(PaymentDetails(), FLAG_CLEAR_BACKSTACK)
            }
            trackPaymentEnterNewCard()
        }

        FontCache.setTypeface(cardInfoExpiration, FontCache.Font.ROBOTO_REGULAR)
        FontCache.setTypeface(cardInfoName, FontCache.Font.ROBOTO_MEDIUM)
        Db.setTemporarilySavedCard(null)
    }

    fun showPaymentForm(fromPaymentError: Boolean) {
        if (!shouldShowPaymentOptions() || fromPaymentError && sectionBillingInfo.billingInfo.isTempCard) {
            show(PaymentDetails(), FLAG_CLEAR_BACKSTACK)
            trackPaymentEnterNewCard()
            if (fromPaymentError) {
                clearCCAndCVV()
            }
        } else {
            show(PaymentOption(), FLAG_CLEAR_BACKSTACK)
            trackShowPaymentOptions()
            if (fromPaymentError) {
                removeStoredCard()
            }
        }
        viewmodel.expandObserver.onNext(true)
    }

    private fun selectTemporaryCard() {
        temporarilySavedCardIsSelected(true, Db.getTemporarilySavedCard())
        viewmodel.billingInfoAndStatusUpdate.onNext(Pair(Db.getTemporarilySavedCard(), ContactDetailsCompletenessStatus.COMPLETE))
        viewmodel.onStoredCardChosen.onNext(Unit)
        viewmodel.onTemporarySavedCreditCardChosen.onNext(Unit)
    }

    protected fun getCreditCardIcon(drawableResourceId: Int): Drawable {
        val icon = ContextCompat.getDrawable(context, drawableResourceId).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
        return icon
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            focusedView.onNext(v)
        } else {
            val isFieldValid = sectionBillingInfo.validateField(v.id)
            if (isFieldValid) {
                sectionLocation.validateField(v.id)
            }
        }
    }

    private fun reset() {
        sectionBillingInfo.bind(BillingInfo())
        val location = Location()
        sectionBillingInfo.billingInfo.location = location
        sectionLocation.bind(location)
        sectionBillingInfo.resetValidation()
        sectionLocation.resetValidation()
    }

    fun selectFirstAvailableCard() {
        val tripItem = Db.getTripBucket().getItem(getLineOfBusiness())
        if (tripItem != null) {
            if ((hasStoredCard() && !tripItem.isPaymentTypeSupported(getCardType(), context)) || !hasStoredCard()) {
                val user = userStateManager.userSource.user
                val storedUserCreditCards = user?.storedCreditCards ?: emptyList()

                for (storedCard in storedUserCreditCards) {
                    if (tripItem.isPaymentTypeSupported(storedCard.type, context)) {
                        sectionBillingInfo.bind(Db.getBillingInfo())
                        Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(BillingInfo())
                        val currentCC = Db.getBillingInfo().storedCard
                        BookingInfoUtils.resetPreviousCreditCardSelectState(userStateManager, currentCC)
                        val card = storedCard
                        Db.getWorkingBillingInfoManager().workingBillingInfo.storedCard = card
                        Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB()
                        sectionBillingInfo.billingInfo.storedCard = card
                        temporarilySavedCardIsSelected(false, sectionBillingInfo.billingInfo)
                        viewmodel.cardTypeSubject.onNext(Optional(card.type))
                        viewmodel.billingInfoAndStatusUpdate.onNext(Pair(sectionBillingInfo.billingInfo, ContactDetailsCompletenessStatus.COMPLETE))
                        viewmodel.cardBIN.onNext(card.id)
                        break
                    }
                }
            }
        }
    }

    open fun clearCCAndCVV() {
        creditCardNumber.setText("")
        Db.getWorkingBillingInfoManager().workingBillingInfo.number = null
        Db.getBillingInfo().number = null
        clearCVV()
        validateAndBind()
    }

    open fun clearCVV() {
        Db.getWorkingBillingInfoManager().workingBillingInfo.securityCode = null
        Db.getBillingInfo().securityCode = null
    }

    open fun isAtLeastPartiallyFilled(): Boolean {
        return creditCardNumber.text.isNotEmpty()
                || (isZipValidationRequired() && creditCardPostalCode.text.isNotEmpty())
                || creditCardName.text.isNotEmpty()
    }

    open fun isCompletelyFilled(): Boolean {
        return (creditCardNumber.text.isNotEmpty()
                && (!isZipValidationRequired() || creditCardPostalCode.text.isNotEmpty())
                && creditCardName.text.isNotEmpty() && expirationDate.text.isNotEmpty()) || hasStoredCard()
    }

    open fun validateAndBind() {
        if (!isCreditCardRequired()) {
            viewmodel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))
            viewmodel.emptyBillingInfo.onNext(Unit)
        } else if (isCreditCardRequired() && (hasStoredCard())) {
            viewmodel.billingInfoAndStatusUpdate.onNext(Pair(sectionBillingInfo.billingInfo, ContactDetailsCompletenessStatus.COMPLETE))
        } else if (isCreditCardRequired() && (isAtLeastPartiallyFilled())) {
            val isLocationFilled = sectionLocation.performValidation()
            val isBillingInfoFilled = sectionBillingInfo.performValidation()
            if (isBillingInfoFilled && isLocationFilled) {
                viewmodel.billingInfoAndStatusUpdate.onNext(Pair(sectionBillingInfo.billingInfo, ContactDetailsCompletenessStatus.COMPLETE))
            } else {
                viewmodel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.INCOMPLETE))
            }
        } else if (isCreditCardRequired() && hasTempCard()) {
            viewmodel.billingInfoAndStatusUpdate.onNext(Pair(Db.getTemporarilySavedCard(), ContactDetailsCompletenessStatus.COMPLETE))
        } else if (isAtLeastPartiallyFilled()) {
            viewmodel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.INCOMPLETE))
        } else {
            viewmodel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))
            viewmodel.emptyBillingInfo.onNext(Unit)
        }
    }

    fun hasTempCard(): Boolean {
        val info = Db.getTemporarilySavedCard()
        return info?.saveCardToExpediaAccount == true
    }

    open fun isComplete(): Boolean {
        if (!isCreditCardRequired()) {
            return true
        } else if (isCreditCardRequired() && (hasStoredCard())) {
            return true
        } else if (isCreditCardRequired() && (isAtLeastPartiallyFilled() && sectionBillingInfo.performValidation() && sectionLocation.performValidation())) {
            return true
        } else return isCreditCardRequired() && Db.getTemporarilySavedCard() != null && Db.getTemporarilySavedCard().saveCardToExpediaAccount
    }

    fun isCreditCardRequired(): Boolean {
        return viewmodel.isCreditCardRequired.value
    }

    fun getLineOfBusiness(): LineOfBusiness {
        return viewmodel.lineOfBusiness.value
    }

    fun isZipValidationRequired(): Boolean {
        return viewmodel.isZipValidationRequired.value
    }

    fun isStateRequired(): Boolean {
        return sectionLocation.isStateRequired
    }

    protected fun hasStoredCard(): Boolean {
        return sectionBillingInfo.billingInfo != null && sectionBillingInfo.billingInfo.hasStoredCard()
    }

    private val billingInfoChangedListener: ISectionEditable.SectionChangeListener = ISectionEditable.SectionChangeListener {
        val cardType = sectionBillingInfo.billingInfo?.getPaymentType(context)
        viewmodel.cardTypeSubject.onNext(Optional(cardType))
        val cardNumber = sectionBillingInfo.billingInfo?.number
        if (cardNumber != null) {
            val currentCardBIN = viewmodel.cardBIN.value
            if (cardNumber.length >= 6) {
                val cardBIN = cardNumber.replace(" ", "").substring(0, 6)
                if (!cardBIN.equals(currentCardBIN)) {
                    viewmodel.cardBIN.onNext(cardBIN)
                }
            }
            else if (cardNumber.length == 0 && currentCardBIN.isNotBlank()) {
                viewmodel.resetCardFees.onNext(Unit)
            }
        }
    }

    /** Save card to account **/
    private fun shouldShowSaveDialog(): Boolean {
        return userStateManager.isUserAuthenticated() &&
                !sectionBillingInfo.billingInfo.saveCardToExpediaAccount &&
                workingBillingInfoChanged() &&
                Db.getWorkingBillingInfoManager().workingBillingInfo.storedCard == null
    }

    private fun showSaveBillingInfoDialog() {
        val dialog = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
                .setTitle(R.string.save_billing_info)
                .setCancelable(false)
                .setMessage(Phrase.from(context, R.string.save_billing_info_message_TEMPLATE)
                        .put("brand", BuildConfig.brand)
                        .format())
                .setPositiveButton(R.string.save, { dialogInterface, i ->
                    OmnitureTracking.trackUserChoosesToSaveCard()
                    userChoosesToSaveCard()
                })
                .setNegativeButton(R.string.no_thanks, { dialogInterface, i ->
                    OmnitureTracking.trackUserChoosesNotToSaveCard()
                    userChoosesNotToSaveCard()
                }).create()
        dialog.show()
    }

    private fun workingBillingInfoChanged(): Boolean {
        if (sectionBillingInfo.billingInfo != null) {
            return Db.getWorkingBillingInfoManager().workingBillingInfo.compareTo(sectionBillingInfo.billingInfo) != 0
        }
        return false
    }

    /** Presenter **/
    class PaymentDefault
    class PaymentOption
    class PaymentDetails

    private val defaultTransition = object : Presenter.DefaultTransition(PaymentDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            viewmodel.menuVisibility.onNext(false)
            toolbarTitle.onNext(getCheckoutToolbarTitle(resources))
            cardInfoContainer.visibility = View.VISIBLE
            paymentOptionsContainer.visibility = View.GONE
            billingInfoContainer.visibility = View.GONE
            validateAndBind()
            cardInfoContainer.setFocusForView()
        }
    }

    private val defaultToOptions = object : Presenter.Transition(PaymentDefault::class.java,
            PaymentOption::class.java) {
        override fun startTransition(forward: Boolean) {
            viewmodel.menuVisibility.onNext(false)
            cardInfoContainer.visibility = if (forward) View.GONE else View.VISIBLE
            paymentOptionsContainer.visibility = if (forward) View.VISIBLE else View.GONE
            billingInfoContainer.visibility = View.GONE
            toolbarTitle.onNext(
                    if (forward) {
                        resources.getString(R.string.checkout_enter_payment_details)
                    } else {
                        getCheckoutToolbarTitle(resources)
                    })
            storedCreditCardList.bind()
            if (!forward) validateAndBind()
            else viewmodel.userHasAtleastOneStoredCard.onNext(userStateManager.isUserAuthenticated() && (userStateManager.userSource.user?.storedCreditCards?.isNotEmpty() == true || Db.getTemporarilySavedCard() != null))
            if (viewmodel.newCheckoutIsEnabled.value) updateUniversalToolbarMenu(!forward) else updateLegacyToolbarMenu(forward)
        }
    }

    protected open fun updateLegacyToolbarMenu(forward: Boolean) {
        if (forward) {
            visibleMenuWithTitleDone.onNext(Unit)
            enableToolbarMenuButton.onNext(true)
            viewmodel.enableMenuItem.onNext(isComplete())
        } else {
            viewmodel.enableMenuItem.onNext(true)
        }
    }

    protected fun updateUniversalToolbarMenu(forward: Boolean) {
        if (forward || currentState == PaymentOption::class.java.name) {
            visibleMenuWithTitleDone.onNext(Unit)
            viewmodel.enableMenuItem.onNext(true)
        } else {
            viewmodel.menuVisibility.onNext(false)
        }
    }

    private fun setToolbarTitleForPaymentDetailsView(showingPaymentForm: Boolean, otherTitle: String) {
        val debitCardNotAccepted = viewmodel.showDebitCardsNotAcceptedSubject.value
        val paymentFormTitle = resources.getString(if (debitCardNotAccepted) R.string.new_credit_card else R.string.new_credit_debit_card)
        toolbarTitle.onNext(if (showingPaymentForm) paymentFormTitle else otherTitle)
    }

    private val defaultToDetails = object : Presenter.Transition(PaymentDefault::class.java,
            PaymentDetails::class.java) {
        override fun endTransition(forward: Boolean) {
            viewmodel.menuVisibility.onNext(forward)
            setToolbarTitleForPaymentDetailsView(forward, getCheckoutToolbarTitle(resources))
            cardInfoContainer.visibility = if (forward) View.GONE else View.VISIBLE
            paymentOptionsContainer.visibility = View.GONE
            billingInfoContainer.visibility = if (forward) View.VISIBLE else View.GONE
            storedCreditCardList.bind()
            trackAnalytics()
            if (!forward) validateAndBind()
            if (forward) {
                if (populateCardholderNameTestEnabled) {
                    populateCardholderName()
                }
                showMaskedCreditCardNumber()
                filledIn.onNext(isCompletelyFilled())
            }
            if (getLineOfBusiness().isMaterialFormEnabled(context)) viewmodel.updateBackgroundColor.onNext(forward)
            if (viewmodel.newCheckoutIsEnabled.value) updateUniversalToolbarMenu(forward) else updateLegacyToolbarMenu(!forward)
            viewmodel.showingPaymentForm.onNext(forward)
            if (materialFormTestEnabled && hideApacBillingFieldsEnabled) {
                if (forward) viewmodel.removeBillingAddressForApac.onNext(PointOfSale.getPointOfSale().shouldHideBillingAddressFields())
                else viewmodel.clearHiddenBillingAddress.onNext(Unit)
            }
        }
    }

    private val optionsToDetails = object : Presenter.Transition(PaymentOption::class.java,
            PaymentDetails::class.java) {
        override fun endTransition(forward: Boolean) {
            viewmodel.menuVisibility.onNext(forward)
            setToolbarTitleForPaymentDetailsView(forward, resources.getString(R.string.checkout_enter_payment_details))
            cardInfoContainer.visibility = View.GONE
            paymentOptionsContainer.visibility = if (forward) View.GONE else View.VISIBLE
            billingInfoContainer.visibility = if (forward) View.VISIBLE else View.GONE
            creditCardNumber.requestFocus()
            onFocusChange(creditCardNumber, true)
            if (forward) {
                if (populateCardholderNameTestEnabled) {
                    populateCardholderName()
                }
                showMaskedCreditCardNumber()
                removeStoredCard()
                temporarilySavedCardIsSelected(false, Db.getTemporarilySavedCard())
                filledIn.onNext(isCompletelyFilled())
            }
            if (forward) Ui.showKeyboard(creditCardNumber, null) else Ui.hideKeyboard(this@PaymentWidget)
            storedCreditCardList.bind()
            trackAnalytics()
            if (!forward) {
                validateAndBind()
                viewmodel.userHasAtleastOneStoredCard.onNext(userStateManager.isUserAuthenticated() && (userStateManager.userSource.user?.storedCreditCards?.isNotEmpty() == true || Db.getTemporarilySavedCard() != null))
            }
            viewmodel.showingPaymentForm.onNext(forward)
            if (getLineOfBusiness().isMaterialFormEnabled(context)) viewmodel.updateBackgroundColor.onNext(forward)
            if (viewmodel.newCheckoutIsEnabled.value) updateUniversalToolbarMenu(forward) else updateLegacyToolbarMenu(!forward)
            if (materialFormTestEnabled && hideApacBillingFieldsEnabled) {
                if (forward) viewmodel.removeBillingAddressForApac.onNext(PointOfSale.getPointOfSale().shouldHideBillingAddressFields())
                else viewmodel.clearHiddenBillingAddress.onNext(Unit)
            }
        }
    }

    fun removeStoredCard() {
        if (Db.getBillingInfo().hasStoredCard()) {
            val card = Db.getBillingInfo().storedCard
            viewmodel.storedCardRemoved.onNext(Optional(card))
            if (populateCardholderNameTestEnabled) {
                populateCardholderName()
            }
        }
    }

    /** Tracking **/
    fun trackAnalytics() {
        if (!ExpediaBookingApp.isAutomation()) {
            OmnitureTracking.trackCheckoutPayment(getLineOfBusiness())
        }
    }

    fun getCardType(): PaymentType {
        if (isCreditCardRequired() && hasStoredCard()) {
            return sectionBillingInfo.billingInfo.storedCard.type
        } else if (isCreditCardRequired() && (isAtLeastPartiallyFilled() && sectionBillingInfo.performValidation() && sectionLocation.performValidation())) {
            return sectionBillingInfo.billingInfo.getPaymentType(context)
        } else if (hasTempCard()) {
            return Db.getTemporarilySavedCard().getPaymentType(context)
        }

        return PaymentType.CARD_UNKNOWN
    }

    open fun shouldShowPaymentOptions(): Boolean {
        val user = userStateManager.userSource.user

        return (userStateManager.isUserAuthenticated() && user?.storedCreditCards?.isNotEmpty() == true
                && getLineOfBusiness() != LineOfBusiness.RAILS)
                || Db.getTemporarilySavedCard() != null
    }

    private fun temporarilySavedCardIsSelected(isSelected: Boolean, info: BillingInfo?) {
        info?.saveCardToExpediaAccount = isSelected
    }

    fun userChoosesToSaveCard() {
        sectionBillingInfo.billingInfo.saveCardToExpediaAccount = true
        sectionBillingInfo.billingInfo.setIsTempCard(true)
        Db.setTemporarilySavedCard(BillingInfo(sectionBillingInfo.billingInfo))
        selectTemporaryCard()
        close()
    }

    fun userChoosesNotToSaveCard() {
        sectionBillingInfo.billingInfo.saveCardToExpediaAccount = false
        sectionBillingInfo.billingInfo.setIsTempCard(true)
        close()
    }

    open fun close() {
        if (currentState != PaymentOption::class.java.name && shouldShowPaymentOptions()) {
            show(PaymentOption(), FLAG_CLEAR_TOP)
        } else {
            clearBackStack()
            val activity = context as Activity
            activity.onBackPressed()
        }
    }

    /**
     * Following methods are for tracking.
     * Don't make this class abstract, since this is a persenter and could have it's own view .xml implementation
     */
    open fun trackShowPaymentOptions() {
        if (viewmodel.lineOfBusiness.value == LineOfBusiness.FLIGHTS_V2) {
            FlightsV2Tracking.trackCheckoutSelectPaymentClick()
        } else if (viewmodel.lineOfBusiness.value == LineOfBusiness.PACKAGES) {
            PackagesTracking().trackCheckoutSelectPaymentClick()
        }
    }

    fun trackPaymentEnterNewCard() {
        OmnitureTracking.trackShowPaymentEnterNewCard(getLineOfBusiness())
    }

    open fun trackPaymentStoredCCSelect() {
        // Let inheriting class call their respective tracking.
        if (viewmodel.lineOfBusiness.value == LineOfBusiness.FLIGHTS_V2) {
            FlightsV2Tracking.trackPaymentStoredCCSelect()
        } else if (viewmodel.lineOfBusiness.value == LineOfBusiness.PACKAGES) {
            PackagesTracking().trackCheckoutPaymentSelectStoredCard()
        }
    }

    open fun showMaskedCreditCardNumber() {
    }

    override fun back(): Boolean {
        if (currentState == PaymentOption::class.java.name) {
            viewmodel.enableMenuItem.onNext(true)
        }
        return super.back()
    }

    fun clearPaymentInfo() {
        reset()
        clearCCAndCVV()
    }

    fun populateCardholderName() {
        if (creditCardName.text.isEmpty()) {
            creditCardName.setText(viewmodel.populateCardholderNameObservable.value)
        }
    }

    private fun goToFirstInvalidField() {
        val firstInvalidField = sectionBillingInfo.firstInvalidField
        if (firstInvalidField != null) {
            firstInvalidField.requestFocus()
            sectionBillingInfo.resetValidation(firstInvalidField.id, false)
        }
    }

    private fun announceErrorsOnForm() {
        val numberOfInvalidFields = sectionBillingInfo.numberOfInvalidFields.plus(sectionLocation.numberOfInvalidFields)
        val announcementString = StringBuilder()
        announcementString.append(Phrase.from(context.resources.getQuantityString(R.plurals.number_of_errors_TEMPLATE, numberOfInvalidFields))
                .put("number", numberOfInvalidFields)
                .format()
                .toString())
                .append(" ")
                .append(context.getString(R.string.accessibility_announcement_please_review_and_resubmit))
        announceForAccessibility(announcementString)
    }
}