package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.packages.PackageFlightFilterViewModel

class PackageFlightFilterWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val ANIMATION_DURATION = 500L
    val rowHeight = resources.getDimensionPixelSize(R.dimen.airlines_filter_height)

    val toolbar: Toolbar by bindView(R.id.filters_toolbar)
    val toolbarDropshadow: View by bindView(R.id.filters_toolbar_dropshadow)

    val sortContainer: LinearLayout by bindView(R.id.sort_hotel)
    val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)

    val durationSeekBar: FilterSeekBar by bindView(R.id.duration_seek_bar)
    val duration: TextView by bindView(R.id.duration)

    val stopsLabel: android.widget.TextView by bindView(R.id.stops_label)
    val stopsContainer: LinearLayout by bindView(R.id.stops_container)

    val departureRangeBar: FilterRangeSeekBar by bindView(R.id.departure_range_bar)
    val departureRangeMinText: TextView by bindView(R.id.departure_range_min_text)
    val departureRangeMaxText: TextView by bindView(R.id.departure_range_max_text)

    val arrivalRangeBar: FilterRangeSeekBar by bindView(R.id.arrival_range_bar)
    val arrivalRangeMinText: TextView by bindView(R.id.arrival_range_min_text)
    val arrivalRangeMaxText: TextView by bindView(R.id.arrival_range_max_text)

    val airlinesLabel: android.widget.TextView by bindView(R.id.airlines_label)
    val airlinesContainer: LinearLayout by bindView(R.id.airlines_container)
    val airlinesMoreLessLabel: TextView by bindView(R.id.show_more_less_text)
    val airlinesMoreLessIcon: ImageButton by bindView(R.id.show_more_less_icon)
    val airlinesMoreLessView: RelativeLayout by bindView(R.id.collapsed_container)

    val dynamicFeedbackWidget: DynamicFeedbackWidget by bindView(R.id.dynamic_feedback_container)
    val dynamicFeedbackClearButton: TextView by bindView(R.id.dynamic_feedback_clear_button)
    val filterContainer: ViewGroup by bindView(R.id.filter_container)
    val doneButton: Button by lazy {
        val button = LayoutInflater.from(context).inflate(R.layout.toolbar_checkmark_item, null) as Button
        button.setTextColor(ContextCompat.getColor(context, R.color.packages_flight_filter_text))
        button.setText(R.string.done)

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, R.color.packages_flight_filter_text), PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        button
    }

    var viewModel: PackageFlightFilterViewModel by notNullAndObservable { vm ->
        doneButton.subscribeOnClick(vm.doneObservable)

        dynamicFeedbackClearButton.setOnClickListener {
            vm.clearObservable.onNext(Unit)
        }

        vm.clearChecks.subscribe {
            stopsContainer.clearChecks()
            airlinesContainer.clearChecks()
        }


        vm.newDurationRangeObservable.subscribe { durationRange ->
            durationSeekBar.upperLimit = durationRange.notches
            duration.text = java.lang.String.format(durationRange.defaultMaxText, context.getString(R.string.flight_duration_hour_short))

            durationSeekBar.setOnSeekBarChangeListener(object : FilterSeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: FilterSeekBar, progress: Int, fromUser: Boolean) {
                    duration.text = java.lang.String.format(durationRange.formatHour(progress), context.getString(R.string.flight_duration_hour_short))
                    vm.durationRangeChangedObserver.onNext(durationRange.update(progress))
                }

                override fun onStartTrackingTouch(seekBar: FilterSeekBar) {}
                override fun onStopTrackingTouch(seekBar: FilterSeekBar) {}
            })
        }

        vm.newDepartureRangeObservable.subscribe { timeRange ->
            departureRangeBar.upperLimit = timeRange.notches
            departureRangeMinText.text = timeRange.defaultMinText
            departureRangeMaxText.text = timeRange.defaultMaxText

            departureRangeBar.setOnRangeSeekBarChangeListener(object : FilterRangeSeekBar.OnRangeSeekBarChangeListener {
                override fun onRangeSeekBarDragChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    departureRangeMinText.text = timeRange.formatValue(minValue)
                    departureRangeMaxText.text = timeRange.formatValue(maxValue)
                    vm.departureRangeChangedObserver.onNext(timeRange.update(minValue, maxValue))
                }

                override fun onRangeSeekBarValuesChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    departureRangeMinText.text = timeRange.formatValue(minValue)
                    departureRangeMaxText.text = timeRange.formatValue(maxValue)
                    vm.departureRangeChangedObserver.onNext(timeRange.update(minValue, maxValue))
                }
            })
        }

        vm.newArrivalRangeObservable.subscribe { timeRange ->
            arrivalRangeBar.upperLimit = timeRange.notches
            arrivalRangeMinText.text = timeRange.defaultMinText
            arrivalRangeMaxText.text = timeRange.defaultMaxText

            arrivalRangeBar.setOnRangeSeekBarChangeListener(object : FilterRangeSeekBar.OnRangeSeekBarChangeListener {
                override fun onRangeSeekBarDragChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    arrivalRangeMinText.text = timeRange.formatValue(minValue)
                    arrivalRangeMaxText.text = timeRange.formatValue(maxValue)
                    vm.arrivalRangeChangedObserver.onNext(timeRange.update(minValue, maxValue))
                }

                override fun onRangeSeekBarValuesChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    arrivalRangeMinText.text = timeRange.formatValue(minValue)
                    arrivalRangeMaxText.text = timeRange.formatValue(maxValue)
                    vm.arrivalRangeChangedObserver.onNext(timeRange.update(minValue, maxValue))
                }
            })
        }

        vm.doneButtonEnableObservable.subscribe { enable ->
            doneButton.alpha = (if (enable) 1.0f else (0.15f))
        }

        vm.updateDynamicFeedbackWidget.subscribe {
            if (it < 0) {
                dynamicFeedbackWidget.hideDynamicFeedback()
            } else {
                dynamicFeedbackWidget.showDynamicFeedback()
                dynamicFeedbackWidget.setDynamicCounterText(it)
            }
        }

        vm.filteredZeroResultObservable.subscribe {
            dynamicFeedbackWidget.animateDynamicFeedbackWidget()
        }

        sortByButtonGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val sort = FlightFilter.Sort.values()[position]
                vm.userFilterChoices.userSort = sort
                PackagesTracking().trackFlightSortBy(sort)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        vm.stopsObservable.subscribe { sortedMap ->
            stopsContainer.removeAllViews()
            if (sortedMap != null && !sortedMap.isEmpty()) {
                stopsLabel.visibility = VISIBLE
                for (key in sortedMap.keys) {
                    val view = Ui.inflate<LabeledCheckableFilter<Int>>(LayoutInflater.from(context), R.layout.labeled_checked_filter, this, false)
                    view.bind(getStopFilterLabel(key.ordinal), key.ordinal, sortedMap.get(key), vm.selectStop)
                    view.subscribeOnClick(view.checkObserver)
                    stopsContainer.addView(view)
                }
            } else {
                stopsLabel.visibility = GONE
            }
        }

        vm.airlinesObservable.subscribe { sortedMap ->
            airlinesContainer.removeAllViews()
            if (sortedMap != null && !sortedMap.isEmpty()) {
                airlinesLabel.visibility = VISIBLE
                if (sortedMap.size > 4) {
                    airlinesMoreLessView.visibility = VISIBLE
                }
                for (key in sortedMap.keys) {
                    val view = Ui.inflate<LabeledCheckableFilter<String>>(LayoutInflater.from(context), R.layout.labeled_checked_filter, this, false)
                    view.bind(key, key, sortedMap.get(key), vm.selectAirline)
                    view.subscribeOnClick(view.checkObserver)
                    airlinesContainer.addView(view)
                }
                setupAirlinesView()
            } else {
                airlinesLabel.visibility = GONE
                airlinesMoreLessView.visibility = GONE
            }
        }

        airlinesMoreLessView.subscribeOnClick(vm.airlinesMoreLessObservable)

        vm.airlinesExpandObservable.subscribe { isSectionExpanded ->
            if (isSectionExpanded) {
                AnimUtils.rotate(airlinesMoreLessIcon)
                airlinesMoreLessLabel.text = resources.getString(R.string.show_less)

                for (i in 3..airlinesContainer.childCount - 1) {
                    val v = airlinesContainer.getChildAt(i)
                    if (v is CheckBoxFilterWidget) {
                        v.visibility = View.VISIBLE
                    }
                }

                val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
                resizeAnimator.addViewSpec(airlinesContainer, rowHeight * airlinesContainer.childCount)
                resizeAnimator.start()

            } else {
                setupAirlinesView()
            }
        }

        vm.sortContainerObservable.subscribe { showSort ->
            sortContainer.visibility = if (showSort) View.VISIBLE else View.GONE
        }
    }

    private fun getStopFilterLabel(numOfStops: Int): String {
        if (numOfStops == 0) {
            return resources.getString(R.string.flight_nonstop_description)
        } else {
            return resources.getQuantityString(R.plurals.flight_filter_stops, numOfStops)
        }
    }

    private fun setupAirlinesView() {
        AnimUtils.reverseRotate(airlinesMoreLessIcon)
        airlinesMoreLessLabel.text = resources.getString(R.string.show_more)

        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        resizeAnimator.addViewSpec(airlinesContainer, rowHeight * 3)
        resizeAnimator.start()

        for (i in 3..airlinesContainer.childCount - 1) {
            val v = airlinesContainer.getChildAt(i)
            if (v is CheckBoxFilterWidget) {
                v.visibility = View.GONE
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            var lp = filterContainer.layoutParams as android.widget.FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_package_flight_filter, this)
        dynamicFeedbackWidget.hideDynamicFeedback()
        toolbar.inflateMenu(R.menu.action_mode_done)
        toolbar.title = resources.getString(R.string.sort_and_filter)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.cars_actionbar_text_color))
        toolbar.menu.findItem(R.id.menu_done).setActionView(doneButton).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        val adapter = ArrayAdapter(getContext(), R.layout.spinner_sort_item, resources.getStringArray(R.array.sort_options_flights).toMutableList())
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        sortByButtonGroup.adapter = adapter

        filterContainer.viewTreeObserver.addOnScrollChangedListener({
            val scrollY = filterContainer.scrollY
            val ratio = (scrollY).toFloat() / 100
            toolbarDropshadow.alpha = ratio
        })
    }

    fun LinearLayout.clearChecks() {
        for (i in 0..childCount - 1) {
            val v = getChildAt(i)
            if (v is LabeledCheckableFilter<*> && v.checkBox.isChecked) {
                v.checkBox.isChecked = false
            }
        }
    }
}