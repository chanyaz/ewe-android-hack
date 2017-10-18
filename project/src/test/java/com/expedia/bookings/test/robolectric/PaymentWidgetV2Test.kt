package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import com.expedia.bookings.data.utils.getPaymentType
import com.expedia.bookings.hotel.animation.AlphaCalculator
import com.expedia.bookings.services.LoyaltyServices
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
import com.expedia.bookings.utils.isDisplayCardsOnPaymentForm
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
import rx.observers.TestSubscriber
import java.math.BigDecimal
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

    private val travelerFirstNameTestSubscriber = TestSubscriber<AccessibleEditText>()
    private val travelerLastNameTestSubscriber = TestSubscriber<AccessibleEditText>()
    private val populateCardholderTestSubscriber = TestSubscriber<String>()

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
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm, AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
        SettingUtils.save(activity, R.string.preference_display_eligible_cards_on_payment_form, false)
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
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPaymentTile() {
        sut.validateAndBind()
        //For Paying With Points Only
        paymentModel.createTripSubject.onNext(getCreateTripResponse(true))
        testPaymentTileInfo("Paying with Points", "Tap to edit", ContextCompat.getDrawable(getContext(), R.drawable.pwp_icon), View.GONE)

        //When user chooses to pay through card and reward points
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch.countDown() }
        paymentModel.burnAmountSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)
        setUserWithStoredCard()
        testPaymentTileInfo("Paying with Points & Visa 4111", "Tap to edit", ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful), View.VISIBLE)

        //WithoutPayingWithPoints
        paymentModel.createTripSubject.onNext(getCreateTripResponse(false))
        setUserWithStoredCard()
        testPaymentTileInfo("Visa 4111", "Tap to edit", ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful), View.GONE)
    }

    @Test
    fun testUnsupportedCardIsNotSelected() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        sut.validateAndBind()
        paymentModel.createTripSubject.onNext(getCreateTripResponse(false))
        setUserWithStoredCard()
        sut.storedCreditCardList.bind()

        val listView = sut.storedCreditCardList.findViewById<View>(R.id.stored_card_list) as ListView
        assertNull(Db.getBillingInfo().storedCard)
        testPaymentTileInfo("Enter payment details", "", ContextCompat.getDrawable(getContext(), R.drawable.ic_checkout_default_creditcard), View.GONE)
        assertEquals(1, listView.adapter.count)
        val tv = listView.adapter.getView(0, null, sut).findViewById<View>(R.id.text1) as TextView
        assertCardImageEquals(R.drawable.unsupported_card, tv)
        val errorMessage = "Hotel does not accept American Express"
        assertEquals(errorMessage, tv.text)
        assertEquals(errorMessage + ", disabled Button", tv.contentDescription.toString())
    }

    @Test
    fun testEmptyPopulateCardholderName() {
        setupCardholderNameSubscriptions()

        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        assertEquals("", populateCardholderTestSubscriber.onNextEvents[0])
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
        assertEquals("", populateCardholderTestSubscriber.onNextEvents[0])
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
        assertTrue(populateCardholderTestSubscriber.onNextEvents.size == 2)
        assertEquals("Bob Lee", populateCardholderTestSubscriber.onNextEvents[1])
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
        assertTrue(populateCardholderTestSubscriber.onNextEvents.size == 2)
        assertEquals("Lee Bob", populateCardholderTestSubscriber.onNextEvents[1])
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
        assertTrue(populateCardholderTestSubscriber.onNextEvents.size == 2)
        assertEquals("Lee Bob", populateCardholderTestSubscriber.onNextEvents[1])
        sut.populateCardholderName()
        assertEquals("Lee Bob", sut.creditCardName.text.toString())

        viewModel.userLogin.onNext(true)
        assertEquals("", sut.creditCardName.text.toString())

        firstNameEditText.setText("Joe")
        lastNameEditText.setText("Shmo")
        viewModel.travelerFirstName.onNext(firstNameEditText)
        viewModel.travelerLastName.onNext(lastNameEditText)
        assertTrue(populateCardholderTestSubscriber.onNextEvents.size == 4)
        assertEquals("Joe Shmo", populateCardholderTestSubscriber.onNextEvents[2])
        sut.populateCardholderName()
        assertEquals("Joe Shmo", sut.creditCardName.text.toString())
    }

    @Test
    fun testToggleOnDisplayCardsOnPaymentForm() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity,
                AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm,
                R.string.preference_display_eligible_cards_on_payment_form)
        assertTrue(isDisplayCardsOnPaymentForm(activity))
    }

    @Test
    fun testToggleOffDisplayCardsOnPaymentForm() {
        AbacusTestUtils.unbucketTestAndDisableFeature(activity,
                AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm,
                R.string.preference_display_eligible_cards_on_payment_form)
        assertFalse(isDisplayCardsOnPaymentForm(activity))
    }

    @Test
    fun testValidCardsListShouldShow() {
        setupAndShowValidCardsList()
        assertTrue(validCardsList.visibility == View.VISIBLE)
    }

    @Test
    fun testValidCardsListShouldNotShow() {
        assertFalse(sut.validCardsList.visibility == View.VISIBLE)
    }

    @Test
    fun testHideGreyCardIconWhenShouldShowCardsList() {
        setupAndShowValidCardsList()
        assertTrue(sut.greyCardIcon.visibility == View.GONE)
    }

    @Test
    fun testAmericanExpressCardShown() {
        val validFormsOfPayment = ArrayList<String>()
        validFormsOfPayment.add("AmericanExpress")
        val response = getCreateTripResponseWithValidFormsOfPayment(validFormsOfPayment)
        setupAndShowValidCardsList()

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        assertValidCardsListShowsValidCards(validCardsList, response)
    }

    @Test
    fun testMultipleValidCardsShown() {
        val validFormsOfPayment = ArrayList<String>()
        validFormsOfPayment.add("AmericanExpress")
        validFormsOfPayment.add("CarteBleue")
        validFormsOfPayment.add("Mastercard")
        validFormsOfPayment.add("Maestro")
        val response = getCreateTripResponseWithValidFormsOfPayment(validFormsOfPayment)
        setupAndShowValidCardsList()

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        assertValidCardsListShowsValidCards(validCardsList, response)
    }

    @Test
    fun testDimAllCardsExceptAmericanExpress() {
        val validFormsOfPayment = ArrayList<String>()
        validFormsOfPayment.add("AmericanExpress")
        validFormsOfPayment.add("CarteBleue")
        validFormsOfPayment.add("Mastercard")
        validFormsOfPayment.add("Maestro")
        val response = getCreateTripResponseWithValidFormsOfPayment(validFormsOfPayment)
        setupAndShowValidCardsList()

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        sut.creditCardNumber.requestFocus()
        sut.creditCardNumber.setText("378282246310005")
        sut.creditCardPostalCode.requestFocus()

        assertAllCardsDimmedExceptValidCardType(validCardsList, PaymentType.CARD_AMERICAN_EXPRESS)
    }

    @Test
    fun testDimNoCards() {
        val validFormsOfPayment = ArrayList<String>()
        validFormsOfPayment.add("AmericanExpress")
        validFormsOfPayment.add("CarteBleue")
        validFormsOfPayment.add("Mastercard")
        validFormsOfPayment.add("Maestro")
        val response = getCreateTripResponseWithValidFormsOfPayment(validFormsOfPayment)
        setupAndShowValidCardsList()

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        sut.creditCardNumber.setText("1111")
        assertAllCardsAreNotDimmed()
    }

    @Test
    fun testCreditCardListStateWhenUnknowncardIsEntered() {
        val validFormsOfPayment = ArrayList<String>()
        validFormsOfPayment.add("AmericanExpress")
        validFormsOfPayment.add("CarteBleue")
        validFormsOfPayment.add("Mastercard")
        validFormsOfPayment.add("Maestro")
        val response = getCreateTripResponseWithValidFormsOfPayment(validFormsOfPayment)
        setupAndShowValidCardsList()

        sut.viewmodel.showValidCards.onNext(response.validFormsOfPayment)

        sut.creditCardNumber.requestFocus()
        sut.creditCardNumber.setText("12345678912345")
        sut.creditCardPostalCode.requestFocus()

        assertAllCardsAreNotDimmed()
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
        else
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemablePointsCreateTripResponse()

        createTripResponse.tripId = "happy"
        Db.getTripBucket().clearHotelV2()
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))

        return createTripResponse
    }

    private fun assertCardImageEquals(cardDrawableResId: Int, tv: TextView) {
        val shadow = Shadows.shadowOf(tv)
        assertEquals(cardDrawableResId, shadow.compoundDrawablesWithIntrinsicBoundsLeft)
    }

    private fun setUserWithStoredCard() {
        val user = User()
        user.addStoredCreditCard(getNewCard())

        val userStateManager = Ui.getApplication(RuntimeEnvironment.application).appComponent().userStateManager()
        userStateManager.userSource.user = user

        sut.viewmodel.isCreditCardRequired.onNext(true)
        sut.sectionBillingInfo.bind(BillingInfo())
        sut.selectFirstAvailableCard()
    }

    private fun getNewCard(): StoredCreditCard {
        val card = StoredCreditCard()

        card.cardNumber = "4111111111111111"
        card.id = "stored-card-id"
        card.type = PaymentType.CARD_AMERICAN_EXPRESS
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

        assertTrue(populateCardholderTestSubscriber.onNextEvents.size == 1)
        assertEquals("", populateCardholderTestSubscriber.onNextEvents[0])
    }

    private fun setPOSWithReversedName(enable: Boolean) {
        val pointOfSale = if (enable) PointOfSaleId.JAPAN else PointOfSaleId.UNITED_STATES
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun setupAndShowValidCardsList() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm, AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
        SettingUtils.save(activity, R.string.preference_display_eligible_cards_on_payment_form, true)
        sut = android.view.LayoutInflater.from(activity).inflate(R.layout.payment_widget_v2, null) as PaymentWidgetV2
        viewModel = PaymentViewModel(activity)
        sut.viewmodel = viewModel
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(activity.applicationContext, paymentModel, UserLoginStateChangedModel())
        val payWithPointsViewModel = PayWithPointsViewModel(paymentModel, shopWithPointsViewModel, activity.applicationContext)
        sut.paymentWidgetViewModel = PaymentWidgetViewModel(activity.application, paymentModel, payWithPointsViewModel)
        validCardsList = sut.findViewById<View>(R.id.valid_cards_list) as LinearLayout
    }

    private fun assertValidCardsListShowsValidCards(validCardsList: LinearLayout, response: HotelCreateTripResponse) {
        val layoutChildCount = validCardsList.childCount
        val validPaymentTypesCount = response.validFormsOfPayment.size

        assertEquals(layoutChildCount, validPaymentTypesCount)

        for (i in 0..layoutChildCount - 1) {
            val cardInValidFormsRes = BookingInfoUtils.getCreditCardIcon(response.validFormsOfPayment[i].getPaymentType())
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
        val validCardRes = BookingInfoUtils.getCreditCardIcon(validType)
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
}
