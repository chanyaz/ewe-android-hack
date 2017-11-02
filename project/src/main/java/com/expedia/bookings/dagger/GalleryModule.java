package com.expedia.bookings.dagger;

import com.expedia.bookings.hotel.util.HotelGalleryManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GalleryModule {
	@Provides
	@Singleton
	HotelGalleryManager provideHotelGalleryManager() {
		return new HotelGalleryManager();
	}
}
