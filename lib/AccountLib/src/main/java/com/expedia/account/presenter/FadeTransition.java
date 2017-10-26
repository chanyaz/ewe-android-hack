package com.expedia.account.presenter;

import android.support.annotation.Nullable;
import android.view.View;

public class FadeTransition extends Presenter.Transition {

	private View vOut;
	private View vIn;

	public FadeTransition(@Nullable View out, @Nullable View in) {
		vOut = out;
		vIn = in;
	}

	@Override
	public void startTransition(boolean forward) {
		if (vOut != null) {
			vOut.setVisibility(View.VISIBLE);
			vOut.setAlpha(forward ? 1 : 0);
		}
		if (vIn != null) {
			vIn.setVisibility(View.VISIBLE);
			vIn.setAlpha(forward ? 0 : 1);
		}


	}

	@Override
	public void updateTransition(float f, boolean forward) {
		if (vOut != null) {
			vOut.setAlpha(forward ? 1 - f : f);
		}
		if (vIn != null) {
			vIn.setAlpha(forward ? f : 1 - f);
		}
	}

	@Override
	public void finalizeTransition(boolean forward) {
		if (vOut != null) {
			vOut.setAlpha(forward ? 0 : 1);
			vOut.setVisibility(forward ? View.GONE : View.VISIBLE);
		}
		if (vIn != null) {
			vIn.setAlpha(forward ? 1 : 0);
			vIn.setVisibility(forward ? View.VISIBLE : View.GONE);
		}
	}
}
