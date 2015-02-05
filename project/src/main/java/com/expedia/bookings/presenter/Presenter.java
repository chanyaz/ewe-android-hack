package com.expedia.bookings.presenter;

import java.util.Stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.ButterKnife.Action;

public class Presenter extends FrameLayout {

	public Presenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			v.setVisibility(View.GONE);
		}
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

	private Stack<View> backstack = new Stack<>();

	public void show(View v) {
		show(v, false);
	}

	public void show(View v, boolean clearBackStack) {
		if (clearBackStack) {
			clearBackStack();
		}
		backstack.push(v);
		for (int i = 0; i < this.getChildCount(); i++) {
			this.getChildAt(i).setVisibility(View.GONE);
		}
		v.setVisibility(View.VISIBLE);
	}

	public void hide(View v) {
		v.setVisibility(View.GONE);
		backstack.remove(v);
	}

	/**
	 * @return true if consumed back press
	 */
	public boolean back() {
		if (backstack.isEmpty()) {
			return false;
		}

		View v = backstack.peek();
		if (v instanceof Presenter) {
			Presenter p = (Presenter) v;
			if (p.back()) {
				return true;
			}
		}
		backstack.pop().setVisibility(View.GONE);

		if (backstack.isEmpty()) {
			// Nothing left to show
			return false;
		}

		show(backstack.pop());
		return true;
	}

	// Utility

	public static final Action<View> SHOW = new Action<View>() {
		@Override
		public void apply(View view, int index) {
			view.setVisibility(View.VISIBLE);
		}
	};

	public static final Action<View> HIDE = new Action<View>() {
		@Override
		public void apply(View view, int index) {
			view.setVisibility(View.GONE);
		}
	};

	// Tread lightly

	public void clearBackStack() {
		while (backstack.size() > 0) {
			View v = backstack.peek();
			if (v instanceof Presenter) {
				Presenter p = (Presenter) v;
				p.clearBackStack();
			}
			backstack.pop().setVisibility(View.GONE);
		}
	}
}
