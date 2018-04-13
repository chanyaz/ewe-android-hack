package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TravelerScope;
import com.expedia.bookings.utils.TravelerManager;
import com.expedia.bookings.utils.validation.TravelerValidator;
import com.expedia.vm.FlightSearchViewModel;
import com.expedia.bookings.packages.vm.PackageSearchViewModel;
import com.expedia.vm.traveler.BaseTravelerValidatorViewModel;
import com.expedia.vm.traveler.HotelTravelerSummaryViewModel;
import com.expedia.vm.traveler.TravelersViewModel;
import com.expedia.vm.traveler.RailTravelersViewModel;
import com.expedia.vm.traveler.RailTravelerSummaryViewModel;
import com.expedia.vm.traveler.TravelerEmailViewModel;
import com.expedia.vm.traveler.TravelerNameViewModel;
import com.expedia.vm.traveler.TravelerPhoneViewModel;
import com.expedia.vm.traveler.TravelerSelectItemViewModel;
import com.expedia.vm.traveler.TravelerSummaryViewModel;
import com.expedia.vm.traveler.TravelerTSAViewModel;

import dagger.Component;

@TravelerScope
@Component(dependencies = {AppComponent.class}, modules = {TravelerModule.class})
public interface TravelerComponent {
	void inject(PackageSearchViewModel viewModel);
	void inject(BaseTravelerValidatorViewModel viewModel);
	void inject(FlightSearchViewModel viewModel);
	void inject(TravelersViewModel viewModel);
	void inject(RailTravelersViewModel viewModel);
	void inject(RailTravelerSummaryViewModel viewModel);
	void inject(TravelerNameViewModel viewModel);
	void inject(TravelerPhoneViewModel viewModel);
	void inject(TravelerSelectItemViewModel viewModel);
	void inject(TravelerTSAViewModel viewModel);
	void inject(TravelerSummaryViewModel viewModel);
	void inject(TravelerEmailViewModel viewModel);
	void inject(HotelTravelerSummaryViewModel viewModel);

	TravelerManager travelerManager();
	TravelerValidator travelerValidator();
}
