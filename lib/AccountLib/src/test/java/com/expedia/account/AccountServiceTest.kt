package com.expedia.account

import com.expedia.account.data.AccountResponse
import com.expedia.account.data.JoinRewardsResponse
import com.expedia.account.data.PartialUser
import com.expedia.account.server.ExpediaAccountApi
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class AccountServiceTest {

    private val commonParams = mapOf(
            "langid" to "1033",
            "siteid" to "1",
            "clientid" to "android",
            "sourceType" to "mobileapp"
    )

    private lateinit var mockApi: ExpediaAccountApi
    private lateinit var sut: AccountService

    @Before
    fun setup() {
        mockApi = Mockito.mock(ExpediaAccountApi::class.java)
        sut = AccountService(mockApi, 1, 1033, "android", Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun signInProfileOnly_callsSignInProfileOnly() {
        Mockito.`when`(mockApi.signInProfileOnly(Mockito.anyBoolean(), Mockito.any()))
                .thenReturn(Observable.just(AccountResponse()))

        sut.signInProfileOnly()

        Mockito.verify(mockApi).signInProfileOnly(true, commonParams)
    }

    @Test
    fun signIn_callsSignIn() {
        Mockito.`when`(mockApi.signIn(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.any()))
                .thenReturn(Observable.just(AccountResponse()))

        sut.signIn("android@expedia.com", "secret", "recaptcha")

        Mockito.verify(mockApi).signIn("android@expedia.com", "secret", true, "recaptcha", commonParams)
    }

    @Test
    fun createUser_callsCreateUser() {
        Mockito.`when`(mockApi.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.any()))
                .thenReturn(Observable.just(AccountResponse()))

        val user = PartialUser()
        user.email = "android@expedia.com"
        user.password = "secret"
        user.firstName = "Android"
        user.lastName = "Test"
        user.enrollInLoyalty = true
        user.expediaEmailOptin = false
        user.recaptchaResponseToken = "recaptcha"

        sut.createUser(user)

        Mockito.verify(mockApi).createUser(user.email, user.password, user.firstName, user.lastName, user.expediaEmailOptin, true, user.enrollInLoyalty, user.recaptchaResponseToken, commonParams)
    }

    @Test
    fun joinRewards_callsJoinRewards() {
        Mockito.`when`(mockApi.joinRewards()).thenReturn(Observable.just(JoinRewardsResponse()))

        sut.joinRewards()

        Mockito.verify(mockApi).joinRewards()
    }
}
