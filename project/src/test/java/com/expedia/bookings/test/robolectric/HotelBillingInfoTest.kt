package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.section.SectionBillingInfo
import com.expedia.bookings.section.SectionFieldEditable
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.NumberMaskEditText
import com.expedia.bookings.widget.PaymentWidgetV2
import com.expedia.vm.PaymentViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelBillingInfoTest {
    lateinit var sectionBillingInfo: com.expedia.bookings.section.SectionBillingInfo
    lateinit var cardNumbFieldEditable: SectionFieldEditable<EditText, BillingInfo>
    lateinit var cardNumbField: NumberMaskEditText
    lateinit var billingInfo: BillingInfo

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        val paymentWidget = android.view.LayoutInflater.from(activity).inflate(R.layout.payment_widget_v2, null) as PaymentWidgetV2
        paymentWidget.viewmodel = PaymentViewModel(activity)
        sectionBillingInfo = paymentWidget.findViewById<View>(R.id.section_billing_info) as SectionBillingInfo
        sectionBillingInfo.setLineOfBusiness(LineOfBusiness.HOTELS)

        cardNumbFieldEditable = sectionBillingInfo.mEditCreditCardNumber
        cardNumbField = cardNumbFieldEditable.field as NumberMaskEditText

        billingInfo = BillingInfo()
        cardNumbFieldEditable.bindData(billingInfo)
    }

    @Test
    fun testVisaDetection() {
        cardNumbField.setText("4111111111111111")
        assertEquals("VI", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_VISA", cardNumbFieldEditable.data.brandName)
    }

    @Test
    fun testCardBrandIconVisa() {
        billingInfo.brandName = "CARD_VISA"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_visa_grey)
    }

    @Test
    fun testMastercardDetection() {
        cardNumbField.setText("5105105105105100")
        assertEquals("CA", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_MASTERCARD", cardNumbFieldEditable.data.brandName)
    }

    @Test
    fun testCardBrandIconMastercard() {
        billingInfo.brandName = "CARD_MASTERCARD"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_master_card_grey)
    }

    @Test
    fun testMaestroDetection() {
        cardNumbField.setText("5001111100001111")
        assertEquals("TO", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_MAESTRO", cardNumbFieldEditable.data.brandName)
    }

    @Test
    fun testCardBrandIconMaestro() {
        billingInfo.brandName = "CARD_MAESTRO"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_maestro_grey)
    }

    @Test
    fun testAMEXDetection() {
        cardNumbField.setText("340000000000000")
        assertEquals("AX", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_AMERICAN_EXPRESS", cardNumbFieldEditable.data.brandName)
    }

    @Test
    fun testCardBrandIconAMEX() {
        billingInfo.brandName = "CARD_AMERICAN_EXPRESS"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_amex_grey)
    }

    @Test
    fun testDinersClubDetection() {
        cardNumbField.setText("60000000000000")
        assertEquals("DC", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_DINERS_CLUB", cardNumbFieldEditable.data.brandName)
    }

    @Test
    fun testCardBrandIconDinersClub() {
        billingInfo.brandName = "CARD_DINERS_CLUB"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_diners_club_grey)
    }

    @Test
    fun testChinaUnionClubDetection() {
        cardNumbField.setText("6200000000000000")
        assertEquals("CU", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_CHINA_UNION_PAY", cardNumbFieldEditable.data.brandName)
    }

    @Test
    fun testCardBrandIconChinaUnion() {
        billingInfo.brandName = "CARD_CHINA_UNION_PAY"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_union_pay_grey)
    }

    @Test
    fun testCarteBlancheDetection() {
        cardNumbField.setText("95000000000000")
        assertEquals("CB", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_CARTE_BLANCHE", cardNumbFieldEditable.data.brandName)
    }

    @Test
    fun testCardBrandIconCarteBlanche() {
        billingInfo.brandName = "CARD_CARTE_BLANCHE"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_carte_blanche_grey)
    }

    @Test
    fun testJCBDetection() {
        cardNumbField.setText("3530111333300000")
        assertEquals("JC", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_JAPAN_CREDIT_BUREAU", cardNumbFieldEditable.data.brandName)
    }

    @Test
    fun testCardBrandIconJCB() {
        billingInfo.brandName = "CARD_JAPAN_CREDIT_BUREAU"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_jcb_grey)
    }

    @Test
    fun testDiscoverDetection() {
        cardNumbField.setText("6011000990139424")
        assertEquals("DS", cardNumbFieldEditable.data.brandCode)
        assertEquals("CARD_DISCOVER", cardNumbFieldEditable.data.brandName)
        assertCardImageEquals(R.drawable.ic_discover_grey)
    }

    @Test
    fun testCardBrandIconDiscover() {
        billingInfo.brandName = "CARD_DISCOVER"
        billingInfo.number = "0"
        sectionBillingInfo.mDisplayCreditCardBrandIconGrey.bindData(billingInfo)
        assertCardImageEquals(R.drawable.ic_discover_grey)
    }

    @Test
    fun maxCardNumberLengthAllowed19() {
        val cardNumberWithLengthOf21Chars = "000000000000000000001"
        cardNumbField.setText(cardNumberWithLengthOf21Chars)
        assertEquals(19, cardNumbField.length())
    }

    @Test
    fun testImageSetWhenCardNumberEntered() {
        cardNumbField.setText("4111111111111111")
        assertCardImageEquals(R.drawable.ic_visa_grey)
    }

    private fun assertCardImageEquals(cardDrawableResId: Int) {
        val cardImageField = sectionBillingInfo.mDisplayCreditCardBrandIconGrey
        cardImageField.bindData(billingInfo)
        val shadowDrawable = Shadows.shadowOf(cardImageField.field.drawable)
        assertEquals(cardDrawableResId, shadowDrawable.createdFromResId)
    }
}
