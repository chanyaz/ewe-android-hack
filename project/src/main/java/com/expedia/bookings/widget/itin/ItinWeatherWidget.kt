package com.expedia.bookings.widget.itin

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.services.WeatherServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.suggestions.SuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.itin.WeatherForecastViewModel
import com.expedia.vm.itin.WeatherViewModel
import javax.inject.Inject

class ItinWeatherWidget(context: Context, attrs: AttributeSet?): LinearLayout(context, attrs) {

    val currentCondition: TextView by bindView(R.id.current_condition_text_view)
    val forecastListView: RecyclerView by bindView(R.id.forecast_list_view)

    private var forecastAdapterViewModel: WeatherForecastViewModel by notNullAndObservable { vm ->

    }

    lateinit private var forecastAdapter: WeatherForecastListAdapter

    var vm: WeatherViewModel by notNullAndObservable {
        vm.currentWeatherObservable.subscribeText(currentCondition)
        vm.forecastDaysObservable.subscribe { forecastdays ->
            forecastAdapter.updateList(forecastdays)
        }
    }

    lateinit var weatherServices: WeatherServices
        @Inject set

    init {
        Ui.getApplication(getContext()).defaultWeatherComponents()
        Ui.getApplication(getContext()).weatherComponent.inject(this)
        View.inflate(context, R.layout.weather_layout, this)
        vm = WeatherViewModel(getContext(), weatherServices)
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        forecastAdapter = WeatherForecastListAdapter(forecastAdapterViewModel)
        forecastListView.layoutManager = LinearLayoutManager(getContext(), HORIZONTAL, false)
        forecastListView.adapter = forecastAdapter
    }
}
