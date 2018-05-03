package com.expedia.bookings.customerfirst.vm

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.StringRes
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.customerfirst.model.CustomerFirstSupportModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.PlayStoreUtil
import com.expedia.bookings.utils.navigation.NavUtils
import com.mobiata.android.SocialUtils
import com.mobiata.android.util.AndroidUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class CustomerFirstSupportViewModel(val context: Context) {

    val customerFirstSupportObservable = PublishSubject.create<CustomerFirstSupportModel>()
    val refreshCustomerSupportSubject = PublishSubject.create<List<CustomerFirstSupportModel>>()

    init {
        customerFirstSupportObservable.subscribe {
            when (it) {
                CustomerFirstSupportModel.TWITTER -> {
                    OmnitureTracking.trackCustomerFirstTwitterClick()
                    customerSupportInvokeApps(it)
                }
                CustomerFirstSupportModel.FACEBOOK -> {
                    OmnitureTracking.trackCustomerFirstMessengerClick()
                    customerSupportInvokeApps(it)
                }
                CustomerFirstSupportModel.PHONE_CALL -> {
                    OmnitureTracking.trackCustomerFirstPhoneClick()
                    SocialUtils.call(context, context.getString(it.uriResId))
                }
                CustomerFirstSupportModel.HELP_TOPICS -> {
                    OmnitureTracking.trackCustomerFirstHelpTopicsClick()
                    val builder = WebViewActivity.IntentBuilder(context)
                            .setUrl(context.getString(R.string.customer_first_help_topics_url))
                            .setTitle(R.string.customer_first_help_topics)
                            .setRetryOnFailure(true)
                    NavUtils.startActivity(context, builder.intent, null)
                }
                else -> { // required to satisfy kotlin codestyle check
                    // Do nothing
                }
            }
        }
    }

    private fun customerSupportInvokeApps(customerFirstSupportModel: CustomerFirstSupportModel) {
        if (!AndroidUtils.isPackageInstalled(context, context.getString(customerFirstSupportModel.packageNameResId))) {
            showAppNotPresentDialog(context, customerFirstSupportModel)
        } else {
            openAppConfirmationDialog(context, customerFirstSupportModel)
        }
    }

    private fun showAppNotPresentDialog(context: Context, customerFirstSupportModel: CustomerFirstSupportModel) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(Phrase.from(context.getString(R.string.customer_first_app_not_present_TEMPLATE)).put("app_name", context.getString(customerFirstSupportModel.nameResId)).format().toString())
                .setPositiveButton(R.string.download, { _, _ ->
                    PlayStoreUtil.openPlayStore(context, context.getString(customerFirstSupportModel.packageNameResId))
                    customerFirstSupportModel.trackCustomerSupportDownloadClick()
                })
                .setNegativeButton(R.string.cancel, { dialog, _ ->
                    dialog.dismiss()
                    customerFirstSupportModel.trackCustomerSupportDownloadCancelClick()
                })
                .show()
    }

    private fun openAppConfirmationDialog(context: Context, customerFirstSupportModel: CustomerFirstSupportModel) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(Phrase.from(context.getString(R.string.customer_first_open_app_confirmation_TEMPLATE))
                .put("app_name", context.getString(customerFirstSupportModel.nameResId))
                .put("brand", BuildConfig.brand)
                .format().toString())
        builder.setPositiveButton(R.string.open, { _, _ ->
            openCustomerSupportURI(context, customerFirstSupportModel.uriResId)
            customerFirstSupportModel.trackCustomerSupportOpenAppClick()
        })
        builder.setNegativeButton(R.string.cancel, { dialog, _ ->
            dialog.dismiss()
            customerFirstSupportModel.trackCustomerSupportOpenCancelClick()
        })
        builder.show()
    }

    private fun openCustomerSupportURI(context: Context, @StringRes supportAppUri: Int) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(supportAppUri)))
        context.startActivity(intent)
    }

    fun getCustomerFirstSupportList(): List<CustomerFirstSupportModel> {
        var customerFirstSupportModelList: List<CustomerFirstSupportModel> = CustomerFirstSupportModel.values().asList()
        return customerFirstSupportModelList.subList(0, customerFirstSupportModelList.size - 1)
    }
}
