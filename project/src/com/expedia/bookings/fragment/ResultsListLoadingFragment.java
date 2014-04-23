package com.expedia.bookings.fragment;

import java.util.concurrent.CopyOnWriteArrayList;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

/**
 * Results loading fragment for Tablet
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsListLoadingFragment extends Fragment {

	private final static String STATE_LOADING_TEXT = "STATE_LOADING_TEXT";
	private final static String STATE_ALONE_GRAVITY = "STATE_ALONE_GRAVITY";
	private final static String STATE_WITH_OTHERS_GRAVITY = "STATE_WITH_OTHERS_GRAVITY";

	private LinearLayout mRootC;
	private TextView mLoadingTv;

	//loading anim vars
	private int mLoadingColorDark = Color.DKGRAY;
	private int mLoadingColorLight = Color.LTGRAY;
	private ViewGroup mLoadingC;
	private String mLoadingText;
	private int mLastListenerCount = 0;
	private int mLoadingAloneGravity = Gravity.CENTER;
	private int mLoadingWithOthersGravity = Gravity.CENTER;

	public static ResultsListLoadingFragment newInstance(String loadingText, int loadingAloneGravity,
		int loadingWithOthersGravity) {
		ResultsListLoadingFragment frag = new ResultsListLoadingFragment();
		frag.setLoadingText(loadingText);
		frag.setLoadingGravity(loadingAloneGravity, loadingWithOthersGravity);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (LinearLayout) inflater.inflate(R.layout.fragment_results_list_loading, null);
		mLoadingTv = Ui.findView(mRootC, R.id.loading_tv);
		mLoadingC = Ui.findView(mRootC, R.id.loading_bars_container);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_LOADING_TEXT)) {
				setLoadingText(savedInstanceState.getString(STATE_LOADING_TEXT));
			}
			mLoadingAloneGravity = savedInstanceState.getInt(STATE_ALONE_GRAVITY, mLoadingAloneGravity);
			mLoadingWithOthersGravity = savedInstanceState.getInt(STATE_WITH_OTHERS_GRAVITY, mLoadingWithOthersGravity);
		}

		if (mLoadingText != null) {
			setLoadingText(mLoadingText);
		}

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		registerForAnimUpdates(this);
	}

	@Override
	public void onPause() {
		unRegisterForAnimUpdates(this);
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mLoadingText != null) {
			outState.putString(STATE_LOADING_TEXT, mLoadingText);
		}
		outState.putInt(STATE_ALONE_GRAVITY, mLoadingAloneGravity);
		outState.putInt(STATE_WITH_OTHERS_GRAVITY, mLoadingWithOthersGravity);
	}

	public void setLoadingText(String text) {
		mLoadingText = text;
		if (mLoadingTv != null) {
			mLoadingTv.setText(mLoadingText);
		}
	}

	public void setLoadingGravity(int aloneGravity, int withOthersGravity) {
		mLoadingAloneGravity = aloneGravity;
		mLoadingWithOthersGravity = withOthersGravity;
		updateGravities(sLoadingFrags.size() <= 1);
	}


	private void loadingAnimUpdate(int animNumber) {
		if (mLoadingC != null && getActivity() != null && isResumed()) {
			for (int i = 0; i < mLoadingC.getChildCount(); i++) {
				mLoadingC.getChildAt(i)
					.setBackgroundColor(i == animNumber ? mLoadingColorDark : mLoadingColorLight);
			}

			int currentListenerCount = sLoadingFrags.size();
			if (mLastListenerCount != currentListenerCount) {
				updateGravities(currentListenerCount <= 1);
				mLastListenerCount = currentListenerCount;
			}
		}
	}

	private void updateGravities(boolean alone) {
		if (mRootC != null) {
			if (alone) {
				mRootC.setGravity(mLoadingAloneGravity);
			}
			else {
				mRootC.setGravity(mLoadingWithOthersGravity);
			}
		}
	}

	private void setLoadingTextVisible(boolean visible) {
		mLoadingTv.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private int getNumberOfAnimationItems() {
		if (mLoadingC != null) {
			return mLoadingC.getChildCount();
		}
		return 0;
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

	private static final int ANIM_UPDATE_TIME = 250;

	private static final Handler sHandler = new Handler();
	private static int sAnimNumber = 0;
	private static CopyOnWriteArrayList<ResultsListLoadingFragment> sLoadingFrags = new CopyOnWriteArrayList<ResultsListLoadingFragment>();
	private static Runnable sAnimRunner;

	private static void registerForAnimUpdates(ResultsListLoadingFragment frag) {
		synchronized (sLoadingFrags) {
			sAnimNumber = 0;//Reset the ticker if we have a new listener
			if (sLoadingFrags.size() == 0) {
				sLoadingFrags.add(frag);

				//This is animation loop that updates sAnimNumber
				sAnimRunner = new Runnable() {
					@Override
					public void run() {
						if (sAnimRunner != null && sAnimRunner == this && sLoadingFrags.size() > 0) {
							sAnimNumber++;

							//This is the runnable that determines how the animNumber is interpreted
							if (sLoadingFrags.size() == 2) {
								sHandler.post(new UpOneDownTheOtherAnimRunnable());
							}
							else {
								sHandler.post(new SynchronizedAnimRunnable());
							}
							sHandler.postDelayed(this, ANIM_UPDATE_TIME);
						}
					}
				};
				sHandler.postDelayed(sAnimRunner, ANIM_UPDATE_TIME);
			}
			else {
				sLoadingFrags.add(frag);
			}
			updateLoadingTextVisibilities();
		}
	}

	private static void unRegisterForAnimUpdates(ResultsListLoadingFragment frag) {
		synchronized (sLoadingFrags) {
			sLoadingFrags.remove(frag);
			sAnimNumber = 0;//Reset the ticker when we lose a listener
			if (sLoadingFrags.size() == 0) {
				sAnimRunner = null;
			}
			updateLoadingTextVisibilities();
		}
	}

	private static void updateLoadingTextVisibilities() {
		synchronized (sLoadingFrags) {
			boolean vis = (sLoadingFrags.size() == 1);
			for (ResultsListLoadingFragment frag : sLoadingFrags) {
				frag.setLoadingTextVisible(vis);
			}
		}
	}

	private static int getTotalAnimCount() {
		int retVal = 0;
		synchronized (sLoadingFrags) {
			for (ResultsListLoadingFragment frag : sLoadingFrags) {
				retVal += frag.getNumberOfAnimationItems();
			}
		}
		return retVal;
	}

	private static int getMaxIndividualAnimCount() {
		int maxLength = 0;
		synchronized (sLoadingFrags) {
			for (int i = 0; i < sLoadingFrags.size(); i++) {
				if (sLoadingFrags.get(i).getNumberOfAnimationItems() > maxLength) {
					maxLength = sLoadingFrags.get(i).getNumberOfAnimationItems();
				}
			}
		}
		return maxLength;
	}

	//This will make all of the loading bars behave identically (assuming they have the same number of items)
	private static class SynchronizedAnimRunnable implements Runnable {
		@Override
		public void run() {
			int max = getMaxIndividualAnimCount() - 1;
			if (max > 0) {
				boolean reverse = (sAnimNumber / max) % 2 == 1;
				int val = sAnimNumber % max;
				if (reverse) {
					val = max - val;
				}
				for (int i = 0; i < sLoadingFrags.size(); i++) {
					sLoadingFrags.get(i)
						.loadingAnimUpdate(val);
				}
			}
		}
	}

	//This will make the loading bar to be animating one instance at a time, and when it switches between instances it
	//switches between going up and going down.
	private static class UpOneDownTheOtherAnimRunnable implements Runnable {
		@Override
		public void run() {
			int max = getMaxIndividualAnimCount();
			int mod = sAnimNumber % getTotalAnimCount(); //0 - total for all loading frags
			int val = mod % max;

			for (int i = 0; i < sLoadingFrags.size(); i++) {
				ResultsListLoadingFragment frag = sLoadingFrags.get(i);
				int iMin = i * max;
				int iMax = iMin + max;
				if (mod >= iMin && mod < iMax) {
					//Active loading bar
					frag.loadingAnimUpdate(i % 2 == 0 ? val : max - 1 - val);
				}
				else {
					frag.loadingAnimUpdate(-1);
				}
			}
		}
	}

}
