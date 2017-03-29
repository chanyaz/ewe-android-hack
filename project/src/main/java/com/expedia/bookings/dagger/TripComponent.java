package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TripScope;
import com.expedia.bookings.fragment.ItinCardDetailsPresenter;
import com.expedia.bookings.widget.itin.HotelItinCard;
import com.expedia.bookings.widget.itin.ItinPOSHeader;

import dagger.Component;

@TripScope
@Component(dependencies = {AppComponent.class}, modules = {TripModule.class})
public interface TripComponent {
	void inject(ItinCardDetailsPresenter presenter);
	void inject(HotelItinCard hotelItinCard);
	void inject(ItinPOSHeader itinPOSHeader);
}
