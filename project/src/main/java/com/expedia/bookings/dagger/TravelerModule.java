package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TravelerScope;
import com.expedia.bookings.utils.TravelerManager;
import com.expedia.bookings.utils.validation.TravelerValidator;

import dagger.Module;
import dagger.Provides;

@Module
public class TravelerModule {
	@Provides
	@TravelerScope
	TravelerValidator provideTravelerValidator() {
		return new TravelerValidator();
	}

	@Provides
	@TravelerScope
	TravelerManager provideTravelerManager() {
		return new TravelerManager();
	}
}
