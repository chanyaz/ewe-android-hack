package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter;
import com.expedia.bookings.presenter.flight.FlightInboundPresenter;
import com.expedia.bookings.presenter.flight.FlightOutboundPresenter;
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter;
import com.expedia.bookings.presenter.flight.FlightPresenter;
import com.expedia.bookings.presenter.flight.FlightSearchPresenter;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.widget.ConfirmationToolbar;
import com.expedia.vm.FlightCheckoutViewModel;
import com.expedia.vm.flights.FlightCreateTripViewModel;

import dagger.Component;

@FlightScope
@Component(dependencies = {AppComponent.class}, modules = {FlightModule.class, FeesModule.class, InsuranceModule.class})
public interface FlightComponent {
	void inject(FlightPresenter presenter);
	void inject(FlightSearchPresenter flightSearchPresenter);
	void inject(FlightOutboundPresenter flightOutboundPresenter);
	void inject(FlightInboundPresenter flightInboundPresenter);
	void inject(FlightOverviewPresenter flightOverviewPresenter);
	void inject(FlightCheckoutPresenter flightCheckoutPresenter);
	void inject(FlightCreateTripViewModel createTripViewModel);
	void inject(FlightCheckoutViewModel checkoutViewModel);
	void inject(ConfirmationToolbar confirmationToolbar);

	SuggestionV4Services suggestionsService();
}
