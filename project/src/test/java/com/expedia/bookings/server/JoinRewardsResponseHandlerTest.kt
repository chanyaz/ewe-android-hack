package com.expedia.bookings.server

import com.expedia.account.AccountService
import com.expedia.account.data.JoinRewardsResponse
import com.expedia.account.data.PartialUser
import com.expedia.account.handler.JoinRewardsResponseHandler
import com.expedia.bookings.services.MockJoinRewardsApi
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.annotations.NonNull
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class JoinRewardsResponseHandlerTest {
    private lateinit var accountService: AccountService
    private lateinit var joinRewardsResponsePublishSubject: PublishSubject<JoinRewardsResponse>
    private lateinit var joinRewardsResponse: JoinRewardsResponse
    private val joinRewardsResponseTestObserver = TestObserver<JoinRewardsResponse>()

    companion object {
        const val siteId = 0
        const val langId = 0
        const val clientId = "accountstest.phone.android"
        const val stubValue = "value"
    }

    @Before
    fun setup() {

        val immediate = object : Scheduler() {
            override fun scheduleDirect(@NonNull run: Runnable, delay: Long, @NonNull unit: TimeUnit): Disposable {
                return super.scheduleDirect(run, 0, unit)
            }

            override fun createWorker(): Worker {
                return ExecutorScheduler.ExecutorWorker(Runnable::run)
            }
        }

        RxJavaPlugins.setInitIoSchedulerHandler({ immediate })
        RxJavaPlugins.setInitComputationSchedulerHandler({ immediate })
        RxJavaPlugins.setInitNewThreadSchedulerHandler({ immediate })
        RxJavaPlugins.setInitSingleSchedulerHandler({ immediate })
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { immediate }

        joinRewardsResponse = JoinRewardsResponse()
        joinRewardsResponsePublishSubject = PublishSubject.create<JoinRewardsResponse>()
    }

    private fun setupRewardsServiceCall(isSuccess: Boolean) {
        accountService = AccountService(MockJoinRewardsApi(isSuccess), siteId, langId, clientId)
    }

    @Test
    fun joinRewardsIsSuccessful() {
        setupRewardsServiceCall(true)
        assertEquals(true, accountService.joinRewards().blockingFirst().loyaltyMembershipActive)
    }

    @Test
    fun joinRewardsIsNotSuccessful() {
        setupRewardsServiceCall(false)
        assertEquals(true, accountService.joinRewards().blockingFirst().errors!!.any())
    }

    @Test
    fun handlerGetsSetupCorrectly() {
        setupRewardsServiceCall(true)
        joinRewardsResponsePublishSubject.subscribe(joinRewardsResponseTestObserver)

        val handler = JoinRewardsResponseHandler(accountService)
        handler.joinRewards(joinRewardsResponsePublishSubject)

        joinRewardsResponseTestObserver.assertSubscribed()
    }

    @Test
    fun signInStubWorks() {
        setupRewardsServiceCall(true)
        val signInResponse = accountService.signIn(stubValue, stubValue, stubValue).blockingFirst()
        assertEquals(true, signInResponse != null)
    }

    @Test
    fun signInProfileStubWorks() {
        setupRewardsServiceCall(true)
        val signInProfileResponse = accountService.signInProfileOnly().blockingFirst()
        assertEquals(true, signInProfileResponse != null)
    }

    @Test
    fun createUserStubWorks() {
        setupRewardsServiceCall(true)
        val createUserResponse = accountService.createUser(PartialUser()).blockingFirst()
        assertEquals(true, createUserResponse != null)
    }

    @Test
    fun facebookAutoLoginStubWorks() {
        setupRewardsServiceCall(true)
        val facebookAutoLoginResponse = accountService.facebookAutoLogin(stubValue, stubValue).blockingFirst()
        assertEquals(true, facebookAutoLoginResponse != null)
    }

    @Test
    fun facebookLinkExistingAccountStubWorks() {
        setupRewardsServiceCall(true)
        val facebookLinkExistingAccountResponse = accountService.facebookLinkExistingAccount(stubValue, stubValue, stubValue, stubValue).blockingFirst()
        assertEquals(true, facebookLinkExistingAccountResponse != null)
    }
    @Test
    fun facebookLinkNewAccountStubWorks() {
        setupRewardsServiceCall(true)
        val facebookLinkNewAccountResponse = accountService.facebookLinkNewAccount(stubValue, stubValue, stubValue).blockingFirst()
        assertEquals(true, facebookLinkNewAccountResponse != null)
    }
}
