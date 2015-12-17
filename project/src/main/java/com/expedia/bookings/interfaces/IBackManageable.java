package com.expedia.bookings.interfaces;

import com.expedia.bookings.interfaces.helpers.BackManager;

/**
 * This acts as a go between. Basically there is a bunch of functionality that we don't want to implement in
 * every fragment that wants to be able to act on onBackPressed. This is designed so that a fragment may
 * implement IBackManageable and then have a local instance of BackManager which does all the work.
 */
public interface IBackManageable {
	/**
	 * Get an instance of BackManager that can control onBackPressed actions.
	 * <p/>
	 * PROTIP:Declare one BackManager as a variable in your fragment and return the same instance every time.
	 *
	 * @return
	 */
	BackManager getBackManager();
}
