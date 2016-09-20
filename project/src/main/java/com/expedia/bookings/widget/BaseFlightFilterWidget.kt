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
import android.view.ViewStub
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.BaseFlightFilterViewModel
import com.squareup.phrase.Phrase
import java.util.Locale

class BaseFlightFilterWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
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
    val departureStub: ViewStub by bindView(R.id.departure_stub)
    val a11yDepartureStub: ViewStub by bindView(R.id.a11y_departure_stub)
    val a11yDepartureStartSeekBar: FilterSeekBar by bindView(R.id.departure_a11y_start_bar)
    val a11yDepartureStartText: TextView by bindView(R.id.departure_a11y_start_text)
    val a11yDepartureEndSeekBar: FilterSeekBar by bindView(R.id.departure_a11y_end_bar)
    val a11yDepartureEndText: TextView by bindView(R.id.departure_a11y_end_text)

    val arrivalStub: ViewStub by bindView(R.id.arrival_stub)
    val arrivalRangeBar: FilterRangeSeekBar by bindView(R.id.arrival_range_bar)
    val arrivalRangeMinText: TextView by bindView(R.id.arrival_range_min_text)
    val arrivalRangeMaxText: TextView by bindView(R.id.arrival_range_max_text)
    val a11yArrivalStub: ViewStub by bindView(R.id.a11y_arrival_stub)
    val a11yArrivalStartSeekBar: FilterSeekBar by bindView(R.id.arrival_a11y_start_bar)
    val a11yArrivalStartText: TextView by bindView(R.id.arrival_a11y_start_text)
    val a11yArrivalEndSeekBar: FilterSeekBar by bindView(R.id.arrival_a11y_end_bar)
    val a11yArrivalEndText: TextView by bindView(R.id.arrival_a11y_end_text)

    val airlinesLabel: android.widget.TextView by bindView(R.id.airlines_label)
    val airlinesContainer: LinearLayout by bindView(R.id.airlines_container)
    val airlinesMoreLessLabel: TextView by bindView(R.id.show_more_less_text)
    val airlinesMoreLessIcon: ImageView by bindView(R.id.show_more_less_icon)
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

    var viewModelBase: BaseFlightFilterViewModel by notNullAndObservable { vm ->
        doneButton.subscribeOnClick(vm.doneObservable)

        dynamicFeedbackClearButton.setOnClickListener {
            dynamicFeedbackClearButton.announceForAccessibility(context.getString(R.string.filters_cleared))
            vm.clearObservable.onNext(Unit)
        }

        vm.clearChecks.subscribe {
            stopsContainer.clearChecks()
            airlinesContainer.clearChecks()
        }

        vm.newDurationRangeObservable.subscribe { durationRange ->
            durationSeekBar.a11yName = context.getString(R.string.flight_duration_label)
            durationSeekBar.currentA11yValue = String.format(Locale.getDefault(), durationRange.formatHour(durationRange.notches), context.getString(R.string.flight_duration_hour_short))
            durationSeekBar.upperLimit = durationRange.notches
            duration.text = String.format(Locale.getDefault(), durationRange.defaultMaxText, context.getString(R.string.flight_duration_hour_short))

            durationSeekBar.setOnSeekBarChangeListener { seekBar, progress, fromUser ->
                duration.text = String.format(Locale.getDefault(), durationRange.formatHour(progress), context.getString(R.string.flight_duration_hour_short))
                durationSeekBar.currentA11yValue = duration.text.toString()
                announceForAccessibility(durationSeekBar.currentA11yValue)
                vm.durationRangeChangedObserver.onNext(durationRange.update(progress))
            }
        }

        var departureStartCurrentProgress: Int
        var departureEndCurrentProgress: Int

        vm.newDepartureRangeObservable.subscribe { timeRange ->
            if (AccessibilityUtil.isTalkBackEnabled(context)) {
                departureStartCurrentProgress = timeRange.minDurationHours
                departureEndCurrentProgress = timeRange.maxDurationHours
                a11yDepartureStartSeekBar.upperLimit = timeRange.maxDurationHours
                a11yDepartureEndSeekBar.upperLimit = timeRange.maxDurationHours
                a11yDepartureStartText.text = timeRange.defaultMinText
                a11yDepartureEndText.text = timeRange.defaultMaxText
                a11yDepartureStartSeekBar.a11yName = context.getString(R.string.departure_time_range_start)
                a11yDepartureEndSeekBar.a11yName = context.getString(R.string.departure_time_range_end)
                a11yDepartureStartSeekBar.currentA11yValue = a11yDepartureStartText.text.toString()
                a11yDepartureEndSeekBar.currentA11yValue = a11yDepartureEndText.text.toString()

                a11yDepartureStartSeekBar.setOnSeekBarChangeListener { seekBar, progress, fromUser ->
                    departureStartCurrentProgress = timeRange.maxDurationHours - progress
                    if (departureStartCurrentProgress < departureEndCurrentProgress) {
                        a11yDepartureStartText.text = timeRange.formatValue(timeRange.maxDurationHours - progress)
                        a11yDepartureStartSeekBar.currentA11yValue = a11yDepartureStartText.text.toString()
                        announceForAccessibility(a11yDepartureStartSeekBar.currentA11yValue)
                        vm.departureRangeChangedObserver.onNext(timeRange.update(timeRange.maxDurationHours - progress, departureEndCurrentProgress))
                    }
                }

                a11yDepartureEndSeekBar.setOnSeekBarChangeListener { seekBar, progress, fromUser ->
                    departureEndCurrentProgress = progress
                    if (departureEndCurrentProgress > departureStartCurrentProgress) {
                        a11yDepartureEndText.text = timeRange.formatValue(progress)
                        a11yDepartureEndSeekBar.currentA11yValue = a11yDepartureEndText.text.toString()
                        announceForAccessibility(a11yDepartureEndSeekBar.currentA11yValue)
                        vm.departureRangeChangedObserver.onNext(timeRange.update(departureStartCurrentProgress, progress))
                    }
                }
            }
            else {
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
        }

        var arrivalStartCurrentProgress: Int
        var arrivalEndCurrentProgress: Int

        vm.newArrivalRangeObservable.subscribe { timeRange ->
            if (AccessibilityUtil.isTalkBackEnabled(context)) {
                arrivalStartCurrentProgress = timeRange.minDurationHours
                arrivalEndCurrentProgress = timeRange.maxDurationHours
                a11yArrivalStartSeekBar.upperLimit = timeRange.maxDurationHours
                a11yArrivalEndSeekBar.upperLimit = timeRange.maxDurationHours
                a11yArrivalStartText.text = timeRange.defaultMinText
                a11yArrivalEndText.text = timeRange.defaultMaxText
                a11yArrivalStartSeekBar.a11yName = context.getString(R.string.arrival_time_range_start)
                a11yArrivalEndSeekBar.a11yName = context.getString(R.string.arrival_time_range_end)
                a11yArrivalStartSeekBar.currentA11yValue = a11yArrivalStartText.text.toString()
                a11yArrivalEndSeekBar.currentA11yValue = a11yArrivalEndText.text.toString()

                a11yArrivalStartSeekBar.setOnSeekBarChangeListener { seekBar, progress, fromUser ->
                    arrivalStartCurrentProgress = timeRange.maxDurationHours - progress
                    if (arrivalStartCurrentProgress < arrivalEndCurrentProgress) {
                        a11yArrivalStartText.text = timeRange.formatValue(timeRange.maxDurationHours - progress)
                        a11yArrivalStartSeekBar.currentA11yValue = a11yArrivalStartText.text.toString()
                        announceForAccessibility(a11yArrivalStartSeekBar.currentA11yValue)
                        vm.arrivalRangeChangedObserver.onNext(timeRange.update(timeRange.maxDurationHours - progress, arrivalEndCurrentProgress))
                    }
                }

                a11yArrivalEndSeekBar.setOnSeekBarChangeListener { seekBar, progress, fromUser ->
                    arrivalEndCurrentProgress = progress
                    if (arrivalEndCurrentProgress > arrivalStartCurrentProgress) {
                        a11yArrivalEndText.text = timeRange.formatValue(progress)
                        a11yArrivalEndSeekBar.currentA11yValue = a11yArrivalEndText.text.toString()
                        announceForAccessibility(a11yArrivalEndSeekBar.currentA11yValue)
                        vm.arrivalRangeChangedObserver.onNext(timeRange.update(arrivalStartCurrentProgress, progress))
                    }
                }
            }
            else {
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
            var isFirstLoad: Boolean = true
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val sort = FlightFilter.Sort.values()[position]
                vm.userFilterChoices.userSort = sort
                if (!isFirstLoad) {
                    trackFlightSortBy(sort)
                }
                isFirstLoad = false
                sortByButtonGroup.contentDescription = Phrase.from(context, R.string.filter_sort_by_content_description_TEMPLATE)
                        .put("sort", sort.name).format().toString()
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
                    view.bind(getStopFilterLabel(key.ordinal), key.ordinal, sortedMap[key], vm.selectStop)
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
                    view.bind(key, key, sortedMap[key], vm.selectAirline)
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
                airlinesMoreLessView.contentDescription = resources.getString(R.string.packages_flight_search_filter_show_less_cont_desc)

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
        airlinesMoreLessView.contentDescription = resources.getString(R.string.packages_flight_search_filter_show_more_cont_desc)

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
        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            a11yDepartureStub.inflate()
            a11yArrivalStub.inflate()
        }
        else {
            departureStub.inflate()
            arrivalStub.inflate()
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
        toolbar.setBackgroundColor(ContextCompat.getColor(this.context,R.color.packages_flight_filter_background_color))
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

    fun trackFlightSortBy(sort: FlightFilter.Sort) {
        if (viewModelBase.lob == LineOfBusiness.PACKAGES) {
            PackagesTracking().trackFlightSortBy(sort)
        } else if (viewModelBase.lob == LineOfBusiness.FLIGHTS_V2) {
            FlightsV2Tracking.trackFlightSortBy(sort)
        }
    }

}