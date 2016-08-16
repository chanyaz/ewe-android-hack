package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TravelerScope;
import com.expedia.bookings.utils.TravelerManager;
import com.expedia.bookings.utils.validation.TravelerValidator;
import com.expedia.bookings.widget.BaseCheckoutPresenter;
import com.expedia.vm.FlightSearchViewModel;
import com.expedia.vm.packages.PackageSearchViewModel;
import com.expedia.vm.traveler.CheckoutTravelerViewModel;
import com.expedia.vm.traveler.TravelerEmailViewModel;
import com.expedia.vm.traveler.TravelerNameViewModel;
import com.expedia.vm.traveler.TravelerPhoneViewModel;
import com.expedia.vm.traveler.TravelerPickerTravelerViewModel;
import com.expedia.vm.traveler.TravelerSummaryViewModel;
import com.expedia.vm.traveler.TravelerTSAViewModel;

import dagger.Component;

@TravelerScope
@Component(modules = {TravelerModule.class})
public interface TravelerComponent {
	void inject(PackageSearchViewModel viewModel);
	void inject(FlightSearchViewModel viewModel);
	void inject(BaseCheckoutPresenter presenter);
	void inject(CheckoutTravelerViewModel viewModel);
	void inject(TravelerNameViewModel viewModel);
	void inject(TravelerPhoneViewModel viewModel);
	void inject(TravelerPickerTravelerViewModel viewModel);
	void inject(TravelerTSAViewModel viewModel);
	void inject(TravelerSummaryViewModel viewModel);
	void inject(TravelerEmailViewModel viewModel);

	TravelerManager travelerManager();
	TravelerValidator travelerValidator();
}
