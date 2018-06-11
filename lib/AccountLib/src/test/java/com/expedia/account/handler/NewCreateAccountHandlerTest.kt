package com.expedia.account.handler

import android.content.DialogInterface
import com.expedia.account.AccountService
import com.expedia.account.AnalyticsListener
import com.expedia.account.Config
import com.expedia.account.CreateAccountErrorRecoveryActions
import com.expedia.account.R
import com.expedia.account.ViewWithLoadingIndicator
import com.expedia.account.data.AccountResponse
import com.expedia.account.test.RecaptchaMatchers.Companion.hasRecaptchaToken
import com.expedia.account.util.MockNetworkConnectivity
import com.expedia.account.util.MockStringSource
import com.expedia.account.util.SimpleDialogBuilder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.argThat
import org.mockito.Mockito

class NewCreateAccountHandlerTest {

    private lateinit var sut: NewCreateAccountHandler
    private lateinit var mockAnalyticsListener: AnalyticsListener
    private lateinit var mockAccountService: AccountService
    private lateinit var mockDialogBuilder: SimpleDialogBuilder
    private lateinit var mockCreateAccountActions: CreateAccountErrorRecoveryActions
    private val networkConnectivity = MockNetworkConnectivity()

    @Before
    fun setup() {
        mockAnalyticsListener = Mockito.mock(AnalyticsListener::class.java)
        mockAccountService = Mockito.mock(AccountService::class.java)
        setupNetworkResponse(AccountResponse())

        mockDialogBuilder = Mockito.mock(SimpleDialogBuilder::class.java)

        val config = Mockito.mock(Config::class.java)
        Mockito.`when`(config.analyticsListener).thenReturn(mockAnalyticsListener)
        Mockito.`when`(config.service).thenReturn(mockAccountService)

        val loadingView = Mockito.mock(ViewWithLoadingIndicator::class.java)
        mockCreateAccountActions = Mockito.mock(CreateAccountErrorRecoveryActions::class.java)

        networkConnectivity.networkConnected = true

        sut = NewCreateAccountHandler(mockDialogBuilder, MockStringSource(), config, "Expedia", mockCreateAccountActions, loadingView)
    }

    private fun setupNetworkResponse(accountResponse: AccountResponse) {
        Mockito.`when`(mockAccountService.createUser(Mockito.any())).thenReturn(
                Observable.just(accountResponse)
                        .subscribeOn(Schedulers.trampoline())
                        .observeOn(Schedulers.trampoline())
        )
    }

    @Test
    fun recaptchaFailure_doesNotCallApi() {
        sut.onRecaptchaFailure()

        Mockito.verify(mockAccountService, Mockito.times(0)).createUser(Mockito.any())
    }

    @Test
    fun recaptchaFailure_tracksAsRecaptchaError() {
        sut.onRecaptchaFailure()

        Mockito.verify(mockAnalyticsListener).userReceivedErrorOnAccountCreationAttempt("Account:recaptcha failure")
    }

    @Test
    fun recaptchaFailure_showsGenericErrorDialog() {
        sut.onRecaptchaFailure()

        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(R.string.acct__Create_account_failed_TITLE,
                        R.string.acct__Create_account_failed,
                        android.R.string.ok)
    }

    @Test
    fun recaptchaSuccess_callsApi_withToken() {
        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockAccountService).createUser(argThat(hasRecaptchaToken("mytoken")))
    }

    @Test
    fun recaptchaSuccess_apiError_tracksError() {
        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockAnalyticsListener).userReceivedErrorOnAccountCreationAttempt("Account:server")
    }

    @Test
    fun recaptchaSuccess_emailAndPasswordIdentical_showsEmailAndPasswordIdenticalDialog() {
        setupNetworkResponseForErrorCode(AccountResponse.ErrorCode.EMAIL_PASSWORD_IDENTICAL_ERROR)

        sut.onRecaptchaSuccess("mytoken")

        val captor = ArgumentCaptor.forClass(DialogInterface.OnClickListener::class.java)
        Mockito.verify(mockDialogBuilder).showSimpleDialog(
                Mockito.eq(R.string.acct__Create_account_failed_TITLE),
                Mockito.eq(R.string.acct__email_password_identical),
                Mockito.eq(android.R.string.ok),
                captor.capture())

        captor.value.onClick(null, 0)
        Mockito.verify(mockCreateAccountActions).focusPasswordField()
    }

    private fun setupNetworkResponseForErrorCode(errorCode: AccountResponse.ErrorCode?) {
        val errorResponse = AccountResponse()
        errorResponse.errors = listOf(AccountResponse.AccountError())
        errorResponse.errors[0].errorCode = errorCode
        setupNetworkResponse(errorResponse)
    }

    @Test
    fun recaptchaSuccess_commonPassword_showsCommonPasswordDialog() {
        setupNetworkResponseForErrorCode(AccountResponse.ErrorCode.COMMON_PASSWORD_ERROR)

        sut.onRecaptchaSuccess("mytoken")

        val captor = ArgumentCaptor.forClass(DialogInterface.OnClickListener::class.java)
        Mockito.verify(mockDialogBuilder).showSimpleDialog(
                Mockito.eq(R.string.acct__Create_account_failed_TITLE),
                Mockito.eq(R.string.acct__common_password),
                Mockito.eq(android.R.string.ok),
                captor.capture())

        captor.value.onClick(null, 0)
        Mockito.verify(mockCreateAccountActions).focusPasswordField()
    }

    @Test
    fun recaptchaSuccess_unknownError_showsGenericErrorDialog() {
        setupNetworkResponseForErrorCode(null)

        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(R.string.acct__Create_account_failed_TITLE,
                        R.string.acct__Create_account_failed,
                        android.R.string.ok)
    }

    @Test
    fun recaptchaSuccess_duplicateEmail_promptsUserToSignInOrChangeEmail() {
        setupNetworkResponseForErrorCode(AccountResponse.ErrorCode.USER_SERVICE_DUPLICATE_EMAIL)

        sut.onRecaptchaSuccess("mytoken")

        Mockito.verify(mockDialogBuilder).showDialogWithItemList(
                safeEq("Expedia" + R.string.acct__Brand_account_already_exists_TITLE),
                safeEq(arrayOf<CharSequence?>(R.string.acct__Sign_in_to_my_existing_account.toString(),
                        R.string.acct__Create_a_new_account_with_different_email.toString())),
                Mockito.any())
    }

    private fun <T> safeEq(value: T) = Mockito.eq(value) ?: value

    @Test
    fun recaptchaSuccess_invalidEmail_showsInvalidEmailDialog() {
        setupNetworkResponseForInvalidField(AccountResponse.ErrorField.email)

        sut.onRecaptchaSuccess("mytoken")

        val captor = ArgumentCaptor.forClass(DialogInterface.OnClickListener::class.java)
        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(Mockito.eq(R.string.acct__Create_account_failed_TITLE),
                        Mockito.eq(R.string.acct__invalid_email_address),
                        Mockito.eq(android.R.string.ok),
                        captor.capture())

        captor.value.onClick(null, 0)
        Mockito.verify(mockCreateAccountActions).focusEmailAddressField()
    }

    private fun setupNetworkResponseForInvalidField(errorField: AccountResponse.ErrorField) {
        val errorResponse = AccountResponse()
        errorResponse.errors = listOf(AccountResponse.AccountError())
        errorResponse.errors[0].errorCode = AccountResponse.ErrorCode.INVALID_INPUT
        errorResponse.errors[0].errorInfo = AccountResponse.ErrorInfo()
        errorResponse.errors[0].errorInfo.field = errorField
        setupNetworkResponse(errorResponse)
    }

    @Test
    fun recaptchaSuccess_invalidFirstName_showsInvalidFirstNameDialog() {
        setupNetworkResponseForInvalidField(AccountResponse.ErrorField.firstName)

        sut.onRecaptchaSuccess("mytoken")

        val captor = ArgumentCaptor.forClass(DialogInterface.OnClickListener::class.java)
        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(Mockito.eq(R.string.acct__Create_account_failed_TITLE),
                        Mockito.eq(R.string.acct__invalid_first_name),
                        Mockito.eq(android.R.string.ok),
                        captor.capture())

        captor.value.onClick(null, 0)
        Mockito.verify(mockCreateAccountActions).focusFirstNameField()
    }

    @Test
    fun recaptchaSuccess_invalidLastName_showsInvalidLastNameDialog() {
        setupNetworkResponseForInvalidField(AccountResponse.ErrorField.lastName)

        sut.onRecaptchaSuccess("mytoken")

        val captor = ArgumentCaptor.forClass(DialogInterface.OnClickListener::class.java)
        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(Mockito.eq(R.string.acct__Create_account_failed_TITLE),
                        Mockito.eq(R.string.acct__invalid_last_name),
                        Mockito.eq(android.R.string.ok),
                        captor.capture())

        captor.value.onClick(null, 0)
        Mockito.verify(mockCreateAccountActions).focusLastNameField()
    }

    @Test
    fun recaptchaSuccess_invalidPassword_showsInvalidPasswordDialog() {
        setupNetworkResponseForInvalidField(AccountResponse.ErrorField.password)

        sut.onRecaptchaSuccess("mytoken")

        val captor = ArgumentCaptor.forClass(DialogInterface.OnClickListener::class.java)
        Mockito.verify(mockDialogBuilder)
                .showSimpleDialog(Mockito.eq(R.string.acct__Create_account_failed_TITLE),
                        Mockito.eq(R.string.acct__invalid_password),
                        Mockito.eq(android.R.string.ok),
                        captor.capture())

        captor.value.onClick(null, 0)
        Mockito.verify(mockCreateAccountActions).focusPasswordField()
    }
}
