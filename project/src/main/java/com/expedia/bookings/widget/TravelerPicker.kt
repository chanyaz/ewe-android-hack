package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.view.ViewGroup
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.ChildAgeSpinnerAdapter
import com.expedia.bookings.utils.StrUtils
import android.widget.ImageButton
import android.widget.TableRow
import android.view.LayoutInflater
import android.widget.Spinner

public class TravelerPicker(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val MAX_GUESTS = 6
    val MIN_ADULTS: Int = 1
    val MAX_ADULTS = 6
    val MIN_CHILDREN: Int = 0
    val MAX_CHILDREN = 4
    val DEFAULT_CHILD_AGE = 10

    var numChildren: Int = 0
    var numAdults: Int = 1
    var listener: TravelersUpdatedListener? = null;
    var travelerInfoText: TextView? = null
    var adultText: TextView? = null
    var childText: TextView? = null
    init {
        View.inflate(context, R.layout.widget_traveler_picker, this)
    }

    public trait TravelersUpdatedListener {
        fun onTravelerUpdate(text: String)
    }

    public fun setTravelerUpdatedListener(travelersUpdatedListener : TravelersUpdatedListener) {
        listener = travelersUpdatedListener
    }

    override fun onFinishInflate() {
        super<LinearLayout>.onFinishInflate()

        travelerInfoText: TextView = findViewById(R.id.num_guests) as TextView
        adultText: TextView = findViewById(R.id.adult) as TextView
        childText: TextView = findViewById(R.id.children) as TextView
        var adultPlus: ImageButton = findViewById(R.id.adults_plus) as ImageButton
        var adultMinus: ImageButton = findViewById(R.id.adults_minus) as ImageButton
        var childrenRow1: TableRow = findViewById(R.id.children_row1) as TableRow
        var childrenRow2: TableRow = findViewById(R.id.children_row2) as TableRow
        var childPlus: ImageButton = findViewById(R.id.children_plus) as ImageButton
        var childMinus: ImageButton = findViewById(R.id.children_minus) as ImageButton

        updateText()
        
        adultPlus.setOnClickListener {
            if (numAdults + numChildren + 1 <= MAX_GUESTS) {
                numAdults++
                updateText()
            }
        }

        adultMinus.setOnClickListener {
            if (numAdults - 1 >= MIN_ADULTS && numAdults - 1 + numChildren <= MAX_GUESTS) {
                numAdults--
                updateText()
            }
        }

        childPlus.setOnClickListener {
            if (numAdults + numChildren + 1 <= MAX_GUESTS && numChildren + 1 <= MAX_CHILDREN) {
                numChildren++

                var spinner = buildChildSpinner()

                if (numChildren <= 2) {
                    childrenRow1.addView(spinner)
                } else {
                    childrenRow2.addView(spinner)
                }

                updateText()
            }
        }

        childMinus.setOnClickListener {
            if (numChildren - 1 >= MIN_CHILDREN && numChildren - 1 <= MAX_CHILDREN && numAdults + numChildren - 1 <= MAX_GUESTS) {
                numChildren--

                if (childrenRow2.getChildCount() > 0) {
                    var spinner: Spinner = childrenRow2.getChildAt(childrenRow2.getChildCount() - 1) as Spinner
                    childrenRow2.removeView(spinner)
                } else {
                    var spinner: Spinner = childrenRow1.getChildAt(childrenRow1.getChildCount() - 1) as Spinner
                    childrenRow1.removeView(spinner)
                }

                updateText()
            }
        }
    }

    private fun buildChildSpinner() : Spinner {
        var layoutInflater: LayoutInflater = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
        var v: View = layoutInflater.inflate(R.layout.child_spinner, null)
        var spinner: Spinner = v.findViewById(R.id.spinner) as Spinner

        var lp: TableRow.LayoutParams = TableRow.LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.hotel_spinner_height))
        lp.weight = 1f
        spinner.setLayoutParams(lp)
        spinner.setAdapter(ChildAgeSpinnerAdapter(getContext()))
        spinner.setSelection(10)

        return spinner;
    }

    private fun updateText() {
        travelerInfoText?.setText(StrUtils.formatGuests(getContext(), numAdults, numChildren))
        childText?.setText(getContext().getResources().getQuantityString(R.plurals.number_of_children, numChildren, numChildren))
        adultText?.setText(getContext().getResources().getQuantityString(R.plurals.number_of_adults, numAdults, numAdults))
    }
}