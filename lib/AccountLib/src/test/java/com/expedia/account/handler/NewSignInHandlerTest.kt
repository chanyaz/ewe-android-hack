package com.expedia.account.handler

import com.expedia.account.AccountService
import com.expedia.account.AnalyticsListener
import com.expedia.account.Config
import com.expedia.account.R
import com.expedia.account.ViewWithLoadingIndicator
import com.expedia.account.data.AccountResponse
import com.expedia.account.util.MockNetworkConnectivity
import com.expedia.account.util.SimpleDialogBuilder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.`when` as whenever

class NewSignInHandlerTest {

    private val emailAddress = "joe@joe.com"
    private val password = "joejoe"

    private lateinit var sut: NewSignInHandler
    private lateinit var mockAnalyticsListener: AnalyticsListener
    private lateinit var mockAccountService: AccountService
    private lateinit var mockDialogBuilder: SimpleDialogBuilder
    private val networkConnectivity = MockNetworkConnectivity()

    @Before
    fun setup() {
        mockAnalyticsListener = Mockito.mock(AnalyticsListener::class.java)
        mockAccountService = Mockito.mock(AccountService::class.java)
        setupNetworkResponse(AccountResponse())

        mockDialogBuilder = Mockito.mock(SimpleDialogBuilder::class.java)

        val config = Mockito.mock(Config::class.java)
        whenever(config.analyticsListener).thenReturn(mockAnalyticsListener)
        whenever(config.service).thenReturn(mockAccountService)

        val loadingView = Mockito.mock(ViewWithLoadingIndicator::class.java)

        networkConnectivity.networkConnected = true

        sut = NewSignInHandler(mockDialogBuilder, networkConnectivity, config, emailAddress, password, loadingView)
    }

    private fun setupNetworkResponse(accountResponse: AccountResponse) {
        whenever(mockAccountService.signIn(any(), any(), any())).thenReturn(
                Observable.just(accountResponse)
                        .subscribeOn(Schedulers.trampoline())
                        .observeOn(Schedulers.trampoline())
        )
    }

    @Test
    fun recaptchaFailure_doesNotCallApi() {
        sut.onRecaptchaFailure()

        Mockito.verify(mockAccountService, Mockito.times(0)).signIn(any(), any(), any())
    }

    @Test
    fun recaptchaFailure_tracksAsRecaptchaError() {
        sut.onRecaptchaFailure()

        Mockito.verify(mockAnalyticsListener).userReceivedErrorOnSignInAttempt("Account:recaptcha failure")
    }

    @Test
    fun recaptchaFailure_showsGenericErrorDialog() {
        sut.onRecaptchaFailure()

        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(R.string.acct__Sign_in_failed_TITLE,
                        R.string.acct__Sign_in_failed_generic,
                        android.R.string.ok)
    }

    @Test
    fun recaptchaSuccess_sendsValidRecaptchaToken() {
        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockAccountService).signIn(emailAddress, password, "mytoken")
    }

    @Test
    fun recaptchaSuccess_apiError_tracksError() {
        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockAnalyticsListener).userReceivedErrorOnSignInAttempt("Account:server")
    }

    @Test
    fun recaptchaSuccess_apiAccountLocked_showsAccountLockedDialog() {
        val limitExceededResponse = AccountResponse()
        limitExceededResponse.detailedStatusMsg = "Login limit exceeded"
        setupNetworkResponse(limitExceededResponse)

        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(R.string.acct__Sign_in_locked_TITLE,
                        R.string.acct__Sign_in_locked,
                        android.R.string.ok)
    }

    @Test
    fun recaptchaSuccess_apiWrongPassword_showsSignInFailedDialog() {
        val limitExceededResponse = AccountResponse()
        limitExceededResponse.detailedStatusMsg = "WrongPassword"
        setupNetworkResponse(limitExceededResponse)

        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(R.string.acct__Sign_in_failed_TITLE,
                        R.string.acct__Sign_in_failed,
                        android.R.string.ok)
    }

    @Test
    fun recaptchaSuccess_noNetwork_showsNetworkErrorDialog() {
        networkConnectivity.networkConnected = false

        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(R.string.acct__Sign_in_failed_TITLE,
                        R.string.acct__no_network_connection,
                        android.R.string.ok)
    }
}
