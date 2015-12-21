package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Component;

@HotelScope
@Component(dependencies = {AppComponent.class}, modules = {PackageModule.class})
public interface PackageComponent {
	SuggestionV4Services suggestionsService();
}
