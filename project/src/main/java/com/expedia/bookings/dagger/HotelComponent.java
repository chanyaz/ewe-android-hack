package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.presenter.hotel.HotelPresenter;
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Component;

@HotelScope
@Component(dependencies = {AppComponent.class}, modules = {HotelModule.class})
public interface HotelComponent {
	void inject(HotelResultsPresenter presenter);
	void inject(HotelPresenter presenter);

	SuggestionV4Services suggestionsService();
}
