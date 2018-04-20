package com.expedia.account.handler

import android.content.Context
import android.content.DialogInterface
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import com.expedia.account.Config
import com.expedia.account.NewAccountView
import com.expedia.account.R
import com.expedia.account.data.AccountResponse
import com.expedia.account.data.Db
import com.expedia.account.newsignin.NewCreateAccountLayout
import com.expedia.account.recaptcha.RecaptchaHandler
import com.expedia.account.util.Events
import com.expedia.account.util.Utils
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class NewCreateAccountHandler(private val context: Context, private val config: Config, val brand: String?, private val createAccountLayout: NewCreateAccountLayout,
                              private val viewPager: ViewPager) : RecaptchaHandler {

    private var accountLoadingDisposable: Disposable? = null

    override fun onRecaptchaSuccess(recaptchaResponseToken: String) {
        doCreateAccount(recaptchaResponseToken)
    }

    override fun onRecaptchaFailure() {
        doCreateAccount(null)
    }

    fun doCreateAccount(recaptchaResponseToken: String?) {
        val user = Db.getNewUser()
        user.recaptchaResponseToken = recaptchaResponseToken
        config.service.createUser(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map({ accountResponse ->
                    if (!TextUtils.isEmpty(accountResponse.tuid)) {
                        accountResponse.success = true
                    }
                    accountResponse
                })
                .subscribe(object : Observer<AccountResponse> {
                    override fun onComplete() {
                        accountLoadingDisposable?.dispose()
                        accountLoadingDisposable = null
                    }

                    override fun onError(e: Throwable) {
                        accountLoadingDisposable?.dispose()
                        accountLoadingDisposable = null
                        showCreateAccountError(e)
                    }

                    override fun onSubscribe(d: Disposable) {
                        accountLoadingDisposable = d
                    }

                    override fun onNext(response: AccountResponse) {
                        if (!response.success) {
                            showCreateAccountError(response)
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

    private fun showCreateAccountError(throwable: Throwable) {
        config.analyticsListener?.userReceivedErrorOnAccountCreationAttempt("Account:local")
        showCreateAccountErrorGeneric()
    }

    private fun showCreateAccountErrorGeneric() {
        showErrorMessageDialog(
                if (Utils.isOnline(context)) {
                    R.string.acct__Create_account_failed
                } else {
                    R.string.acct__no_network_connection
                }, null)
    }

    private fun showCreateAccountError(response: AccountResponse) {
        config.analyticsListener?.userReceivedErrorOnAccountCreationAttempt("Account" + response.errors[0].errorInfo?.cause ?: "server")

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
        showCreateAccountErrorGeneric()
    }

    private fun showErrorAccountExistsDialog () {
        val res = context.resources
        val signIn = NewAccountView.AccountTab.SIGN_IN.ordinal
        val createAccount = NewAccountView.AccountTab.CREATE_ACCOUNT.ordinal
        // Define the array here, instead of in arrays.xml, so that we
        // can be sure of the index, returned in the listener
        val items = arrayOfNulls<CharSequence>(NewAccountView.AccountTab.values().size)
        items[signIn] = res.getString(R.string.acct__Sign_in_to_my_existing_account)
        items[createAccount] = res.getString(R.string.acct__Create_a_new_account_with_different_email)

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
                    viewPager.currentItem = signIn
                }
                createAccount -> {
                    createNew = true
                    user.email = null
                    createAccountLayout.focusEmailAddress()
                }
            }
            config.analyticsListener?.accountCreationAttemptWithPreexistingEmail(usedExisting, createNew)
            Events.post(Events.PartialUserDataChanged())
        }

        val title = Utils.obtainBrandedPhrase(
                context, R.string.acct__Brand_account_already_exists_TITLE, brand)
                .format()

        AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(items, listener)
                .create()
                .show()
    }

    private fun showErrorEmailDialog () {
        val okButtonClickListener = DialogInterface.OnClickListener { _, _ ->
            createAccountLayout.focusEmailAddress()
        }
        showErrorMessageDialog(R.string.acct__invalid_email_address, okButtonClickListener)
    }

    private fun showErrorPasswordDialog (errorCode: AccountResponse.ErrorCode) {

        val okButtonClickListener = DialogInterface.OnClickListener { _, _ ->
            createAccountLayout.focusPassword()
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
            createAccountLayout.focusFirstName()
        }
        showErrorMessageDialog(R.string.acct__invalid_first_name, okButtonClickListener)
    }

    private fun showErrorLastNameDialog () {
        val okButtonClickListener = DialogInterface.OnClickListener { _, _ ->
            createAccountLayout.focusLastName()
        }
        showErrorMessageDialog(R.string.acct__invalid_last_name, okButtonClickListener)
    }

    private fun showErrorMessageDialog(messageId: Int, listener: DialogInterface.OnClickListener?) {
        AlertDialog.Builder(context)
                .setTitle(R.string.acct__Create_account_failed_TITLE)
                .setMessage(messageId)
                .setPositiveButton(android.R.string.ok, listener)
                .create()
                .show()
    }
}
