package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinMapWidgetViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.util.notNullAndObservable
import java.util.Locale

class FlightItinMapWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val cardView by bindView<CardView>(R.id.flight_itin_map_card_view)
    val itinActionsButtons by bindView<ItinActionButtons>(R.id.itinActionButtons)
    val TERMINAL_MAP_BOTTOM_SHEET_TAG = "TERMINAL_MAP_BOTTOM_SHEET"

    var viewModel by notNullAndObservable<FlightItinMapWidgetViewModel> { vm ->
        vm.itinCardDataObservable
                .filter { it.flightLeg?.getSegment(0)?.originWaypoint?.airport?.latitude != null && it.flightLeg?.getSegment(0)?.originWaypoint?.airport?.longitude != null }
                .subscribe {
                    cardView.visibility = View.VISIBLE
                    itinActionsButtons.viewModel.rightButtonVisibilityObservable.onNext(true)
                    itinActionsButtons.viewModel.rightButtonDrawableObservable.onNext(R.drawable.ic_directions_icon_cta_button)
                    itinActionsButtons.viewModel.rightButtonTextObservable.onNext(context.resources.getString(R.string.itin_action_directions))
                }
        vm.itinCardDataObservable
                .filter { it.flightLeg?.getSegment(0)?.originWaypoint?.airport?.mAirportCode != null || it.flightLeg?.getSegment(0)?.destinationWaypoint?.airport?.mAirportCode != null }
                .subscribe {
                    cardView.visibility = View.VISIBLE
                    itinActionsButtons.viewModel.leftButtonVisibilityObservable.onNext(true)
                    itinActionsButtons.viewModel.leftButtonDrawableObservable.onNext(R.drawable.itin_flight_terminal_map_icon)
                    itinActionsButtons.viewModel.leftButtonTextObservable.onNext(context.resources.getString(R.string.itin_action_terminal_maps))
                }
    }

    init {
        View.inflate(context, R.layout.widget_flight_itin_map, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        itinActionsButtons.viewModel.leftButtonClickedObservable.subscribe {
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val terminalMapsDialog = FlightItinTerminalMapBottomSheet.newInstance(viewModel.itinCardDataObservable.value.flightLeg?.firstWaypoint?.airport?.mAirportCode, viewModel.itinCardDataObservable.value.flightLeg?.lastWaypoint?.airport?.mAirportCode)
            terminalMapsDialog.show(fragmentManager, TERMINAL_MAP_BOTTOM_SHEET_TAG)
            OmnitureTracking.trackItinNewFlightTerminalMaps()
        }
        itinActionsButtons.viewModel.rightButtonClickedObservable.subscribe {
            val airport = viewModel.itinCardDataObservable.value.flightLeg?.getSegment(0)?.originWaypoint?.airport
            val pattern = "geo:0,0?q=%f,%f(%s)"
            val uriStr = String.format(Locale.getDefault(), pattern, airport?.latitude, airport?.longitude, airport?.mName)
            val airportUri = Uri.parse(uriStr)
            val directionsIntent = Intent(ACTION_VIEW, airportUri)
            NavUtils.startActivitySafe(context, directionsIntent)
            OmnitureTracking.trackItinNewFlightDirections()
        }
    }
}