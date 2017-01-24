package com.expedia.bookings.widget.itin

import android.content.Context
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
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.itin.WeatherViewModel
import javax.inject.Inject

class ItinWeatherWidget(context: Context, attrs: AttributeSet?): LinearLayout(context, attrs) {

    val currentCondition: TextView by bindView(R.id.current_condition_text_view)
    var vm: WeatherViewModel by notNullAndObservable {
        vm.currentWeatherObservable.subscribeText(currentCondition)
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
    }
}
