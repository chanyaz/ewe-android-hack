package com.expedia.bookings.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.TableRow
import android.widget.TextView
import com.expedia.bookings.R
import com.squareup.phrase.Phrase

class PendingPointsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments.getString("title")
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle(title)

        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.fragment_dialog_pending_points, null)
        alertDialogBuilder.setView(view)

        val flightDaysText = view.findViewById(R.id.flights_days) as TextView
        val flightRow = view.findViewById(R.id.flight_points_row) as TableRow
        flightRow.contentDescription = createAccessibilityText(R.string.pending_points_flights_accessibility_TEMPLATE, FLIGHT_DAYS)
        flightDaysText.text = createDaysText(FLIGHT_DAYS)

        val bundleDaysText = view.findViewById(R.id.packages_days) as TextView
        val bundleRow = view.findViewById(R.id.bundles_points_row) as TableRow
        bundleRow.contentDescription = createAccessibilityText(R.string.pending_points_bundles_accessibility_TEMPLATE, BUNDLE_DAYS)
        bundleDaysText.text = createDaysText(BUNDLE_DAYS)

        val activitiesDaysText = view.findViewById(R.id.activities_days) as TextView
        val activityRow = view.findViewById(R.id.activities_points_row) as TableRow
        activityRow.contentDescription = createAccessibilityText(R.string.pending_points_activities_accessibility_TEMPLATE, ACTIVITY_DAYS)
        activitiesDaysText.text = createDaysText(ACTIVITY_DAYS)

        val hotelsDaysText = view.findViewById(R.id.hotels_days) as TextView
        val hotelRow = view.findViewById(R.id.hotels_points_row) as TableRow
        hotelRow.contentDescription = createAccessibilityText(R.string.pending_points_hotels_accessibility_TEMPLATE, HOTEL_DAYS)
        hotelsDaysText.text = createDaysText(HOTEL_DAYS)

        val cruisesDaysText = view.findViewById(R.id.cruises_days) as TextView
        val cruisesRow = view.findViewById(R.id.cruises_points_row) as TableRow
        cruisesRow.contentDescription = createAccessibilityText(R.string.pending_points_cruises_accessibility_TEMPLATE, CRUISE_DAYS)
        cruisesDaysText.text = createDaysText(CRUISE_DAYS)

        val carsDaysText = view.findViewById(R.id.cars_days) as TextView
        val carsRow = view.findViewById(R.id.cars_points_row) as TableRow
        carsRow.contentDescription = createAccessibilityText(R.string.pending_points_cars_accessibility_TEMPLATE, CAR_DAYS)
        carsDaysText.text = createDaysText(CAR_DAYS)

        alertDialogBuilder.setNegativeButton(resources.getString(R.string.ok), { dialog, which ->
            dialog?.dismiss()
        })

        return alertDialogBuilder.create()
    }

    fun createAccessibilityText(lobStringId: Int, days: Int): String {
        return Phrase.from(activity, lobStringId)
                .put("number_of_days", days.toString()).format().toString()
    }

    fun createDaysText(days: Int): String {
        return Phrase.from(activity, R.string.number_of_days_points_TEMPLATE)
                .put("number_of_days", days.toString()).format().toString()
    }

    companion object {
        @JvmStatic
        fun newInstance(title: String): PendingPointsDialogFragment {
            val frag = PendingPointsDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args

            return frag
        }

        const val FLIGHT_DAYS: Int = 30
        const val BUNDLE_DAYS: Int = 30
        const val ACTIVITY_DAYS: Int = 30
        const val HOTEL_DAYS: Int = 35
        const val CRUISE_DAYS: Int = 45
        const val CAR_DAYS: Int = 90
    }
}
