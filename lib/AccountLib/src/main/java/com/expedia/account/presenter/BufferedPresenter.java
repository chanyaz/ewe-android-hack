package com.expedia.account.presenter;

import android.content.Context;
import android.util.AttributeSet;

public class BufferedPresenter extends Presenter {

	private int mFlags;
	private Object mData;

	public BufferedPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onTransitionComplete() {
		if (mData != null) {
			super.show(mData, mFlags);
		}
		mData = null;
		mFlags = 0;
	}

	@Override
	public void show(Object state, int flags) {
		if (!isReadyToAnimate) {
			mFlags = flags;
			mData = state;
		}
		super.show(state, flags);
	}
}
