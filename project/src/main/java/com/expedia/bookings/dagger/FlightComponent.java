package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.presenter.flight.FlightPresenter;
import com.expedia.bookings.services.FlightServices;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Component;

@FlightScope
@Component(dependencies = {AppComponent.class}, modules = {FlightModule.class})
public interface FlightComponent {
	void inject(FlightPresenter presenter);
	FlightServices flightServices();
	SuggestionV4Services suggestionsService();
}
