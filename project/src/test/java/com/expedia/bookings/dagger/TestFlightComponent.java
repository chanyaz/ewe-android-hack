package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.FlightScope;

import dagger.Component;

@FlightScope
@Component(dependencies = { AppComponent.class }, modules = {
	FlightModule.class, TestFlightSuggestionModule.class, FeesModule.class, InsuranceModule.class })
interface TestFlightComponent extends FlightComponent {
}
