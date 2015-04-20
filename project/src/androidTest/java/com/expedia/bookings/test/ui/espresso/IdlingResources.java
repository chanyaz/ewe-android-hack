package com.expedia.bookings.test.ui.espresso;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.CountingIdlingResource;

import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

public class IdlingResources {
	public static class Resource {
		public String tag;
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
			return isEditSearchWindowPresent;
		}

		public LxIdlingResource() {
			super(LxIdlingResource.class.getSimpleName());
		}

		@Subscribe
		public void on(Events.LXNewSearchParamsAvailable event) {
			resource.increment();
		}

		@Subscribe
		public void on(Events.LXSearchResultsAvailable event) {
			resource.decrement();
		}

		@Subscribe
		public void on(Events.LXShowSearchError event) {
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
