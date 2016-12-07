package com.expedia.bookings.dagger;

import java.io.File;

import javax.inject.Singleton;

import android.content.Context;

import com.expedia.bookings.utils.EncryptionUtil;
import com.expedia.bookings.utils.TestEncryptionUtil;

import dagger.Module;
import dagger.Provides;

@Module
public final class TestCryptoModule {
	private static final String SECRET_KEY_FILE_OLD = "secure_key_file_old.dat";
	private static final String SECRET_KEY_FILE = "secure_key_file.dat";
	private static final String COOKIES_FILE = "cookies-6-encrypted.dat";
	private static final String KEYSTORE_ALIAS = "COOKIE_KEYSTORE";

	@Provides
	@Singleton
	EncryptionUtil provideEncryptionUtil(Context context) {
		final File encryptedOld = context.getFileStreamPath(SECRET_KEY_FILE_OLD);
		final File encrypted = context.getFileStreamPath(SECRET_KEY_FILE);
		File cookies = context.getFileStreamPath(COOKIES_FILE);
		encryptedOld.delete();
		encrypted.delete();
		cookies.delete();
		return new TestEncryptionUtil(context, encryptedOld, encrypted, KEYSTORE_ALIAS, false);
	}

}
