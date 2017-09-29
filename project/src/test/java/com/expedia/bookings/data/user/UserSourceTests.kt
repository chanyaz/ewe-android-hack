package com.expedia.bookings.data.user

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.FileCipher
import org.json.JSONException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class UserSourceTests {
    private val testUser: User
        get() {
            val user = User()
            val traveler = Traveler()
            traveler.firstName = "Paul"
            traveler.lastName = "Kite"
            user.primaryTraveler = traveler

            return user
        }

    @JvmField
    @Rule
    val expectedException: ExpectedException = ExpectedException.none()

    @Before
    fun setup() {
        Db.setUser(null)
    }

    @Test
    fun testGetUserReturnsNullWhenNoUser() {
        assertNull(Db.getUser())
        assertNull(UserSource(RuntimeEnvironment.application).user)
    }

    @Test
    fun testSetUserPopulatesDBUser() {
        val userSource = UserSource(RuntimeEnvironment.application)

        assertNull(Db.getUser())
        assertNull(userSource.user)

        userSource.user = testUser

        assertNotNull(Db.getUser())
    }

    @Test
    fun testGetUserCreatesUserSuccessfullyWhenUserIsNull() {
        val testFileCipher = TestFileCipher("whatever", " { tuid: 1 }")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        testUser.save(RuntimeEnvironment.application)

        assertNull(Db.getUser())
        assertNotNull(userSource.user)
        assertNotNull(Db.getUser())
    }

    @Test
    fun testGetUserReturnsSetUser() {
        val user = testUser
        val userSource = UserSource(RuntimeEnvironment.application)
        userSource.user = user

        assertEquals(userSource.user, user)
    }

    @Test
    fun testGetUserReturnsNullForNullSetUser() {
        val userSource = UserSource(RuntimeEnvironment.application)
        userSource.user = testUser
        userSource.user = null

        assertNull(userSource.user)
    }

    @Test(expected = FileNotFoundException::class)
    fun testLoadUserThrowsExceptionWhenUserDataFileIsMissing() {
        UserSource(RuntimeEnvironment.application).loadUser()
    }

    @Test
    fun testLoadUserThrowsExceptionWhenUnableToDecryptUserDataFile() {
        val testFileCipher = TestFileCipher(null)
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        testUser.save(RuntimeEnvironment.application)

        expectedException.expect(Exception::class.java)
        expectedException.expectMessage("FileCipher unable to initialize to decrypt user.dat")

        userSource.loadUser()
    }

    @Test
    fun testLoadUserThrowsExceptionWhenDecryptedFileIsNullOrEmpty() {
        val testFileCipher = TestFileCipher("whatever")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        testUser.save(RuntimeEnvironment.application)

        expectedException.expect(Exception::class.java)
        expectedException.expectMessage("Contents of decrypted user.dat file are null or empty.")

        userSource.loadUser()
    }

    @Test
    fun testLoadUserThrowsExceptionWhenUnableToCreateUserFromJSON() {
        val testFileCipher = TestFileCipher("whatever", "invalid JSON")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        testUser.save(RuntimeEnvironment.application)

        expectedException.expect(JSONException::class.java)

        userSource.loadUser()
    }

    @Test
    fun testLoadUserPopulatesUserWhenSuccessful() {
        val testFileCipher = TestFileCipher("whatever", " { tuid: 1 }")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        testUser.save(RuntimeEnvironment.application)

        assertNull(Db.getUser())

        userSource.loadUser()

        assertNotNull(Db.getUser())
        assertNotNull(userSource.user)
    }

    private class TestFileCipher(val password: String?, val results: String = "") : FileCipher(password) {
        override fun isInitialized(): Boolean = !password.isNullOrEmpty()
        override fun loadSecureData(file: File?): String = results
    }
}