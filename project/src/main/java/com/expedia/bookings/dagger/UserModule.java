package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import android.content.Context;

import com.expedia.bookings.data.user.UserStateManager;

import dagger.Module;
import dagger.Provides;

@Module
public final class UserModule {
	@Provides
	@Singleton
	UserStateManager provideUserStateManager(Context context) {
		return new UserStateManager(context);
	}
}
