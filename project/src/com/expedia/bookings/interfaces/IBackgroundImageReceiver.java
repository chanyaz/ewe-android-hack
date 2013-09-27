package com.expedia.bookings.interfaces;

public interface IBackgroundImageReceiver {
	/**
	 * Tell the listeners we have valid bg images in Db. We tell the listeners the total width/height
	 * incase they are overlays and need to do clipping
	 * 
	 * @param totalRootViewWidth
	 * @param totalRootViewHeight
	 */
	public void bgImageInDbUpdated(int totalRootViewWidth);
}
