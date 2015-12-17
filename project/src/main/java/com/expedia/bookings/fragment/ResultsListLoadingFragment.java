package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;

/**
 * Results loading fragment for Tablet
 */
public class ResultsListLoadingFragment extends LobableFragment {

	private ViewGroup mRootC;
	private TextView mLoadingTextView;

	//loading anim vars
	private int mLoadingColorDark = Color.DKGRAY;
	private int mLoadingColorLight = Color.LTGRAY;

	private boolean mIsAnimating = false;
	private ArrayList<ValueAnimator> mAnimations = new ArrayList<ValueAnimator>();

	public static ResultsListLoadingFragment newInstance(LineOfBusiness lob) {
		ResultsListLoadingFragment frag = new ResultsListLoadingFragment();
		frag.setLob(lob);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLoadingColorLight = Color.parseColor("#5c6874");
		mLoadingColorDark = Color.parseColor("#485562");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_results_list_loading, null);
		mLoadingTextView = Ui.findView(mRootC, R.id.loading_textview);

		if (getLob() == LineOfBusiness.HOTELS) {
			mLoadingTextView.setText(getString(R.string.loading_hotels));
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			mLoadingTextView.setText(getString(R.string.loading_flights));
		}

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();

		mRootC.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (getActivity() == null || !isResumed()) {
					mRootC.getViewTreeObserver().removeOnPreDrawListener(this);
				}
				if (mRootC != null && mRootC.getWidth() > 0 && isAdded()) {
					//Don't register for animations until we're drawing
					registerForAnimUpdates(ResultsListLoadingFragment.this);
					mRootC.getViewTreeObserver().removeOnPreDrawListener(this);
				}
				return true;
			}
		});
	}

	@Override
	public void onPause() {
		unRegisterForAnimUpdates(this);
		cleanup();
		super.onPause();
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		// Ignore
	}

	private void updateAnimation() {
		if (mRootC != null && getActivity() != null && isResumed() && !mIsAnimating) {
			mIsAnimating = true;
			int found = 0;
			for (int i = 0; i < mRootC.getChildCount(); i++) {
				View child = mRootC.getChildAt(i);
				if (child instanceof FrameLayout) {
					found ++;
					if (found % 2 == 0) {
						animateBackground(child, mLoadingColorDark, mLoadingColorLight);
					}
					else {
						animateBackground(child, mLoadingColorLight, mLoadingColorDark);
					}
				}
			}
		}
	}

	private void animateBackground(final View view, int startColor, int endColor) {
		ValueAnimator animation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
		animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				view.setBackgroundColor((Integer) animator.getAnimatedValue());
			}

		});
		animation.setRepeatMode(ValueAnimator.REVERSE);
		animation.setRepeatCount(ValueAnimator.INFINITE);
		animation.setDuration(350);
		animation.start();
		mAnimations.add(animation);
	}

	private void cleanup() {
		for (ValueAnimator animation : mAnimations) {
			animation.cancel();
		}
		mAnimations.clear();
		mIsAnimating = false;
	}

	/**
	 * BELOW LIES THE STATIC CODE WE USE TO COORDINATE OUR ANIMATIONS BETWEEN INSTANCES OF THIS LIST CLASS
	 * <p/>
	 * The basic way that this works is that we have a single instance of a Runnable loop. We run the loop if any
	 * listeners are registered, otherwise it dies.
	 * <p/>
	 */

	private static final int ANIM_UPDATE_TIME = 350;

	private static final Handler sHandler = new Handler();
	private static CopyOnWriteArrayList<ResultsListLoadingFragment> sLoadingFrags = new CopyOnWriteArrayList<ResultsListLoadingFragment>();
	private static Runnable sAnimRunner = new Runnable() {
		@Override
		public void run() {
			if (sLoadingFrags.size() > 0) {
				for (int i = 0; i < sLoadingFrags.size(); i++) {
					sLoadingFrags.get(i).updateAnimation();
				}
				sHandler.postDelayed(sAnimRunner, ANIM_UPDATE_TIME);
			}
		}
	};

	private static void registerForAnimUpdates(ResultsListLoadingFragment frag) {
		synchronized (sLoadingFrags) {
			if (sLoadingFrags.size() == 0) {
				sLoadingFrags.add(frag);
				sHandler.postDelayed(sAnimRunner, ANIM_UPDATE_TIME);
			}
			else if (!sLoadingFrags.contains(frag)) {
				sLoadingFrags.add(frag);
			}
		}
	}

	private static void unRegisterForAnimUpdates(ResultsListLoadingFragment frag) {
		synchronized (sLoadingFrags) {
			sLoadingFrags.remove(frag);
			if (sLoadingFrags.size() == 0) {
				sHandler.removeCallbacks(sAnimRunner);
			}
		}
	}
}
