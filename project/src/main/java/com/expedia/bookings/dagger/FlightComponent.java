package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter;
import com.expedia.bookings.presenter.flight.FlightPresenter;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.vm.FlightCheckoutViewModel;
import com.expedia.vm.flights.FlightCreateTripViewModel;

import dagger.Component;

@FlightScope
@Component(dependencies = {AppComponent.class}, modules = {FlightModule.class, FeesModule.class, InsuranceModule.class})
public interface FlightComponent {
	void inject(FlightPresenter presenter);
	void inject(FlightCheckoutPresenter flightCheckoutPresenter);
	void inject(FlightCreateTripViewModel createTripViewModel);
	void inject(FlightCheckoutViewModel checkoutViewModel);

	SuggestionV4Services suggestionsService();
}
