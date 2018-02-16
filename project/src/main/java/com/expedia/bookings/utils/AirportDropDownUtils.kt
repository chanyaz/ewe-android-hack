package com.expedia.bookings.utils

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.support.v7.widget.ListPopupWindow
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.navigation.NavUtils

class AirportDropDownUtils {

    companion object {

        @JvmStatic fun createAirportListPopupWindow(context: Context, anchorView: View): ListPopupWindow {
            val listPopupWindow = ListPopupWindow(context)
            listPopupWindow.anchorView = anchorView
            listPopupWindow.verticalOffset = -anchorView.height
            listPopupWindow.inputMethodMode = ListPopupWindow.INPUT_METHOD_NEEDED
            return listPopupWindow
        }

        @JvmStatic fun fetchingRoutesProgressDialog(context: Context): ProgressDialog {
            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage(context.resources.getString(R.string.loading_air_asia_routes))
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            return progressDialog
        }

        @JvmStatic fun failedFetchingRoutesAlertDialog(context: Context): AlertDialog {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(context.resources.getString(R.string.error_could_not_load_air_asia))
            builder.setNeutralButton(context.resources.getString(R.string.ok), { _, _ -> NavUtils.goToLaunchScreen(context) })
            return builder.create()
        }
    }
}
