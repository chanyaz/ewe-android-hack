package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import android.accounts.AccountManager;
import android.content.Context;

import com.expedia.bookings.data.user.TestFileCipher;
import com.expedia.bookings.data.user.UserSource;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.notification.NotificationManager;
import com.expedia.model.UserLoginStateChangedModel;
import dagger.Module;
import dagger.Provides;

@Module
public class TestUserModule {
	@Provides
	@Singleton
	UserStateManager provideUserStateManager(Context context, UserLoginStateChangedModel userLoginStateChangedModel,
		NotificationManager notificationManager) {
		return new UserStateManager(context, userLoginStateChangedModel, notificationManager, AccountManager.get(context), new UserSource(context,
			new TestFileCipher("", "")));
	}
}

