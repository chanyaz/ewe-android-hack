package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TripScope;
import com.expedia.bookings.fragment.ItinCardDetailsPresenter;
import com.expedia.vm.itin.AddGuestItinViewModel;

import dagger.Component;

@TripScope
@Component(dependencies = {AppComponent.class}, modules = {TripModule.class})
public interface TripComponent {
	void inject(ItinCardDetailsPresenter presenter);
	void inject(AddGuestItinViewModel addGuestItinViewModel);
}
