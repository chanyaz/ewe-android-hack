package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.presenter.packages.PackageHotelPresenter;
import com.expedia.bookings.presenter.packages.PackagePresenter;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Component;

@HotelScope
@Component(dependencies = {AppComponent.class}, modules = {PackageModule.class})
public interface PackageComponent {
	void inject(PackagePresenter presenter);
	void inject(PackageHotelPresenter presenter);
	PackageServices packageServices();
	SuggestionV4Services suggestionsService();
}
