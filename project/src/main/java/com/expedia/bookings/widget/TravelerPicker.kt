package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TableRow
import com.expedia.bookings.R
import com.expedia.bookings.utils.StrUtils
import java.util.ArrayList
import kotlin.properties.Delegates

public class TravelerPicker(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val travelerInfoText: TextView by Delegates.lazy {
        findViewById(R.id.num_guests) as TextView
    }

    val adultText: TextView by Delegates.lazy {
        findViewById(R.id.adult) as TextView
    }

    val childText: TextView by Delegates.lazy {
        findViewById(R.id.children) as TextView
    }

    val childrenRow1 : TableRow by Delegates.lazy {
        findViewById(R.id.children_row1) as TableRow
    }

    val childrenRow2 :TableRow by Delegates.lazy {
        findViewById(R.id.children_row2) as TableRow
    }

    val MAX_GUESTS = 6
    val MIN_ADULTS: Int = 1
    val MIN_CHILDREN: Int = 0
    val MAX_CHILDREN = 4
    val DEFAULT_CHILD_AGE = 10

    var numChildren: Int = 0
    var numAdults: Int = 1
    var listener: (String) -> Unit = { _ -> Unit };

    init {
        View.inflate(context, R.layout.widget_traveler_picker, this)
    }

    public fun onUpdate(listener : (String) -> Unit) {
        this.listener = listener
    }

    override fun onFinishInflate() {
        super<LinearLayout>.onFinishInflate()

        var adultPlus: ImageButton = findViewById(R.id.adults_plus) as ImageButton
        var adultMinus: ImageButton = findViewById(R.id.adults_minus) as ImageButton
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

                var spinner = buildChildSpinner(numChildren)

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

    private fun buildChildSpinner(numChildren : Int) : Spinner {
        var layoutInflater: LayoutInflater = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
        var v: View = layoutInflater.inflate(R.layout.child_spinner, null)
        var spinner: Spinner = v.findViewById(R.id.spinner) as Spinner

        var lp: TableRow.LayoutParams = TableRow.LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.hotel_spinner_height))
        lp.weight = 1f

        if (numChildren == 2 || numChildren == 4) {
            var margin : Int = getResources().getDimensionPixelSize(R.dimen.hotel_traveler_table_margin);
            lp.setMargins(margin, 0, 0, 0)
        }

        spinner.setLayoutParams(lp)
        spinner.setAdapter(ChildAgeSpinnerAdapter(getContext()))
        spinner.setSelection(DEFAULT_CHILD_AGE)

        return spinner;
    }

    private fun updateText() {
        travelerInfoText.setText(StrUtils.formatGuests(getContext(), numAdults, numChildren))
        childText.setText(getContext().getResources().getQuantityString(R.plurals.number_of_children, numChildren, numChildren))
        adultText.setText(getContext().getResources().getQuantityString(R.plurals.number_of_adults, numAdults, numAdults))
        listener(StrUtils.formatGuests(getContext(), numAdults, numChildren))
    }

    fun getChildAges() : MutableList<Int> {
        var children : MutableList<Int> = ArrayList<Int>()
        for (i in 0..childrenRow1.getChildCount() -1 ) {
            children.add(getChildAge(i))
        }
        for (i in 0..childrenRow2.getChildCount() -1 ) {
            children.add(getChildAge(i))
        }
        return children
    }

    private fun getChildAge(index: Int): Int {
        var spinner: Spinner = childrenRow1.getChildAt(index) as Spinner
        return spinner.getSelectedItem() as Int
    }
}
