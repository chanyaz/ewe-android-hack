package com.expedia.bookings.test

import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.user.User
import org.junit.Test
import kotlin.test.assertEquals

class UserTest {

    @Test
    fun testExpiredCreditCard() {
        val userWithExpiredCreditCard = getUserWithOneCardAndExpired()
        assertEquals(0, userWithExpiredCreditCard.storedCreditCards.size)
    }

    @Test
    fun testMultipleCards() {
        val userWithExpiredCreditCard = getUserWithMultipleCardsIncludingExpired()
        assertEquals(4, userWithExpiredCreditCard.storedCreditCards.size)
        assertEquals(0, userWithExpiredCreditCard.storedCreditCards.filter { it.isExpired }.size)
    }

    private fun getUserWithOneCardAndExpired(): User {
        val user = User()
        addCreditCardToUser(user, true)
        return user
    }

    private fun getUserWithMultipleCardsIncludingExpired(): User {
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
