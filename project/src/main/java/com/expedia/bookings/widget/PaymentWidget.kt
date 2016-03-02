package com.expedia.bookings.widget

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.activity.GoogleWalletActivity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.section.ISectionEditable
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.bookings.section.SectionBillingInfo
import com.expedia.bookings.section.SectionLocation
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.WalletUtils
import com.expedia.bookings.utils.bindOptionalView
import com.expedia.bookings.utils.bindView
import com.expedia.util.getCheckoutToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeImageDrawable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.PaymentViewModel
import com.squareup.phrase.Phrase

public open class PaymentWidget(context: Context, attr: AttributeSet) : Presenter(context, attr), View.OnFocusChangeListener {
    val REQUEST_CODE_GOOGLE_WALLET_ACTIVITY = 1989
    val cardInfoContainer: ViewGroup by bindView(R.id.card_info_container)
    val paymentOptionsContainer: ViewGroup by bindView(R.id.section_payment_options_container)
    val billingInfoContainer: ViewGroup by bindView(R.id.section_billing_info_container)
    val paymentOptionCreditDebitCard: TextView by bindView(R.id.payment_option_credit_debit)
    val paymentOptionGoogleWallet: TextView by bindView(R.id.payment_option_google_wallet)
    val sectionBillingInfo: SectionBillingInfo by bindView(R.id.section_billing_info)
    val sectionLocation: SectionLocation by bindView(R.id.section_location_address)
    val creditCardNumber: NumberMaskEditText by bindView(R.id.edit_creditcard_number)
    val creditCardName: EditText by bindView(R.id.edit_name_on_card)
    val creditCardPostalCode: EditText by bindView(R.id.edit_address_postal_code)
    val cardInfoIcon: RoundImageView by bindView(R.id.card_info_icon)
    val cardInfoName: TextView by bindView(R.id.card_info_name)
    val cardInfoExpiration: TextView by bindView(R.id.card_info_expiration)
    val paymentStatusIcon: ContactDetailsCompletenessStatusImageView by bindView(R.id.card_info_status_icon)
    val storedCreditCardList: StoredCreditCardList by bindView(R.id.stored_creditcard_list)
    val invalidPaymentContainer: ViewGroup by bindView(R.id.invalid_payment_container)
    val invalidPaymentText: TextView by bindView(R.id.invalid_payment_text)
    val sectionCreditCardContainer: ViewGroup by bindView(R.id.section_credit_card_container)
    val filledInCardDetailsMiniView: TextView by bindView(R.id.filled_in_card_details_mini_view)
    val spacerAboveFilledInCardDetailsMiniView: View by bindView(R.id.spacer_above_filled_in_card_details_mini_view)
    val pwpSmallIcon: ImageView? by bindOptionalView(R.id.pwp_small_icon)

    var viewmodel: PaymentViewModel by notNullAndObservable { vm ->
        init(vm)
    }

    open protected fun init(vm: PaymentViewModel){
        vm.cardTitle.subscribeText(cardInfoName)
        vm.cardSubtitle.subscribeTextAndVisibility(cardInfoExpiration)
        vm.paymentType.subscribeImageDrawable(cardInfoIcon)

        vm.tempCard.subscribe { it ->
            filledInCardDetailsMiniView.text = it.first
            filledInCardDetailsMiniView.setCompoundDrawablesWithIntrinsicBounds(it.second, null, null, null)
            filledInCardDetailsMiniView.visibility = if (it.first.isNullOrBlank()) GONE else VISIBLE
            spacerAboveFilledInCardDetailsMiniView.visibility = if (it.first.isNullOrBlank()) GONE else VISIBLE
        }

        vm.pwpSmallIcon.subscribeVisibility(pwpSmallIcon)

        vm.isCreditCardRequired.subscribeVisibility(this)

        vm.iconStatus.subscribe {
            paymentStatusIcon.status = it
        }
        vm.invalidPayment.subscribe { text ->
            invalidPaymentText.text = text
            invalidPaymentContainer.visibility = if (text.isNullOrBlank()) GONE else VISIBLE
        }
        vm.lineOfBusiness.subscribe { lob ->
            sectionBillingInfo.setLineOfBusiness(lob)
            sectionLocation.setLineOfBusiness(lob)
            storedCreditCardList.setLineOfBusiness(lob)
            if (lob == LineOfBusiness.HOTELSV2) {
                val shouldShowDebitCreditHint = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCKOCreditDebitTest)
                creditCardNumber.setHint(if (shouldShowDebitCreditHint) R.string.credit_debit_card_hint else R.string.credit_card_hint)
            }
        }

        vm.doneClicked.subscribe {
            val hasStoredCard = hasStoredCard()
            val billingIsValid = !hasStoredCard && sectionBillingInfo.performValidation()
            val postalIsValid = !hasStoredCard && sectionLocation.performValidation()
            if (hasStoredCard || (billingIsValid && postalIsValid)) {
                if (shouldShowSaveDialog()) {
                    showSaveBillingInfoDialog()
                } else {
                    close()
                }
            }
        }

        vm.userLogin.subscribe { isLoggedIn ->
            if (isLoggedIn && !isFilled() && Db.getUser()?.storedCreditCards?.size == 1) {
                sectionBillingInfo.bind(Db.getBillingInfo())
                selectFirstAvailableCard()
            }
        }

        vm.emptyBillingInfo.subscribe{
            reset()
        }

        vm.userHasAtleastOneStoredCard.subscribe { hasCard ->
            if (hasCard) {
                paymentOptionCreditDebitCard.setTextColor(getResources().getColor(R.color.hotelsv2_checkout_text_color))
                paymentOptionCreditDebitCard.setCompoundDrawablesRelativeWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.add_new_credit_card), null, null, null)
            } else {
                paymentOptionCreditDebitCard.setTextColor(getResources().getColor(R.color.hotels_primary_color))
                paymentOptionCreditDebitCard.setCompoundDrawablesRelativeWithIntrinsicBounds(getCreditCardIcon(R.drawable.add_new_credit_card), null, getContext().getResources().getDrawable(R.drawable.enter_new_credit_card_arrow), null)
            }
        }
    }

    open val storedCreditCardListener = object : StoredCreditCardList.IStoredCreditCardListener {
        override fun onStoredCreditCardChosen(card: StoredCreditCard) {
            sectionBillingInfo.billingInfo.storedCard = card
            temporarilySavedCardIsSelected(false)
            viewmodel.completeBillingInfo.onNext(sectionBillingInfo.billingInfo)
            viewmodel.onStoredCardChosen.onNext(Unit)
            closePopup()
        }

        override fun onTemporarySavedCreditCardChosen(info: BillingInfo) {
            removeStoredCard()
            temporarilySavedCardIsSelected(true)
            viewmodel.completeBillingInfo.onNext(Db.getTemporarilySavedCard())
            closePopup()
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

        sectionBillingInfo.addInvalidCharacterListener { text, mode ->
            val activity = context as AppCompatActivity
            InvalidCharacterHelper.showInvalidCharacterPopup(activity.supportFragmentManager, mode)
        }
        sectionBillingInfo.addChangeListener(mValidFormsOfPaymentListener)
        filledInCardDetailsMiniView.setCompoundDrawablesWithIntrinsicBounds(getCreditCardIcon(R.drawable.ic_hotel_credit_card), null, null, null)
        storedCreditCardList.setStoredCreditCardListener(storedCreditCardListener)

        cardInfoContainer.setOnClickListener {
            if (shouldShowPaymentOptions()) {
                show(PaymentOption(), FLAG_CLEAR_BACKSTACK)
            } else {
                show(PaymentDetails(), FLAG_CLEAR_BACKSTACK)
            }
            viewmodel.expandObserver.onNext(true)
        }

        filledInCardDetailsMiniView.setOnClickListener {
            show(PaymentDetails())
        }

        paymentOptionCreditDebitCard.setOnClickListener {
            if (shouldShowPaymentOptions()) {
                show(PaymentDetails())
            } else {
                show(PaymentDetails(), FLAG_CLEAR_BACKSTACK)
            }
        }

        paymentOptionGoogleWallet.setOnClickListener {
            openGoogleWallet()
        }

        FontCache.setTypeface(cardInfoExpiration, FontCache.Font.ROBOTO_REGULAR)
        FontCache.setTypeface(cardInfoName, FontCache.Font.ROBOTO_MEDIUM)

    }

    protected fun getCreditCardIcon(drawableResourceId: Int): Drawable {
        val icon = ContextCompat.getDrawable(context, drawableResourceId).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
        return icon
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            viewmodel.editText.onNext(v as EditText)
            if (v === creditCardPostalCode && isZipValidationRequired()) {
                sectionLocation.resetValidation()
            }
            sectionBillingInfo.resetValidation(v.id, true)
        }
    }

    protected fun reset() {
        sectionBillingInfo.bind(BillingInfo())
        val location = Location()
        sectionBillingInfo.billingInfo.location = location
        sectionLocation.bind(location)
        sectionBillingInfo.resetValidation()
        sectionLocation.resetValidation()
    }

    fun selectFirstAvailableCard() {
        Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(BillingInfo())
        val currentCC = Db.getBillingInfo().storedCard
        BookingInfoUtils.resetPreviousCreditCardSelectState(context, currentCC)
        val card = Db.getUser().storedCreditCards[0]
        Db.getWorkingBillingInfoManager().workingBillingInfo.storedCard = card
        Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB()
        sectionBillingInfo.billingInfo.storedCard = card
        viewmodel.completeBillingInfo.onNext(sectionBillingInfo.billingInfo)
    }

    open fun isFilled(): Boolean {
        return !creditCardNumber.text.toString().isEmpty() || !creditCardPostalCode.text.toString().isEmpty() || !creditCardName.text.toString().isEmpty()
    }

    open fun validateAndBind() {
        if (!isCreditCardRequired()) {
            viewmodel.emptyBillingInfo.onNext(Unit)
        } else if (isCreditCardRequired() && (hasStoredCard())) {
            viewmodel.completeBillingInfo.onNext(sectionBillingInfo.billingInfo)
        } else if (isCreditCardRequired() && (isFilled() && sectionBillingInfo.performValidation() && sectionLocation.performValidation())) {
            viewmodel.completeBillingInfo.onNext(sectionBillingInfo.billingInfo)
        } else if (isCreditCardRequired() && hasTempCard()) {
            viewmodel.completeBillingInfo.onNext(Db.getTemporarilySavedCard())
        } else if (isFilled()) {
            viewmodel.completeBillingInfo.onNext(null)
            viewmodel.incompleteBillingInfo.onNext(Unit)
        } else {
            viewmodel.completeBillingInfo.onNext(null)
            viewmodel.emptyBillingInfo.onNext(Unit)
        }
    }

    fun hasTempCard() : Boolean{
        val info = Db.getTemporarilySavedCard()
        return info?.saveCardToExpediaAccount ?: false
    }

    open fun isComplete() : Boolean {
        if (!isCreditCardRequired()) {
            return true
        } else if (isCreditCardRequired() && (hasStoredCard())) {
            return true
        } else if (isCreditCardRequired() && (isFilled() && sectionBillingInfo.performValidation() && sectionLocation.performValidation())) {
            return true
        } else if (isCreditCardRequired() && Db.getTemporarilySavedCard() != null && Db.getTemporarilySavedCard().saveCardToExpediaAccount) {
            return true
        } else {
            return false
        }
    }

    fun isCreditCardRequired() : Boolean {
        return viewmodel.isCreditCardRequired.value
    }

    fun getLineOfBusiness() : LineOfBusiness {
        return viewmodel.lineOfBusiness.value
    }

    fun isZipValidationRequired() : Boolean {
        return viewmodel.isZipValidationRequired.value
    }

    protected fun hasStoredCard(): Boolean {
        return sectionBillingInfo.billingInfo != null && sectionBillingInfo.billingInfo.hasStoredCard()
    }

    private val mValidFormsOfPaymentListener: ISectionEditable.SectionChangeListener = ISectionEditable.SectionChangeListener {
        val cardType = sectionBillingInfo.billingInfo?.paymentType
        viewmodel.cardType.onNext(cardType)
    }

    /** Google Wallet **/
    private fun openGoogleWallet() {
        val i = Intent(context, GoogleWalletActivity::class.java)
        (context as AppCompatActivity).startActivityForResult(i, REQUEST_CODE_GOOGLE_WALLET_ACTIVITY)
    }

    /** Save card to account **/
    private fun shouldShowSaveDialog(): Boolean {
        return getLineOfBusiness() == LineOfBusiness.HOTELSV2 && User.isLoggedIn(context) &&
                !sectionBillingInfo.billingInfo.saveCardToExpediaAccount &&
                workingBillingInfoChanged() &&
                Db.getWorkingBillingInfoManager().workingBillingInfo.storedCard == null
    }

    private fun showSaveBillingInfoDialog() {
        val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.save_billing_info)
                .setCancelable(false)
                .setMessage(Phrase.from(context, R.string.save_billing_info_message_TEMPLATE)
                        .put("brand", BuildConfig.brand)
                        .format())
                .setPositiveButton(R.string.save, DialogInterface.OnClickListener { dialogInterface, i ->
                    userChoosesToSaveCard()
                })
                .setNegativeButton(R.string.no_thanks, DialogInterface.OnClickListener { dialogInterface, i ->
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
            viewmodel.toolbarTitle.onNext(getCheckoutToolbarTitle(resources))
            cardInfoContainer.visibility = View.VISIBLE
            paymentOptionsContainer.visibility = View.GONE
            billingInfoContainer.visibility = View.GONE
            validateAndBind()
        }
    }

    private val defaultToOptions = object : Presenter.Transition(PaymentDefault::class.java,
            PaymentOption::class.java) {
        override fun startTransition(forward: Boolean) {
            viewmodel.menuVisibility.onNext(false)
            cardInfoContainer.visibility = if (forward) View.GONE else View.VISIBLE
            paymentOptionsContainer.visibility = if (forward) View.VISIBLE else View.GONE
            paymentOptionGoogleWallet.visibility = if (WalletUtils.isWalletSupported(getLineOfBusiness())) View.VISIBLE else View.GONE
            billingInfoContainer.visibility = View.GONE
            viewmodel.toolbarTitle.onNext(if (forward) resources.getString(R.string.checkout_enter_payment_details) else getCheckoutToolbarTitle(resources))
            storedCreditCardList.bind()
            updateToolbarMenu(forward)
            if (!forward) validateAndBind()
        }
    }

    protected open fun updateToolbarMenu(forward: Boolean) {
        if (forward) {
            viewmodel.visibleMenuWithTitleDone.onNext(Unit)
            viewmodel.enableToolbarMenuButton.onNext(true)
        }
    }

    private val defaultToDetails = object : Presenter.Transition(PaymentDefault::class.java,
            PaymentDetails::class.java) {
        override fun endTransition(forward: Boolean) {
            viewmodel.menuVisibility.onNext(forward)
            viewmodel.toolbarTitle.onNext(if (forward) resources.getString(R.string.new_credit_debit_card) else getCheckoutToolbarTitle(resources))
            cardInfoContainer.visibility = if (forward) View.GONE else View.VISIBLE
            paymentOptionsContainer.visibility = View.GONE
            billingInfoContainer.visibility =if (forward) View.VISIBLE else View.GONE
            trackAnalytics()
            if (!forward) validateAndBind()
        }
    }

    private val optionsToDetails = object : Presenter.Transition(PaymentOption::class.java,
            PaymentDetails::class.java) {
        override fun endTransition(forward: Boolean) {
            viewmodel.menuVisibility.onNext(forward)
            viewmodel.toolbarTitle.onNext(resources.getString(if (forward) R.string.new_credit_debit_card else R.string.checkout_enter_payment_details))
            cardInfoContainer.visibility = View.GONE
            paymentOptionsContainer.visibility = if (forward) View.GONE else View.VISIBLE
            billingInfoContainer.visibility = if (forward) View.VISIBLE else View.GONE
            creditCardNumber.requestFocus()
            if (forward) {
                removeStoredCard()
                temporarilySavedCardIsSelected(false)
            }
            if (forward) Ui.showKeyboard(creditCardNumber, null) else Ui.hideKeyboard(this@PaymentWidget)
            storedCreditCardList.bind()
            trackAnalytics()
            if (!forward)  {
                viewmodel.visibleMenuWithTitleDone.onNext(Unit)
                viewmodel.enableToolbarMenuButton.onNext(true)
                validateAndBind()
            }
        }
    }

    fun removeStoredCard() {
        if (Db.getBillingInfo().hasStoredCard()) {
            val card = Db.getBillingInfo().storedCard
            viewmodel.storedCardRemoved.onNext(card)
        }
    }

    /** Tracking **/
    fun trackAnalytics() {
        if (!ExpediaBookingApp.isAutomation()) {
            if (getLineOfBusiness() == LineOfBusiness.HOTELSV2) {
                HotelV2Tracking().trackHotelV2PaymentEdit()
                if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCKOPostalCodeTest) && !PointOfSale.getPointOfSale().requiresHotelPostalCode()) {
                    sectionLocation.visibility = View.GONE
                }
            } else {
                OmnitureTracking.trackCheckoutPayment(getLineOfBusiness())
            }
        }
    }

    fun getCardType(): PaymentType {
        if (isCreditCardRequired() && hasStoredCard()) {
            return sectionBillingInfo.billingInfo.storedCard.type
        } else if (isCreditCardRequired() && (isFilled() && sectionBillingInfo.performValidation() && sectionLocation.performValidation())) {
            return sectionBillingInfo.billingInfo.paymentType
        }

        return PaymentType.UNKNOWN
    }

    open fun shouldShowPaymentOptions(): Boolean {
        return (User.isLoggedIn(context) && Db.getUser().storedCreditCards.isNotEmpty()) || WalletUtils.isWalletSupported(getLineOfBusiness())
    }

    private fun temporarilySavedCardIsSelected(isSelected: Boolean) {
        val info = Db.getTemporarilySavedCard()
        info?.saveCardToExpediaAccount = isSelected
    }

    fun userChoosesToSaveCard() {
        sectionBillingInfo.billingInfo.saveCardToExpediaAccount = true
        sectionBillingInfo.billingInfo.setIsTempCard(true)
        temporarilySavedCardIsSelected(true)
        Db.setTemporarilySavedCard(BillingInfo(sectionBillingInfo.billingInfo))
        storedCreditCardListener.onTemporarySavedCreditCardChosen(Db.getTemporarilySavedCard())
        close()
    }

    fun userChoosesNotToSaveCard() {
        sectionBillingInfo.billingInfo.saveCardToExpediaAccount = false
        sectionBillingInfo.billingInfo.setIsTempCard(true)
        close()
    }

    open fun close() {
        clearBackStack()
        val activity = context as Activity
        activity.onBackPressed()
    }

    open fun closePopup() {
        close()
    }
}