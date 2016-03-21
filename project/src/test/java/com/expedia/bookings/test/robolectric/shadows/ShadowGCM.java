package com.expedia.bookings.test.robolectric.shadows;

import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.IOException;

@Implements(GoogleCloudMessaging.class)
public class ShadowGCM {
	@Implementation
	public synchronized String register(String... senderIds) throws IOException {
		return "";
	}

	@Implementation
	public void send(String to, String msgId, Bundle data) {
	}
}
