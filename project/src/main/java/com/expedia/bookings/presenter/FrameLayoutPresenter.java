package com.expedia.bookings.presenter;

import java.util.Stack;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.expedia.bookings.otto.Events;

import butterknife.ButterKnife;

public abstract class FrameLayoutPresenter<T> extends FrameLayout implements IPresenter<T> {

	private Stack<T> backstack;

	public FrameLayoutPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		initBackStack();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	// Backstack

	@Override
	public void initBackStack() {
		backstack = new Stack<>();
	}

	@Override
	public Stack<T> getBackStack() {
		return backstack;
	}
}
