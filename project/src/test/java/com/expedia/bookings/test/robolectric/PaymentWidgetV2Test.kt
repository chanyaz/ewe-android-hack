package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import com.expedia.bookings.extensions.getParentTextInputLayout
import com.expedia.bookings.extensions.getPaymentType
import com.expedia.bookings.hotel.animation.AlphaCalculator
import com.expedia.bookings.section.StoredCreditCardSpinnerAdapter
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isCreditCardMessagingForPayLaterEnabled
import com.expedia.bookings.utils.isShowSavedCoupons
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.PaymentWidgetV2
import com.expedia.bookings.widget.StoredCreditCardList
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.PayWithPointsViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.PaymentWidgetViewModel
import com.expedia.vm.ShopWithPointsViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class PaymentWidgetV2Test {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private val travelerFirstNameTestSubscriber = TestObserver<AccessibleEditText>()
    private val travelerLastNameTestSubscriber = TestObserver<AccessibleEditText>()
    private val populateCardholderTestSubscriber = TestObserver<String>()

    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private var shopWithPointsViewModel: ShopWithPointsViewModel by Delegates.notNull()
    private var sut: PaymentWidgetV2 by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var viewModel: PaymentViewModel by Delegates.notNull()
    lateinit var paymentTileInfo: TextView
    lateinit var paymentTileOption: TextView
    lateinit var paymentTileIcon: ImageView
    lateinit var pwpSmallIcon: ImageView
    lateinit var storedCardList: StoredCreditCardList
    lateinit var firstNameEditText: AccessibleEditText
    lateinit var lastNameEditText: AccessibleEditText
    lateinit var validCardsList: LinearLayout

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        Locale.setDefault(Locale.US)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppAllowUnknownCardTypes,
                AbacusUtils.EBAndroidAppAllowUnknownCardTypes,
                AbacusUtils.EBAndroidAppHotelMaterialForms,
                AbacusUtils.EBAndroidAppSavedCoupons)
        sut = android.view.LayoutInflater.from(activity).inflate(R.layout.payment_widget_v2, null) as PaymentWidgetV2
        viewModel = PaymentViewModel(activity)
        sut.viewmodel = viewModel
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(activity.applicationContext, paymentModel, UserLoginStateChangedModel())
        val payWithPointsViewModel = PayWithPointsViewModel(paymentModel, shopWithPointsViewModel, activity.applicationContext)
        sut.paymentWidgetViewModel = PaymentWidgetViewModel(activity.application, paymentModel, payWithPointsViewModel)

        paymentTileInfo = sut.findViewById<View>(R.id.card_info_name) as TextView
        paymentTileOption = sut.findViewById<View>(R.id.card_info_expiration) as TextView
        paymentTileIcon = sut.findViewById<View>(R.id.card_info_icon) as ImageView
        pwpSmallIcon = sut.findViewById<View>(R.id.pwp_small_icon) as ImageView
        storedCardList = sut.findViewById<View>(R.id.stored_creditcard_list) as StoredCreditCardList
        validCardsList = sut.findViewById<View>(R.id.valid_cards_list) as LinearLayout
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFullyPWPEnablesMenuButton() {
        val testObserver = TestObserver<Boolean>()
        sut.validateAndBind()
        sut.enableToolbarMenuButton.subscribe(testObserver)

        val createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemableOrbucksCreateTripResponse()
        createTripResponse.tripId = "happy"
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))

        testObserver.assertNoValues()

        paymentModel.createTripSubject.onNext(createTripResponse)

        testObserver.assertValue(true)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNonFullyPWPDisablesMenuButton() {
        val testObserver = TestObserver<Boolean>()
        sut.validateAndBind()
        sut.enableToolbarMenuButton.subscribe(testObserver)

        val createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        createTripResponse.tripId = "happy"
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))

        testObserver.assertNoValues()

        paymentModel.createTripSubject.onNext(createTripResponse)

        testObserver.assertValue(false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPaymentTile() {
        sut.validateAndBind()
        //For Paying With Points Only
        paymentModel.createTripSubject.onNext(getCreateTripResponse(true))
        testPaymentTileInfo("Paying with Points", "Tap to edit", ContextCompat.getDrawable(getContext(), R.drawable.pwp_icon)!!, View.GONE)

        //When user chooses to pay through card and reward points
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch.countDown() }
        paymentModel.burnAmountSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)
        setUserWithStoredCards()
        testPaymentTileInfo("Paying with Points & Visa 4111", "Tap to edit", ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful)!!, View.VISIBLE)
        //WithoutPayingWithPoints
        paymentModel.createTripSubject.onNext(getCreateTripResponse(false))
        setUserWithStoredCards()
        testPaymentTileInfo("Visa 4111", "Tap to edit", ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful)!!, View.GONE)
    }

    @Test
    fun testPaymentTitleWithPayWithPointsHotelWithDepositRequirement() {
        sut.validateAndBind()
        paymentModel.createTripSubject.onNext(getPayLaterResponse())
        sut.viewmodel.shouldShowPayLaterMessaging.onNext(true)

        testPaymentTileInfo("Enter payment details", "Only needed to confirm your booking", ContextCompat.getDrawable(getContext(), R.drawable.ic_checkout_default_creditcard)!!, View.GONE)
    }

    @Test
    fun testPaymentTitleWithPayWithPointsHotelWithNoDepositRequirement() {
        sut.validateAndBind()
        paymentModel.createTripSubject.onNext(getPayLaterResponse())

        testPaymentTileInfo("Enter payment details", "", ContextCompat.getDrawable(getContext(), R.drawable.ic_checkout_default_creditcard)!!, View.GONE)
    }

    @Test
    fun testUnsupportedCardIsNotSelected() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        sut.validateAndBind()
        paymentModel.createTripSubject.onNext(getCreateTripResponse(false))
        setUserWithStoredCards()
        sut.storedCreditCardList.bind()

        val listView = sut.storedCreditCardList.findViewById<View>(R.id.stored_card_list) as ListView
        assertNull(Db.getBillingInfo().storedCard)
        testPaymentTileInfo("Enter payment details", "", ContextCompat.getDrawable(getContext(), R.drawable.ic_checkout_default_creditcard)!!, View.GONE)
        assertEquals(1, listView.adapter.count)
        val tv = listView.adapter.getView(0, null, sut).findViewById<View>(R.id.text1) as TextView
        assertCardImageEquals(R.drawable.unsupported_card, tv)
        val errorMessage = "Hotel does not accept American Express"
        assertEquals(errorMessage, tv.text)
        assertEquals(errorMessage + ". Button, Disabled", tv.contentDescription.toString())
    }

    @Test
    fun testEmptyPopulateCardholderName() {
        setupCardholderNameSubscriptions()

        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        assertEquals("", populateCardholderTestSubscriber.values()[0])
        sut.populateCardholderName()
        assertEquals("", sut.creditCardName.text.toString())
    }

    @Test
    fun testInvalidPopulateCardholderName() {
        setupCardholderNameSubscriptions()

        firstNameEditText.setText("")
        lastNameEditText.setText("Lee")
        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        assertEquals("", populateCardholderTestSubscriber.values()[0])
        sut.populateCardholderName()
        assertEquals("", sut.creditCardName.text.toString())
    }

    @Test
    fun testPopulateAndDontRewriteCardholderName() {
        setupCardholderNameSubscriptions()

        firstNameEditText.setText("Bob")
        lastNameEditText.setText("Lee")
        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        assertTrue(populateCardholderTestSubscriber.valueCount() == 2)
        assertEquals("Bob Lee", populateCardholderTestSubscriber.values()[1])
        sut.populateCardholderName()
        assertEquals("Bob Lee", sut.creditCardName.text.toString())

        firstNameEditText.setText("John")
        lastNameEditText.setText("Lee")
        sut.creditCardName.setText("Bob Leee")
        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        sut.populateCardholderName()
        assertEquals("Bob Leee", sut.creditCardName.text.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPopulateReversedCardholderName() {
        setupCardholderNameSubscriptions()
        setPOSWithReversedName(true)

        firstNameEditText.setText("Bob")
        lastNameEditText.setText("Lee")
        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        assertTrue(populateCardholderTestSubscriber.valueCount() == 2)
        assertEquals("Lee Bob", populateCardholderTestSubscriber.values()[1])
        sut.populateCardholderName()
        assertEquals("Lee Bob", sut.creditCardName.text.toString())
        setPOSWithReversedName(false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testLoginPopulateCardholderName() {
        setupCardholderNameSubscriptions()

        firstNameEditText.setText("Lee")
        lastNameEditText.setText("Bob")
        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        assertTrue(populateCardholderTestSubscriber.valueCount() == 2)
        assertEquals("Lee Bob", populateCardholderTestSubscriber.values()[1])
        sut.populateCardholderName()
        assertEquals("Lee Bob", sut.creditCardName.text.toString())

        viewModel.userAuthenticationState.onNext(true)
        assertEquals("", sut.creditCardName.text.toString())

        firstNameEditText.setText("Joe")
        lastNameEditText.setText("Shmo")
        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        assertTrue(populateCardholderTestSubscriber.valueCount() == 4)
        assertEquals("Joe Shmo", populateCardholderTestSubscriber.values()[2])
        sut.populateCardholderName()
        assertEquals("Joe Shmo", sut.creditCardName.text.toString())
    }

    @Test
    fun testValidCardsListShouldShow() {
        assertTrue(validCardsList.visibility == View.VISIBLE)
    }

    @Test
    fun testHideGreyCardIconWhenShouldShowCardsList() {
        assertTrue(sut.greyCardIcon.visibility == View.GONE)
    }

    @Test
    fun testAmericanExpressCardShown() {
        val validFormsOfPayment = ArrayList<String>()
        validFormsOfPayment.add("AmericanExpress")
        val response = getCreateTripResponseWithValidFormsOfPayment(validFormsOfPayment)

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        assertValidCardsListShowsValidCards(validCardsList, response)
    }

    @Test
    fun testMultipleValidCardsShown() {
        val response = getCreateTripResponseWithValidFormsOfPayment(createValidFormOfPaymentStringList())

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        assertValidCardsListShowsValidCards(validCardsList, response)
    }

    @Test
    fun testDimAllCardsExceptAmericanExpress() {
        val response = getCreateTripResponseWithValidFormsOfPayment(createValidFormOfPaymentStringList())

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        sut.creditCardNumber.requestFocus()
        sut.creditCardNumber.setText("378282246310005")
        sut.creditCardPostalCode.requestFocus()

        assertAllCardsDimmedExceptValidCardType(validCardsList, PaymentType.CARD_AMERICAN_EXPRESS)
    }

    @Test
    fun testDimNoCards() {
        val response = getCreateTripResponseWithValidFormsOfPayment(createValidFormOfPaymentStringList())

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        sut.creditCardNumber.setText("1111")
        assertAllCardsAreNotDimmed()
    }

    @Test
    fun testCreditCardListStateWhenUnknowncardIsEntered() {
        val response = getCreateTripResponseWithValidFormsOfPayment(createValidFormOfPaymentStringList())

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        sut.creditCardNumber.requestFocus()
        sut.creditCardNumber.setText("12345678912345")
        sut.creditCardPostalCode.requestFocus()

        assertAllCardsAreNotDimmed()
    }

    @Test
    fun testWhetherRightCreditCardIsInList() {

        val validFormsOfPayment = createValidFormOfPaymentStringList()
        validFormsOfPayment.add("Maestro")
        validFormsOfPayment.add("AA")
        val response = getCreateTripResponseWithValidFormsOfPayment(validFormsOfPayment)

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        assertEquals(4, sut.validCardsList.childCount)

        assertCardTypeDisplayed(createValidFormOfPaymentList())
    }

    @Test
    fun testIsCreditCardMessagingFeatureEnabledForPayLaterHotel() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging, AbacusVariant.BUCKETED.value)

        assertTrue(isCreditCardMessagingForPayLaterEnabled(activity))
    }

    @Test
    fun testIsCreditCardMessagingFeatureDisabledForPayLaterHotel() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging)

        assertFalse(isCreditCardMessagingForPayLaterEnabled(activity))
    }

    @Test
    fun testForProperResetOfCardList() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        sut.validateAndBind()
        paymentModel.createTripSubject.onNext(getCreateTripResponse(false))
        setUserWithStoredCards()
        sut.storedCreditCardList.bind()

        sut.paymentOptionCreditDebitCard.performClick()

        assertAllCardsAreNotDimmed()
    }

    @Test
    fun testToolbarForHotelUniversalCheckoutWithHotelMaterialFormsTurnedOn() {
        setupHotelMaterialForms()
        val doneMenuVisibilitySubscriber = TestObserver<Unit>()
        sut.visibleMenuWithTitleDone.subscribe(doneMenuVisibilitySubscriber)

        sut.show(PaymentWidget.PaymentOption())
        sut.show(PaymentWidget.PaymentDetails())
        sut.paymentOptionCreditDebitCard.performClick()

        assertEquals(1, doneMenuVisibilitySubscriber.values().size)
    }

    @Test
    fun testPostalCodeNotShownForMaterialFormsWhenNotRequired() {
        setupHotelMaterialForms()
        sut.viewmodel.isZipValidationRequired.onNext(false)

        assertEquals(View.GONE, sut.postalCodeLayout.visibility)
    }

    @Test
    fun testPostalCodeShownForMaterialFormsWhenRequired() {
        setupHotelMaterialForms()
        sut.viewmodel.isZipValidationRequired.onNext(true)

        assertEquals(View.VISIBLE, sut.postalCodeLayout.visibility)
    }

    @Test
    fun testToolbarDoesNotChangeWhenUserEnterPaymentDetailsWithHotelMaterialFormsTurnedOn() {
        setupHotelMaterialForms()
        val nextMenuVisibilitySubscriber = TestObserver<Boolean>()
        sut.filledIn.subscribe(nextMenuVisibilitySubscriber)

        sut.paymentOptionCreditDebitCard.performClick()
        sut.creditCardNumber.setText("44444444444")

        assertEquals(0, nextMenuVisibilitySubscriber.values().size)
    }

    @Test
    fun testToolbarForHotelUniversalCheckoutWithHotelMaterialFormsTurnedOff() {
        val doneMenuVisibilitySubscriber = TestObserver<Unit>()
        sut.visibleMenuWithTitleDone.subscribe(doneMenuVisibilitySubscriber)

        sut.show(PaymentWidget.PaymentOption())
        sut.show(PaymentWidget.PaymentDetails())
        sut.paymentOptionCreditDebitCard.performClick()

        assertEquals(0, doneMenuVisibilitySubscriber.values().size)
    }

    @Test
    fun testMaterialPaymentFormErrorStates() {
        setupHotelMaterialForms()
        sut.sectionBillingInfo.performValidation()
        assertEquals("Enter a valid card number", sut.creditCardNumber.getParentTextInputLayout()!!.error)
        assertEquals("Enter a valid month and year", sut.expirationDate.getParentTextInputLayout()!!.error)
        assertEquals("Enter name as it appears on the card", sut.creditCardName.getParentTextInputLayout()!!.error)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMaterialZipCode() {
        setToUKPOS(false)
        setupHotelMaterialForms()
        assertEquals("Zip Code", sut.creditCardPostalCode.getParentTextInputLayout()!!.hint)
        assertEquals(InputType.TYPE_CLASS_NUMBER, sut.creditCardPostalCode.inputType)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMaterialPostalCode() {
        setToUKPOS(true)
        setupHotelMaterialForms()
        assertEquals("Postal Code", sut.creditCardPostalCode.getParentTextInputLayout()!!.hint)
        assertEquals(InputType.TYPE_CLASS_TEXT, sut.creditCardPostalCode.inputType)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMaterialZipCodeError() {
        setToUKPOS(false)
        setupHotelMaterialForms()
        sut.sectionBillingInfo.performValidation()
        assertEquals("Enter a valid zip code", sut.creditCardPostalCode.getParentTextInputLayout()!!.error)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMaterialPostalCodeError() {
        setToUKPOS(true)
        setupHotelMaterialForms()
        sut.sectionBillingInfo.performValidation()
        assertEquals("Enter a valid postal code", sut.creditCardPostalCode.getParentTextInputLayout()!!.error)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMaterialCreditCardNumberNotFocusedOnPaymentForm() {
        setupHotelMaterialForms()
        sut.show(PaymentWidget.PaymentOption())
        sut.show(PaymentWidget.PaymentDetails())
        sut.paymentOptionCreditDebitCard.performClick()
        assertFalse(sut.creditCardNumber.isFocused)
    }

    @Test
    fun testSavedCouponABTestEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppSavedCoupons)
        assertTrue(isShowSavedCoupons(getContext()))
    }

    @Test
    fun testSavedCouponABTestDefaultState() {
        assertFalse(isShowSavedCoupons(getContext()))
    }

    @Test
    fun testSavedCouponABTestDisabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppSavedCoupons, AbacusVariant.CONTROL.value)
        assertFalse(isShowSavedCoupons(getContext()))
    }

    @Test
    fun testErrorWhenPayableWithPointsAndInvalidPaymentInfo() {
        sut.isFullPayableWithPoints = true
        sut.creditCardNumber.setText("213412342134213421341234")
        sut.onDoneClicked()

        assertEquals(sut.sectionBillingInfo.firstInvalidField, sut.creditCardNumber)
    }

    @Test
    fun testStoredCreditCardSelectStatus() {
        setUserWithStoredCards(true)
        Db.getTripBucket().add(TripBucketItemHotelV2(getCreateTripResponseWithValidFormsOfPayment(createValidFormOfPaymentStringList())))
        sut.sectionBillingInfo.bind(BillingInfo())
        sut.selectFirstAvailableCard()
        val creditCardListAdapter = storedCardList.listView.adapter as StoredCreditCardSpinnerAdapter

        assertTrue(creditCardListAdapter.getItem(1).isSelectable)

        storedCardList.listView.performItemClick(storedCardList.listView.getChildAt(1), 1, 1)

        assertTrue(creditCardListAdapter.getItem(0).isSelectable)
    }

    private fun testPaymentTileInfo(paymentInfo: String, paymentOption: String, paymentIcon: Drawable, pwpSmallIconVisibility: Int) {
        assertEquals(paymentInfo, paymentTileInfo.text)
        assertEquals(paymentOption, paymentTileOption.text)
        assertEquals(paymentIcon, paymentTileIcon.drawable)
        assertEquals(pwpSmallIconVisibility, pwpSmallIcon.visibility)
    }

    private fun getCreateTripResponse(hasRedeemablePoints: Boolean): HotelCreateTripResponse {
        val createTripResponse: HotelCreateTripResponse
        if (hasRedeemablePoints)
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        else createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemablePointsCreateTripResponse()

        createTripResponse.tripId = "happy"
        Db.getTripBucket().clearHotelV2()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))

        return createTripResponse
    }

    private fun getPayLaterResponse(): HotelCreateTripResponse {
        val createTripResponse: HotelCreateTripResponse

        createTripResponse = mockHotelServiceTestRule.getPayLaterOfferCreateTripResponse()

        createTripResponse.tripId = "happy"
        Db.getTripBucket().clearHotelV2()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))

        return createTripResponse
    }

    private fun assertCardImageEquals(cardDrawableResId: Int, tv: TextView) {
        val shadow = Shadows.shadowOf(tv)
        assertEquals(cardDrawableResId, shadow.compoundDrawablesWithIntrinsicBoundsLeft)
    }

    private fun setUserWithStoredCards(multipleCardRequired: Boolean = false) {
        val user = User()
        user.addStoredCreditCard(getNewCard())
        if (multipleCardRequired) {
            user.addStoredCreditCard(getNewCard(PaymentType.CARD_MAESTRO))
        }
        val userStateManager = Ui.getApplication(RuntimeEnvironment.application).appComponent().userStateManager()
        UserLoginTestUtil.setupUserAndMockLogin(user, userStateManager)

        sut.viewmodel.isCreditCardRequired.onNext(true)
        sut.sectionBillingInfo.bind(BillingInfo())
        sut.selectFirstAvailableCard()
    }

    private fun getNewCard(paymentType: PaymentType = PaymentType.CARD_AMERICAN_EXPRESS): StoredCreditCard {
        val card = StoredCreditCard()
        card.cardNumber = "4111111111111111"
        card.id = "stored-card-id"
        card.type = paymentType
        card.description = "Visa 4111"
        card.setIsGoogleWallet(false)
        return card
    }

    private fun setupCardholderNameSubscriptions() {
        viewModel.travelerFirstName.subscribe(travelerFirstNameTestSubscriber)
        viewModel.travelerLastName.subscribe(travelerLastNameTestSubscriber)
        viewModel.populateCardholderNameObservable.subscribe(populateCardholderTestSubscriber)

        firstNameEditText = AccessibleEditText(activity.applicationContext, attributeSet = null)
        lastNameEditText = AccessibleEditText(activity.applicationContext, attributeSet = null)

        assertTrue(populateCardholderTestSubscriber.valueCount() == 1)
        assertEquals("", populateCardholderTestSubscriber.values()[0])
    }

    private fun setPOSWithReversedName(enable: Boolean) {
        val pointOfSale = if (enable) PointOfSaleId.JAPAN else PointOfSaleId.UNITED_STATES
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun assertValidCardsListShowsValidCards(validCardsList: LinearLayout, response: HotelCreateTripResponse) {
        val layoutChildCount = validCardsList.childCount
        val validPaymentTypesCount = response.validFormsOfPayment.size

        assertEquals(layoutChildCount, validPaymentTypesCount)

        for (i in 0..layoutChildCount - 1) {
            val cardInValidFormsRes = BookingInfoUtils.getCreditCardRectangularIcon(response.validFormsOfPayment[i].getPaymentType())
            val cardInList = (validCardsList.getChildAt(i) as ImageView)
            assertEquals(cardInList.tag, cardInValidFormsRes)
        }
    }

    private fun getCreateTripResponseWithValidFormsOfPayment(cardNames: ArrayList<String>): HotelCreateTripResponse {
        val response = HotelCreateTripResponse()
        val validFormsOfPayment = ArrayList<ValidFormOfPayment>()
        for (name in cardNames) {
            val validPayment = ValidFormOfPayment()
            validPayment.name = name
            ValidFormOfPaymentUtils.addValidPayment(validFormsOfPayment, validPayment)
        }
        response.validFormsOfPayment = validFormsOfPayment
        Db.getTripBucket().clearHotelV2()
        Db.getTripBucket().add(TripBucketItemHotelV2(response))

        return response
    }

    private fun assertAllCardsDimmedExceptValidCardType(validCardsList: LinearLayout, validType: PaymentType) {
        val validCardRes = BookingInfoUtils.getCreditCardRectangularIcon(validType)
        for (i in 0..(validCardsList.childCount - 1)) {
            val cardInList = (validCardsList.getChildAt(i) as ImageView)
            if (cardInList.tag == validCardRes) {
                assertEquals(AlphaCalculator.getAlphaValue(percentage = 100), cardInList.imageAlpha)
            } else {
                assertEquals(AlphaCalculator.getAlphaValue(percentage = 10), cardInList.imageAlpha)
            }
        }
    }

    private fun assertAllCardsAreNotDimmed() {
        for (i in 0..(validCardsList.childCount - 1)) {
            val cardInList = (validCardsList.getChildAt(i) as ImageView)
            assertEquals(AlphaCalculator.getAlphaValue(percentage = 100), cardInList.imageAlpha)
        }
    }

    private fun assertCardTypeDisplayed(validFormsOfPayment: ArrayList<PaymentType>) {
        for (i in 0..(validCardsList.childCount - 1)) {
            val cardInList = (validCardsList.getChildAt(i) as ImageView)
            assertEquals(cardInList.tag, BookingInfoUtils.getCreditCardRectangularIcon(validFormsOfPayment.get(i)))
        }
    }

    private fun createValidFormOfPaymentStringList(): ArrayList<String> {
        val validFormsOfPayment = ArrayList<String>()
        validFormsOfPayment.add("AmericanExpress")
        validFormsOfPayment.add("CarteBleue")
        validFormsOfPayment.add("MasterCard")
        validFormsOfPayment.add("Maestro")
        return validFormsOfPayment
    }

    private fun createValidFormOfPaymentList(): ArrayList<PaymentType> {
        val validFormsOfPayment = ArrayList<PaymentType>()
        validFormsOfPayment.add(PaymentType.CARD_AMERICAN_EXPRESS)
        validFormsOfPayment.add(PaymentType.CARD_CARTE_BLEUE)
        validFormsOfPayment.add(PaymentType.CARD_MASTERCARD)
        validFormsOfPayment.add(PaymentType.CARD_MAESTRO)
        return validFormsOfPayment
    }

    private fun setupHotelMaterialForms() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppHotelMaterialForms)
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        sut = android.view.LayoutInflater.from(activity).inflate(R.layout.material_payment_widget_v2, null) as PaymentWidgetV2
        viewModel = PaymentViewModel(activity)
        sut.viewmodel = viewModel
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(activity.applicationContext, paymentModel, UserLoginStateChangedModel())
        val payWithPointsViewModel = PayWithPointsViewModel(paymentModel, shopWithPointsViewModel, activity.applicationContext)
        sut.paymentWidgetViewModel = PaymentWidgetViewModel(activity.application, paymentModel, payWithPointsViewModel)

        sut.viewmodel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        sut.viewmodel.emptyBillingInfo.onNext(Unit)
    }

    private fun setToUKPOS(isUKPOS: Boolean) {
        val pointOfSale = if (isUKPOS) PointOfSaleId.UNITED_KINGDOM else PointOfSaleId.UNITED_STATES
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }
}
