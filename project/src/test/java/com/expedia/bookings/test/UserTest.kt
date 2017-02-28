package com.expedia.bookings.test

import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RoboTestHelper
import org.junit.Test
import kotlin.test.assertEquals

class UserTest {

    @Test
    fun testExpiredCreditCardWhenBucketed() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppRemoveExpiredCreditCards)
        val userWithExpiredCreditCard = getUserWithOneCardAndExpired()
        assertEquals(0, userWithExpiredCreditCard.storedCreditCards.size)
    }

    @Test
    fun testExpiredCreditCardWhenNotBucketed() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppRemoveExpiredCreditCards)
        val userWithExpiredCreditCard = getUserWithOneCardAndExpired()
        assertEquals(1, userWithExpiredCreditCard.storedCreditCards.size)
    }

    @Test
    fun testMultipleCardsWhenBucketed() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppRemoveExpiredCreditCards)
        val userWithExpiredCreditCard = getUserWithMultipleCards()
        assertEquals(4, userWithExpiredCreditCard.storedCreditCards.size)
        assertEquals(0, userWithExpiredCreditCard.storedCreditCards.filter { it.isExpired }.size)
    }

    @Test
    fun testMultipleCardsWhenNotBucketed() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppRemoveExpiredCreditCards)
        val userWithExpiredCreditCard = getUserWithMultipleCards()
        assertEquals(6, userWithExpiredCreditCard.storedCreditCards.size)
        assertEquals(2, userWithExpiredCreditCard.storedCreditCards.filter { it.isExpired }.size)
    }

    private fun getUserWithOneCardAndExpired(): User {
        val user = User()
        addCreditCardToUser(user, true)
        return user
    }

    private fun getUserWithMultipleCards(): User {
        val user = User()
        addCreditCardToUser(user, true)
        for (i in 2..5) {
            addCreditCardToUser(user, false)
        }
        addCreditCardToUser(user, true)
        return user
    }

    private fun addCreditCardToUser(user: User, isExpired: Boolean) {
        user.addStoredCreditCard(getDummyStoredCreditCard(isExpired))
    }

    private fun getDummyStoredCreditCard(isExpired: Boolean): StoredCreditCard {
        val storedCreditCard = StoredCreditCard()
        storedCreditCard.isExpired = isExpired
        return storedCreditCard
    }


}
