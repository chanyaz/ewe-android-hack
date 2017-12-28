package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TripScope;
import com.expedia.bookings.itin.activity.NewAddGuestItinActivity;
import com.expedia.bookings.presenter.trips.ItinSignInPresenter;
import com.expedia.bookings.presenter.trips.AddGuestItinWidget;
import com.expedia.bookings.itin.ItinPageUsableTracking;
import com.expedia.bookings.services.TripsServicesInterface;
import com.expedia.bookings.widget.itin.ItinPOSHeader;
import com.expedia.vm.itin.ItinSignInViewModel;
import dagger.Component;

@TripScope
@Component(dependencies = {AppComponent.class}, modules = {TripModule.class})
public interface TripComponent {
	void inject(ItinPOSHeader itinPOSHeader);
	void inject(NewAddGuestItinActivity activity);
	void inject(ItinSignInViewModel itinSignInViewModel);
	void inject(ItinSignInPresenter itinSignInPresenter);
	void inject(AddGuestItinWidget addGuestItinWidget);

	ItinPageUsableTracking itinPageUsableTracking();
	TripsServicesInterface tripServices();
}
