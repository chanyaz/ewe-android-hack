package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TripScope;
import com.expedia.bookings.itin.common.ItinPageUsableTracking;
import com.expedia.bookings.itin.common.NewAddGuestItinActivity;
import com.expedia.bookings.itin.hotel.details.HotelItinDetailsActivity;
import com.expedia.bookings.itin.hotel.pricingRewards.HotelItinPricingAdditionalInfoActivity;
import com.expedia.bookings.itin.hotel.pricingRewards.HotelItinPricingRewardsActivity;
import com.expedia.bookings.itin.lx.details.LxItinDetailsActivity;
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil;
import com.expedia.bookings.presenter.trips.AddGuestItinWidget;
import com.expedia.bookings.presenter.trips.ItinSignInPresenter;
import com.expedia.bookings.services.TripShareUrlShortenServiceInterface;
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
	void inject(LxItinDetailsActivity lxItinDetailsActivity);

	void inject(HotelItinDetailsActivity hotelItinDetailsActivity);
	void inject(HotelItinPricingRewardsActivity hotelItinPricingRewardsActivity);
	void inject(HotelItinPricingAdditionalInfoActivity hotelItinPricingAdditionalInfoActivity);

	ItinPageUsableTracking itinPageUsableTracking();
	TripsServicesInterface tripServices();
	TripShareUrlShortenServiceInterface tripShareUrlShortenService();
	IJsonToItinUtil jsonUtilProvider();
}
