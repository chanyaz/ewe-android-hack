package com.expedia.account.handler

import android.content.DialogInterface
import com.expedia.account.Config
import com.expedia.account.CreateAccountErrorRecoveryActions
import com.expedia.account.NewAccountView
import com.expedia.account.R
import com.expedia.account.ViewWithLoadingIndicator
import com.expedia.account.data.AccountResponse
import com.expedia.account.data.Db
import com.expedia.account.recaptcha.RecaptchaHandler
import com.expedia.account.util.Events
import com.expedia.account.util.SimpleDialogBuilder
import com.expedia.account.util.StringSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class NewCreateAccountHandler(private val dialogBuilder: SimpleDialogBuilder,
                              private val stringSource: StringSource,
                              private val config: Config,
                              private val brand: String,
                              private val createAccountActions: CreateAccountErrorRecoveryActions,
                              private val loadingView: ViewWithLoadingIndicator) : RecaptchaHandler {

    private var accountLoadingDisposable: Disposable? = null

    override fun onRecaptchaSuccess(recaptchaResponseToken: String) {
        doCreateAccount(recaptchaResponseToken)
    }

    override fun onRecaptchaFailure() {
        showRecaptchaError()
    }

    fun doCreateAccount(recaptchaResponseToken: String?) {
        val user = Db.getNewUser()
        user.recaptchaResponseToken = recaptchaResponseToken
        config.service.createUser(user)
                .map { accountResponse ->
                    if (!accountResponse.tuid.isNullOrEmpty()) {
                        accountResponse.success = true
                    }
                    accountResponse
                }
                .subscribe(object : Observer<AccountResponse> {
                    override fun onComplete() {
                        accountLoadingDisposable?.dispose()
                        accountLoadingDisposable = null
                    }

                    override fun onError(e: Throwable) {
                        accountLoadingDisposable?.dispose()
                        accountLoadingDisposable = null
                        loadingView.cancelLoading()
                        showNetworkCreateAccountError()
                    }

                    override fun onSubscribe(d: Disposable) {
                        accountLoadingDisposable = d
                    }

                    override fun onNext(response: AccountResponse) {
                        if (!response.success) {
                            loadingView.cancelLoading()
                            showResponseCreateAccountError(response)
                        } else {
                            doCreateAccountSuccessful()
                        }
                    }
                })
    }

    private fun doCreateAccountSuccessful() {
        config.analyticsListener?.userSucceededInCreatingAccount()
        config.accountSignInListener?.onSignInSuccessful()
    }

    private fun showRecaptchaError() {
        config.analyticsListener?.userReceivedErrorOnAccountCreationAttempt("Account:recaptcha failure")
        showErrorGeneralDialog()
    }

    private fun showNetworkCreateAccountError() {
        config.analyticsListener?.userReceivedErrorOnAccountCreationAttempt("Account:local")
        showErrorNoNetworkDialog()
    }

    private fun showResponseCreateAccountError(response: AccountResponse) {
        config.analyticsListener?.userReceivedErrorOnAccountCreationAttempt("Account:" + (response.errors?.get(0)?.errorInfo?.cause ?: "server"))

        if (response.hasError(AccountResponse.ErrorCode.EMAIL_PASSWORD_IDENTICAL_ERROR)) {
            showErrorPasswordDialog(AccountResponse.ErrorCode.EMAIL_PASSWORD_IDENTICAL_ERROR)
            return
        }

        if (response.hasError(AccountResponse.ErrorCode.COMMON_PASSWORD_ERROR)) {
            showErrorPasswordDialog(AccountResponse.ErrorCode.COMMON_PASSWORD_ERROR)
            return
        }

        if (response.hasError(AccountResponse.ErrorCode.USER_SERVICE_DUPLICATE_EMAIL)) {
            showErrorAccountExistsDialog()
            return
        }

        if (response.hasError(AccountResponse.ErrorCode.INVALID_INPUT)) {
            val error = response.findError(AccountResponse.ErrorCode.INVALID_INPUT)
            when (error?.errorInfo?.field) {
                AccountResponse.ErrorField.email -> {
                    showErrorEmailDialog()
                    return
                }
                AccountResponse.ErrorField.password -> {
                    showErrorPasswordDialog(AccountResponse.ErrorCode.INVALID_INPUT)
                    return
                }
                AccountResponse.ErrorField.firstName -> {
                    showErrorFirstNameDialog()
                    return
                }
                AccountResponse.ErrorField.lastName -> {
                    showErrorLastNameDialog()
                    return
                }
                else ->
                    return
            }
        }
        // Catch all for anything else. (E.g. reCaptcha token error, which mAPI returns a response with a null errorCode)
        showErrorGeneralDialog()
    }

    private fun showErrorNoNetworkDialog() {
        showErrorMessageDialog(R.string.acct__no_network_connection, null)
    }

    private fun showErrorGeneralDialog() {
        showErrorMessageDialog(R.string.acct__Create_account_failed, null)
    }

    private fun showErrorAccountExistsDialog () {
        val signIn = NewAccountView.AccountTab.SIGN_IN.ordinal
        val createAccount = NewAccountView.AccountTab.CREATE_ACCOUNT.ordinal
        // Define the array here, instead of in arrays.xml, so that we
        // can be sure of the index, returned in the listener
        val items = arrayOfNulls<CharSequence>(NewAccountView.AccountTab.values().size)
        items[signIn] = stringSource.getString(R.string.acct__Sign_in_to_my_existing_account)
        items[createAccount] = stringSource.getString(R.string.acct__Create_a_new_account_with_different_email)

        val listener = DialogInterface.OnClickListener { _, which ->
            val user = Db.getNewUser()
            var usedExisting = false
            var createNew = false
            when (which) {
                signIn -> {
                    usedExisting = true
                    user.password = null
                    user.lastName = null
                    user.firstName = null
                    createAccountActions.showSignInPage()
                }
                createAccount -> {
                    createNew = true
                    user.email = null
                    createAccountActions.focusEmailAddressField()
                }
            }
            config.analyticsListener?.accountCreationAttemptWithPreexistingEmail(usedExisting, createNew)
            Events.post(Events.PartialUserDataChanged())
        }

        val title = stringSource.getBrandedString(R.string.acct__Brand_account_already_exists_TITLE, brand)

        dialogBuilder.showDialogWithItemList(title, items, listener)
    }

    private fun showErrorEmailDialog () {
        val okButtonClickListener = DialogInterface.OnClickListener { _, _ ->
            createAccountActions.focusEmailAddressField()
        }
        showErrorMessageDialog(R.string.acct__invalid_email_address, okButtonClickListener)
    }

    private fun showErrorPasswordDialog (errorCode: AccountResponse.ErrorCode) {

        val okButtonClickListener = DialogInterface.OnClickListener { _, _ ->
            createAccountActions.focusPasswordField()
        }

        val errorMessageId = when (errorCode) {
            AccountResponse.ErrorCode.EMAIL_PASSWORD_IDENTICAL_ERROR -> R.string.acct__email_password_identical
            AccountResponse.ErrorCode.COMMON_PASSWORD_ERROR -> R.string.acct__common_password
            else -> R.string.acct__invalid_password
        }
        showErrorMessageDialog(errorMessageId, okButtonClickListener)
    }

    private fun showErrorFirstNameDialog () {
        val okButtonClickListener = DialogInterface.OnClickListener { _, _ ->
            createAccountActions.focusFirstNameField()
        }
        showErrorMessageDialog(R.string.acct__invalid_first_name, okButtonClickListener)
    }

    private fun showErrorLastNameDialog () {
        val okButtonClickListener = DialogInterface.OnClickListener { _, _ ->
            createAccountActions.focusLastNameField()
        }
        showErrorMessageDialog(R.string.acct__invalid_last_name, okButtonClickListener)
    }

    private fun showErrorMessageDialog(messageId: Int, listener: DialogInterface.OnClickListener?) {
        dialogBuilder.showSimpleDialog(
                titleResId = R.string.acct__Create_account_failed_TITLE,
                messageResId = messageId,
                buttonLabelResId = android.R.string.ok,
                buttonClickListener = listener)
    }
}
