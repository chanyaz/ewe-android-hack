package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Parcelable;
import android.view.ViewGroup;

/**
 * This interface is for classes that want to do something after login. It was developed for itin so we can wait for itins on the login screen.
 * NOTE: This is not a view, and it does not have to contain a gui component (although if it is long running it should display an indicator at the very least)
 */
public interface LoginExtender extends Parcelable {
	/**
	 * The login has finished successfully, now is your time to do some stuff. If we have gui components add them to the extenderContainer
	 * @param fragment - the login fragment
	 * @param extenderContainer - a container set aside for views if the extender has a gui component
	 */
	public void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer);

	/**
	 * We dont want to leak memory so we give you a chance to cleanup when the login fragment thinks cleaning is smart to do.
	 * At the time of this writting this gets called in onPause
	 */
	public void cleanUp();

	/**
	 * Set status message from Extender
	 */
	public void setExtenderStatus(String status);
}
