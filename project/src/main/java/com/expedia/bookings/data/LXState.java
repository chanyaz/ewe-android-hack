package com.expedia.bookings.data;

import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

public class LXState {
	public LXSearchParams searchParams;

	public LXState() {
		Events.register(this);
	}

	@Subscribe
	public void onLXNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable event) {
		searchParams = event.lxSearchParams;
	}

}
