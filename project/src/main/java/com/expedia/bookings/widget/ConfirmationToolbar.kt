package com.expedia.bookings.widget

import android.app.PendingIntent
import android.app.PendingIntent.getBroadcast
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.flights.FlightConfirmationShareBroadcastReceiver
import com.expedia.vm.ConfirmationToolbarViewModel
import com.mobiata.android.util.SettingUtils
import rx.Observer
import javax.inject.Inject

class ConfirmationToolbar(context: Context, attrs: AttributeSet?) : Toolbar(context, attrs) {

    lateinit var itinTripServices: ItinTripServices
        @Inject set
    lateinit var viewModel: ConfirmationToolbarViewModel

    val menuItem: MenuItem by lazy {
        val item = menu.findItem(R.id.menu_share)
        val variateForTest = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing)
        if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal) {
            item.icon = null
        }
        AccessibilityUtil.setMenuItemContentDescription(this, context.getString(R.string.share_action_content_description))
        item
    }

    val navIcon: Drawable by lazy {
        val navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        navIcon
    }
    val progressDialog = ProgressDialog(context)

    init {
        Ui.getApplication(getContext()).flightComponent().inject(this)

        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing)) {
            inflateMenu(R.menu.confirmation_menu)
            menuItem.setOnMenuItemClickListener {
                progressDialog.setCancelable(true)
                progressDialog.show()
                itinTripServices.getTripDetails(viewModel.tripId, makeNewItinResponseObserver())
                FlightsV2Tracking.trackConfirmationShareItinClicked()
                false
            }
        }

        setNavigationOnClickListener {
            NavUtils.goToLaunchScreen(context)
        }

        navigationIcon = navIcon
        setNavigationContentDescription(R.string.toolbar_nav_icon_close_cont_desc)
        navigationIcon?.setVisible(true, true)
    }

    private fun shareTrip(shareMessage: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        shareIntent.type = "text/plain"

        SettingUtils.save(getContext(), "TripType", "Flight")

        val receiver = Intent(getContext(), FlightConfirmationShareBroadcastReceiver::class.java)
        val pendingIntent = getBroadcast(getContext(), 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
        val chooserIntent = Intent.createChooser(shareIntent, "", pendingIntent.intentSender)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntent)
        getContext().startActivity(chooserIntent)
    }

    private fun makeNewItinResponseObserver(): Observer<AbstractItinDetailsResponse> {
        return object : Observer<AbstractItinDetailsResponse> {
            override fun onCompleted() {
                progressDialog.dismiss()
            }

            override fun onNext(itinDetailsResponse: AbstractItinDetailsResponse) {
                val shareMessage = viewModel.getShareMessage(itinDetailsResponse)
                shareTrip(shareMessage)
            }

            override fun onError(e: Throwable?) {

            }
        }
    }
}
