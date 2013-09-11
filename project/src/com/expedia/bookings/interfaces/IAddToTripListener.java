package com.expedia.bookings.interfaces;

import android.graphics.Rect;

public interface IAddToTripListener<T> {

	public void beginAddToTrip(T data, Rect globalCoordinates, int shadeColor);

	public void guiElementInPosition();
}
