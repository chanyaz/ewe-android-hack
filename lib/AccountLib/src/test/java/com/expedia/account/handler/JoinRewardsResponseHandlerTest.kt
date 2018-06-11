package com.expedia.account.handler

import com.expedia.account.AccountService
import com.expedia.account.data.JoinRewardsResponse
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when` as whenever

class JoinRewardsResponseHandlerTest {
    private lateinit var sut: JoinRewardsResponseHandler
    private lateinit var mockAccountService: AccountService

    @Before
    fun setup() {
        mockAccountService = Mockito.mock(AccountService::class.java)
        sut = JoinRewardsResponseHandler(mockAccountService)
    }

    private fun setupRewardsServiceCall(isSuccess: Boolean) {
        val response = JoinRewardsResponse()
        if (isSuccess) {
            response.loyaltyMembershipActive = true
        } else {
            val mobileError = JoinRewardsResponse.MobileError()
            response.errors = listOf(mobileError)
        }

        whenever(mockAccountService.joinRewards())
                .thenReturn(Observable.just(response)
                        .observeOn(Schedulers.trampoline())
                        .subscribeOn(Schedulers.trampoline()))
    }

    @Test
    fun joinRewardsIsSuccessful() {
        setupRewardsServiceCall(true)
        val testObserver = TestObserver<JoinRewardsResponse>()
        sut.joinRewards(testObserver)

        testObserver.assertValueCount(1)
        testObserver.assertComplete()
        assertEquals(true, testObserver.values()[0].loyaltyMembershipActive)
    }

    @Test
    fun joinRewardsIsNotSuccessful() {
        setupRewardsServiceCall(false)
        val testObserver = TestObserver<JoinRewardsResponse>()
        sut.joinRewards(testObserver)

        testObserver.assertValueCount(1)
        testObserver.assertComplete()
        assertEquals(1, testObserver.values()[0].errors?.size)
    }
}
