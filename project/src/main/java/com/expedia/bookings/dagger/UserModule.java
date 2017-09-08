package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import android.content.Context;

import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.model.UserLoginStateChangedModel;

import dagger.Module;
import dagger.Provides;

@Module
public final class UserModule {
	@Provides
	@Singleton
	UserStateManager provideUserStateManager(Context context, UserLoginStateChangedModel userLoginStateChangedModel) {
		return new UserStateManager(context, userLoginStateChangedModel);
	}
}
