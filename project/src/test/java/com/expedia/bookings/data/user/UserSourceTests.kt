package com.expedia.bookings.data.user

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import org.json.JSONException
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @After
    fun tearDown() {
        RuntimeEnvironment.application.getFileStreamPath("user.dat").delete()
    }

    @Test
    fun testGetUserReturnsNullWhenNoUser() {
        assertNull(UserSource(RuntimeEnvironment.application).user)
    }

    @Test
    fun testGetUserCreatesUserSuccessfullyWhenUserIsNull() {
        val testFileCipher = TestFileCipher("whatever", " { tuid: 1 }")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        UserLoginTestUtil.createEmptyUserDataFile()

        assertNotNull(userSource.user)
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
        val testFileCipher = TestFileCipher(null, "")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        UserLoginTestUtil.createEmptyUserDataFile()

        expectedException.expect(Exception::class.java)
        expectedException.expectMessage("FileCipher unable to initialize to decrypt user.dat")

        userSource.loadUser()
    }

    @Test
    fun testLoadUserThrowsExceptionWhenDecryptedFileIsNullOrEmpty() {
        val testFileCipher = TestFileCipher("whatever", "")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        UserLoginTestUtil.createEmptyUserDataFile()

        expectedException.expect(Exception::class.java)
        expectedException.expectMessage("Contents of decrypted user.dat file are null or empty.")

        userSource.loadUser()
    }

    @Test
    fun testLoadUserDeletesUserFileWhenEmpty() {
        val testFileCipher = TestFileCipher("whatever", "")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        UserLoginTestUtil.createEmptyUserDataFile()

        assertTrue(RuntimeEnvironment.application.getFileStreamPath("user.dat").exists())
        assertNull(userSource.user)
        assertFalse(RuntimeEnvironment.application.getFileStreamPath("user.dat").exists())
    }

    @Test
    fun testLoadUserThrowsExceptionWhenUnableToCreateUserFromJSON() {
        val testFileCipher = TestFileCipher("whatever", "invalid JSON")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        UserLoginTestUtil.createEmptyUserDataFile()

        expectedException.expect(JSONException::class.java)

        userSource.loadUser()
    }

    @Test
    fun testLoadUserDeletesUserFileWhenUnableToCreateUserFromJSON() {
        val testFileCipher = TestFileCipher("whatever", "invalid JSON")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        UserLoginTestUtil.createEmptyUserDataFile()

        assertTrue(RuntimeEnvironment.application.getFileStreamPath("user.dat").exists())
        assertNull(userSource.user)
        assertFalse(RuntimeEnvironment.application.getFileStreamPath("user.dat").exists())
    }

    @Test
    fun testLoadUserPopulatesUserWhenSuccessful() {
        val testFileCipher = TestFileCipher("whatever", " { tuid: 1 }")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        UserLoginTestUtil.createEmptyUserDataFile()

        userSource.loadUser()

        assertNotNull(userSource.user)
    }

    @Test
    fun testSaveUserPersistsUserToDisk() {
        val testFileCipher = TestFileCipher("whatever")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        userSource.user = testUser

        assertTrue(RuntimeEnvironment.application.getFileStreamPath("user.dat").exists())
    }

    @Test
    fun testSaveUserWithNullUserDoesNothingIfNoUserExists() {
        val testFileCipher = TestFileCipher("whatever", "")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        userSource.user = null

        assertFalse(RuntimeEnvironment.application.getFileStreamPath("user.dat").exists())
    }

    @Test
    fun testSaveUserWithNullUserDeletesSaveFile() {
        val testFileCipher = TestFileCipher("whatever", "")
        val userSource = UserSource(RuntimeEnvironment.application, testFileCipher)

        userSource.user = testUser

        assertTrue(RuntimeEnvironment.application.getFileStreamPath("user.dat").exists())

        userSource.user = null

        assertFalse(RuntimeEnvironment.application.getFileStreamPath("user.dat").exists())
    }
}