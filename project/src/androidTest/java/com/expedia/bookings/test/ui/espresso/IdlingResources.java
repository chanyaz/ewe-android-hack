package com.expedia.bookings.test.ui.espresso;

import java.util.UUID;

import com.expedia.bookings.otto.Events;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.CountingIdlingResource;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

public class IdlingResources {

	public static void registerSuggestionResource(SuggestionResource res) {
		Log.d("Registering idling resource: " + res.getTag());
		Events.register(res);
		Espresso.registerIdlingResources(res.getIdlingResource());
	}

	public static void unregisterSuggestionResource(SuggestionResource res) {
		Log.d("Unregistering idling resource: " + res.getTag());
		// Currently no way to unregister an idling resource from espresso
		// But we can stop it from recieving events
		Events.unregister(res);
	}

	public static class SuggestionResource {
		public String mTag;
		private CountingIdlingResource mIdlingResource;

		public SuggestionResource() {
			mTag = "SuggestionResource_" + UUID.randomUUID().toString();
			mIdlingResource = new CountingIdlingResource(mTag);
		}

		public String getTag() {
			return mTag;
		}

		public CountingIdlingResource getIdlingResource() {
			return mIdlingResource;
		}

		@Subscribe
		public void on(Events.SuggestionQueryStarted event) {
			mIdlingResource.increment();
		}

		@Subscribe
		public void on(Events.SuggestionResultsDelivered event) {
			mIdlingResource.decrement();
		}
	}
}
