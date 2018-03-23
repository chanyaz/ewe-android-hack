package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.presenter.TravelersActivity;
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter;
import com.expedia.bookings.presenter.flight.FlightInboundPresenter;
import com.expedia.bookings.presenter.flight.FlightOutboundPresenter;
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter;
import com.expedia.bookings.presenter.flight.FlightPresenter;
import com.expedia.bookings.presenter.flight.FlightSearchPresenter;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.KrazyglueServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.vm.FlightCheckoutViewModel;
import com.expedia.vm.flights.BaggageInfoViewModel;
import com.expedia.vm.flights.FlightCreateTripViewModel;
import com.expedia.vm.flights.RecentSearchViewModel;

import dagger.Component;

public interface TravelerActivityComponent {
	void inject(TravelersActivity travelersActivity);
}
