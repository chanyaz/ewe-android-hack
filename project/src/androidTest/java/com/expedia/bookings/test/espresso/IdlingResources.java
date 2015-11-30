package com.expedia.bookings.test.espresso;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.CountingIdlingResource;

import com.expedia.bookings.otto.Events;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

public class IdlingResources {
	public static class Resource {
		protected CountingIdlingResource resource;

		public Resource(String tag) {
			resource = new CountingIdlingResource(tag);
		}

		public void register() {
			Espresso.registerIdlingResources(resource);
			Events.register(this);
		}

		public void unregister() {
			Events.unregister(this);
			Espresso.unregisterIdlingResources(resource);
		}

	}

	public static class LxIdlingResource extends Resource {
		private boolean isEditSearchWindowPresent = false;

		public boolean isInSearchEditMode() {
			Common.delay(1);
			return isEditSearchWindowPresent;
		}

		public LxIdlingResource() {
			super(LxIdlingResource.class.getSimpleName());
		}

		@Subscribe
		public void on(Events.LXNewSearchParamsAvailable event) {
			Log.d("LxIdlingResource - Events.LXNewSearchParamsAvailable");
			resource.increment();
		}

		@Subscribe
		public void on(Events.LXSearchResultsAvailable event) {
			Log.d("LxIdlingResource - Events.LXSearchResultsAvailable");
			resource.decrement();
		}

		@Subscribe
		public void on(Events.LXShowSearchError event) {
			Log.v("LxIdlingResource - Events.LXShowSearchError");
			//LXNewSearchParamsAvailable can be terminated with LXSearchResultsAvailable or LXShowSearchError
			//Though LXShowSearchError can be broadcast without LXNewSearchParamsAvailable
			//This takes care of both the scenarios for our purposes
			if (!resource.isIdleNow()) {
				resource.decrement();
			}
			isEditSearchWindowPresent = true;
		}
	}

	public static class SuggestionResource extends Resource {
		public SuggestionResource() {
			super(SuggestionResource.class.getSimpleName());
		}

		@Subscribe
		public void on(Events.SuggestionQueryStarted event) {
			resource.increment();
		}

		@Subscribe
		public void on(Events.SuggestionResultsDelivered event) {
			resource.decrement();
		}
	}
}
