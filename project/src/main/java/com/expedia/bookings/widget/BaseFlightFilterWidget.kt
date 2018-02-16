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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
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

    val sortContainer: LinearLayout by bindView(R.id.sort_flights)
    val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)

    val durationSeekBar: FilterSeekBar by bindView(R.id.duration_seek_bar)
    val duration: TextView by bindView(R.id.duration)

    val stopsLabel: android.widget.TextView by bindView(R.id.stops_label)
    val priceLabelForStops: android.widget.TextView by bindView(R.id.price_label_stops)
    val stopsContainer: LinearLayout by bindView(R.id.stops_container)

    val departureRangeBar: FilterRangeSeekBar by bindView(R.id.departure_range_bar)
    val departureRangeMinText: TextView by bindView(R.id.departure_range_min_text)
    val departureRangeMaxText: TextView by bindView(R.id.departure_range_max_text)

    val arrivalRangeBar: FilterRangeSeekBar by bindView(R.id.arrival_range_bar)
    val arrivalRangeMinText: TextView by bindView(R.id.arrival_range_min_text)
    val arrivalRangeMaxText: TextView by bindView(R.id.arrival_range_max_text)

    val airlinesLabel: android.widget.TextView by bindView(R.id.airlines_label)
    val priceLabelForAirline: android.widget.TextView by bindView(R.id.price_label_airline)
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

        ObservableOld.combineLatest(vm.clearChecks, vm.stopsObservable, { unit, stops -> stops }).subscribe {
            if (it.size > 1) stopsContainer.clearChecks()
            airlinesContainer.clearChecks()
        }

        vm.clearObservable.subscribe {
            sortByButtonGroup.setSelection(0, false)
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
                if (fromUser) {
                    vm.durationFilterInteractionFromUser.onNext(Unit)
                }
            }
        }

        vm.newDepartureRangeObservable.subscribe { timeRange ->
            departureRangeBar.a11yStartName = context.getString(R.string.departure_time_range_start)
            departureRangeBar.a11yEndName = context.getString(R.string.departure_time_range_end)
            departureRangeBar.currentA11yStartValue = timeRange.defaultMinText
            departureRangeBar.currentA11yEndValue = timeRange.defaultMaxText

            departureRangeBar.upperLimit = timeRange.notches
            departureRangeMinText.text = timeRange.defaultMinText
            departureRangeMaxText.text = timeRange.defaultMaxText

            departureRangeBar.setOnRangeSeekBarChangeListener(object : FilterRangeSeekBar.OnRangeSeekBarChangeListener {
                override fun onRangeSeekBarDragChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    departureRangeMinText.text = timeRange.formatValue(minValue)
                    departureRangeMaxText.text = timeRange.formatValue(maxValue)
                    vm.departureRangeChangedObserver.onNext(timeRange.update(minValue, maxValue))
                }

                override fun onRangeSeekBarValuesChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int, thumb: FilterRangeSeekBar.Thumb) {
                    departureRangeMinText.text = timeRange.formatValue(minValue)
                    departureRangeMaxText.text = timeRange.formatValue(maxValue)
                    departureRangeBar.currentA11yStartValue = departureRangeMinText.text.toString()
                    departureRangeBar.currentA11yEndValue = departureRangeMaxText.text.toString()
                    announceForAccessibility(departureRangeBar.getAccessibilityText(thumb))
                    vm.departureRangeChangedObserver.onNext(timeRange.update(minValue, maxValue))
                }
            })
        }

        vm.newArrivalRangeObservable.subscribe { timeRange ->
            arrivalRangeBar.a11yStartName = context.getString(R.string.arrival_time_range_start)
            arrivalRangeBar.a11yEndName = context.getString(R.string.arrival_time_range_end)
            arrivalRangeBar.currentA11yStartValue = timeRange.defaultMinText
            arrivalRangeBar.currentA11yEndValue = timeRange.defaultMaxText

            arrivalRangeBar.upperLimit = timeRange.notches
            arrivalRangeMinText.text = timeRange.defaultMinText
            arrivalRangeMaxText.text = timeRange.defaultMaxText

            arrivalRangeBar.setOnRangeSeekBarChangeListener(object : FilterRangeSeekBar.OnRangeSeekBarChangeListener {
                override fun onRangeSeekBarDragChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    arrivalRangeMinText.text = timeRange.formatValue(minValue)
                    arrivalRangeMaxText.text = timeRange.formatValue(maxValue)
                    vm.arrivalRangeChangedObserver.onNext(timeRange.update(minValue, maxValue))
                }

                override fun onRangeSeekBarValuesChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int, thumb: FilterRangeSeekBar.Thumb) {
                    arrivalRangeMinText.text = timeRange.formatValue(minValue)
                    arrivalRangeMaxText.text = timeRange.formatValue(maxValue)
                    arrivalRangeBar.currentA11yStartValue = arrivalRangeMinText.text.toString()
                    arrivalRangeBar.currentA11yEndValue = arrivalRangeMaxText.text.toString()
                    announceForAccessibility(arrivalRangeBar.getAccessibilityText(thumb))
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
                sortByButtonGroup.setAccessibilityHoverFocus()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        vm.stopsObservable.subscribe { sortedMap ->
            stopsContainer.removeAllViews()
            if (sortedMap != null && !sortedMap.isEmpty()) {
                stopsLabel.visibility = VISIBLE
                for (key in sortedMap.keys) {
                    val view: RelativeLayout
                    if (vm.shouldShowShowPriceAndLogoOnFilter) {
                        view = Ui.inflate<LabeledCheckableFilterWithPriceAndLogo<Int>>(LayoutInflater.from(context), R.layout.labeled_checked_filter_with_logo_and_price, this, false)
                        if (sortedMap.size == 1) {
                            view.bind(getStopFilterLabel(key.ordinal), key.ordinal, sortedMap[key])
                        } else {
                            view.bind(getStopFilterLabel(key.ordinal), key.ordinal, sortedMap[key], false, vm.selectStop)
                        }
                    } else {
                        view = Ui.inflate<LabeledCheckableFilter<Int>>(LayoutInflater.from(context), R.layout.labeled_checked_filter, this, false)
                        if (sortedMap.size == 1) {
                            view.bind(getStopFilterLabel(key.ordinal), key.ordinal, sortedMap[key]?.count)
                        } else {

                            view.bind(getStopFilterLabel(key.ordinal), key.ordinal, sortedMap[key]?.count, vm.selectStop)
                        }
                    }
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
                    val view: RelativeLayout
                    if (vm.shouldShowShowPriceAndLogoOnFilter) {
                        view = Ui.inflate<LabeledCheckableFilterWithPriceAndLogo<String>>(LayoutInflater.from(context), R.layout.labeled_checked_filter_with_logo_and_price, this, false)
                        view.bind(key, key, sortedMap[key], true, vm.selectAirline)
                    } else {
                        view = Ui.inflate<LabeledCheckableFilter<String>>(LayoutInflater.from(context), R.layout.labeled_checked_filter, this, false)
                        view.bind(key, key, sortedMap[key]?.count, vm.selectAirline)
                    }
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

        if (vm.shouldShowShowPriceAndLogoOnFilter) {
            priceLabelForStops.visibility = View.VISIBLE
            priceLabelForAirline.visibility = View.VISIBLE
        } else {
            priceLabelForStops.visibility = View.GONE
            priceLabelForAirline.visibility = View.GONE
        }
    }

    private fun getStopFilterLabel(numOfStops: Int): String {
        when (numOfStops) {
            0 -> {
                return if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.ITALY) {
                    resources.getString(R.string.flight_direct_description)
                } else {
                    resources.getString(R.string.flight_nonstop_description)
                }
            }
            1 -> return resources.getString(R.string.flight_one_stop_description)
            else -> {
                return resources.getString(R.string.flight_two_plus_stops_description)
            }
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
            val lp = filterContainer.layoutParams as android.widget.FrameLayout.LayoutParams
            lp.topMargin = lp.topMargin + statusBarHeight
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_package_flight_filter, this)
        dynamicFeedbackWidget.hideDynamicFeedback()
        toolbar.inflateMenu(R.menu.action_mode_done)
        toolbar.title = resources.getString(R.string.sort_and_filter)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.actionbar_text_color_inverse))
        toolbar.menu.findItem(R.id.menu_done).setActionView(doneButton).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        toolbar.setBackgroundColor(ContextCompat.getColor(this.context, R.color.packages_flight_filter_background_color))
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
