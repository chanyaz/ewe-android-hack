package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.WeatherScope;
import com.expedia.bookings.widget.itin.ItinWeatherWidget;

import dagger.Component;

@WeatherScope
@Component(dependencies = { AppComponent.class }, modules = { WeatherModule.class })
public interface WeatherComponent {
	void inject(ItinWeatherWidget itinWeatherWidget);
}
