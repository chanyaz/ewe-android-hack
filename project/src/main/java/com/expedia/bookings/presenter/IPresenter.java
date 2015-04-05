package com.expedia.bookings.presenter;

import java.util.Stack;

public interface IPresenter<T> {

	Stack<T> getBackStack();

	boolean back();

	void clearBackStack();

	void show(T presenter);
}
