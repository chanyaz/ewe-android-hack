package com.expedia.bookings.fragment.base;

import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.expedia.bookings.animation.AnimationListenerAdapter;

/**
 * An internal version of Fragment with some optimizations that any Fragment
 * can appreciate!
 */
public class Fragment extends android.support.v4.app.Fragment {

	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		Animation animation = super.onCreateAnimation(transit, enter, nextAnim);

		// If we're on an OS that supports HW layers, then we should take advantage
		// of it during an animation.
		if (Build.VERSION.SDK_INT >= 11) {
			if (animation == null && nextAnim != 0) {
				animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
			}

			if (animation != null) {
				getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);

				animation.setAnimationListener(new AnimationListenerAdapter() {
					@Override
					public void onAnimationEnd(Animation animation) {
						getView().setLayerType(View.LAYER_TYPE_NONE, null);
					}
				});
			}
		}

		return animation;
	}

}
