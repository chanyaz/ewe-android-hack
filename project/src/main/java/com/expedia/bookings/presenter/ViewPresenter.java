package com.expedia.bookings.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import butterknife.ButterKnife.Action;

public class ViewPresenter extends FrameLayoutPresenter<View> {

	public ViewPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			v.setVisibility(View.GONE);
		}
	}

	@Override
	public void show(View v) {
		show(v, false);
	}

	public void show(View v, boolean clearBackStack) {
		if (clearBackStack) {
			clearBackStack();
		}
		getBackStack().push(v);
		for (int i = 0; i < this.getChildCount(); i++) {
			this.getChildAt(i).setVisibility(View.GONE);
		}
		v.setVisibility(View.VISIBLE);
	}

	@Override
	public void hide(View v) {
		v.setVisibility(View.GONE);
		getBackStack().remove(v);
	}

	@Override
	public boolean back() {
		if (getBackStack().isEmpty()) {
			return false;
		}

		View v = getBackStack().peek();
		if (v instanceof IPresenter) {
			IPresenter p = (IPresenter) v;
			if (p.back()) {
				return true;
			}
		}
		getBackStack().pop().setVisibility(View.GONE);

		if (getBackStack().isEmpty()) {
			// Nothing left to show
			return false;
		}

		show(getBackStack().pop());
		return true;
	}

	@Override
	public void clearBackStack() {
		while (getBackStack().size() > 0) {
			View v = getBackStack().peek();
			if (v instanceof IPresenter) {
				IPresenter p = (IPresenter) v;
				p.clearBackStack();
			}
			getBackStack().pop().setVisibility(View.GONE);
		}
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
}
