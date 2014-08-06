package com.expedia.bookings.fragment;

import java.util.concurrent.CopyOnWriteArrayList;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

/**
 * Results loading fragment for Tablet
 */
public class ResultsListLoadingFragment extends Fragment {

	private ViewGroup mRootC;

	//loading anim vars
	private int mLoadingColorDark = Color.DKGRAY;
	private int mLoadingColorLight = Color.LTGRAY;

	public static ResultsListLoadingFragment newInstance() {
		ResultsListLoadingFragment frag = new ResultsListLoadingFragment();
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
		super.onPause();
	}

	private void updateAnimation(int animNumber) {
		if (mRootC != null && getActivity() != null && isResumed()) {
			for (int i = 0; i < mRootC.getChildCount(); i++) {
				int color = i % 2 == animNumber % 2 ? mLoadingColorDark : mLoadingColorLight;
				mRootC.getChildAt(i).setBackgroundColor(color);
			}
		}
	}

	/**
	 * BELOW LIES THE STATIC CODE WE USE TO COORDINATE OUR ANIMATIONS BETWEEN INSTANCES OF THIS LIST CLASS
	 * <p/>
	 * The basic way that this works is that we have a single instance of a Runnable loop. We run the loop if any
	 * listeners are registered, otherwise it dies. The loop simply increments the sAnimNumber. This basically just
	 * acts as the number of animation ticks.
	 * <p/>
	 * The loop then posts a runnable that handles converting sAnimNumber into something useful for our instances.
	 * This way we can change the behavior of the individual instances, but in a coordinated way.
	 */

	private static final int ANIM_UPDATE_TIME = 350;

	private static final Handler sHandler = new Handler();
	private static CopyOnWriteArrayList<ResultsListLoadingFragment> sLoadingFrags = new CopyOnWriteArrayList<ResultsListLoadingFragment>();
	private static int sAnimNumber = 0;
	private static Runnable sAnimRunner = new Runnable() {
		@Override
		public void run() {
			if (sLoadingFrags.size() > 0) {
				for (int i = 0; i < sLoadingFrags.size(); i++) {
					sLoadingFrags.get(i).updateAnimation(sAnimNumber);
				}
				sAnimNumber++;
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
