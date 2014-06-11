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
import android.view.ViewTreeObserver;
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

	private ViewGroup mRootC;
	private LinearLayout mContentC;
	private TextView mLoadingTv;
	private ViewGroup mLoadingC;
	private View mBgView;

	//loading anim vars
	private int mLoadingColorDark = Color.DKGRAY;
	private int mLoadingColorLight = Color.LTGRAY;
	private String mLoadingText;
	private int mLastGravity = -1;
	private int mLastListenerCount = 0;
	private int mLoadingAloneGravity = Gravity.CENTER;
	private int mLoadingWithOthersGravity = Gravity.CENTER;

	//grow animation variables
	private float mStartTranslationY;
	private float mEndTranslationY;
	private float mStartTranslationX;
	private float mEndTranslationX;
	private float mEndScaleY;
	private float mEndScaleX;

	public static ResultsListLoadingFragment newInstance(String loadingText, int loadingAloneGravity,
		int loadingWithOthersGravity) {
		ResultsListLoadingFragment frag = new ResultsListLoadingFragment();
		frag.setLoadingText(loadingText);
		frag.setLoadingGravity(loadingAloneGravity, loadingWithOthersGravity);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_results_list_loading, null);
		mContentC = Ui.findView(mRootC, R.id.content_container);
		mLoadingTv = Ui.findView(mRootC, R.id.loading_tv);
		mLoadingC = Ui.findView(mRootC, R.id.loading_bars_container);
		mBgView = Ui.findView(mRootC, R.id.loading_bg_view);

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

		updateGravities(false, false);

		//Add layout listener to update view positions if the layout changes
		mRootC.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
				int oldTop,
				int oldRight,
				int oldBottom) {
				//If our width changes, update accordingly
				if (right - left != oldRight - oldLeft) {
					setLoadingGravity(mLoadingAloneGravity, mLoadingWithOthersGravity);
				}
			}
		});

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
				if (mLoadingC != null && mLoadingC.getWidth() > 0 && isAdded()) {
					//Update our positioning before we draw
					setLoadingGravity(mLoadingAloneGravity, mLoadingWithOthersGravity);

					//Don't register for animations until we're drawing
					registerForAnimUpdates(ResultsListLoadingFragment.this);

					//Remove predraw listener
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mLoadingText != null) {
			outState.putString(STATE_LOADING_TEXT, mLoadingText);
		}
		outState.putInt(STATE_ALONE_GRAVITY, mLoadingAloneGravity);
		outState.putInt(STATE_WITH_OTHERS_GRAVITY, mLoadingWithOthersGravity);
	}


	public void initGrowToRowsAnimation() {
		//We don't want the loading animation to be happening while we are growing our rows.
		unRegisterForAnimUpdates(this);

		//Set all cells to the light color
		for (int i = 0; i < mLoadingC.getChildCount(); i++) {
			mLoadingC.getChildAt(i).setBackgroundColor(mLoadingColorLight);
		}

		//Get our dimens and figure out where these things are going
		//Based on what we know about the row heights, lets set up our animation.
		int listMarginTop = getResources().getDimensionPixelSize(R.dimen.results_list_margin_top);
		int dividerHeight = getResources().getDimensionPixelSize(R.dimen.results_list_spacer_height);
		int yPaddding = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_padding_y);
		int rowHeight = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_height_with_padding);
		int xPadding = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_padding_x);

		int animToWidth = mRootC.getWidth() - 2 * xPadding;
		int animToHeight = 2 * dividerHeight + 3 * rowHeight;

		//The final values of the animation
		mEndScaleY = animToHeight / mLoadingC.getHeight();
		mEndScaleX = animToWidth / mLoadingC.getWidth();
		mStartTranslationY = mLoadingC.getTranslationY();
		mEndTranslationY = (int) ((listMarginTop + dividerHeight + 2 * yPaddding) - mLoadingC.getY());
		mStartTranslationX = mLoadingC.getTranslationX();
		mEndTranslationX = xPadding;

		//pivots
		mLoadingC.setPivotY(0);
		mLoadingC.setPivotX(0);

		//Set up our hardware layers
		mLoadingC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mBgView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mLoadingTv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}

	public void setGrowToRowsAnimPercentage(float percentage) {
		//The text fades out
		mLoadingTv.setTranslationY(percentage * mRootC.getHeight());
		mLoadingTv.setAlpha(1f - percentage);

		//The remainder of the animation is split into phases, this indicates where we switch between them
		float animationSplit = 0.65f;

		//Phase 1) Move and scale things into place
		if (percentage < animationSplit) {
			float firstHalfPercentage = percentage / animationSplit;
			mLoadingC
				.setTranslationX(mStartTranslationX + firstHalfPercentage * (mEndTranslationX - mStartTranslationX));
			mLoadingC
				.setTranslationY(mStartTranslationY + firstHalfPercentage * (mEndTranslationY - mStartTranslationY));
			mLoadingC.setScaleX(1f + firstHalfPercentage * (mEndScaleX - 1f));
			mLoadingC.setScaleY(1f + firstHalfPercentage * (mEndScaleY - 1f));
		}
		else {
			mLoadingC.setTranslationY(mEndTranslationY);
			mLoadingC.setTranslationX(mEndTranslationX);
			mLoadingC.setScaleX(mEndScaleX);
			mLoadingC.setScaleY(mEndScaleY);
		}

		//Phase 2) Fade out our views to expose the content behind
		if (percentage >= animationSplit) {
			float secondPartPerc = 1f - ((percentage - animationSplit) / (1f - animationSplit));
			mContentC.setAlpha(secondPartPerc);
			mBgView.setAlpha(secondPartPerc);
		}
		else {
			mContentC.setAlpha(1f);
			mBgView.setAlpha(1f);
		}
	}

	public void cleanUpGrowToRowsAnim() {
		mLoadingC.setTranslationY(0f);
		mLoadingC.setTranslationX(0f);
		mLoadingC.setScaleX(1f);
		mLoadingC.setScaleY(1f);
		mContentC.setAlpha(1f);
		mBgView.setAlpha(1f);
		mLoadingTv.setAlpha(1f);

		mLoadingC.setLayerType(View.LAYER_TYPE_NONE, null);
		mBgView.setLayerType(View.LAYER_TYPE_NONE, null);
		mLoadingTv.setLayerType(View.LAYER_TYPE_NONE, null);

		//Reset any required translations...
		updateGravities(sLoadingFrags.size() <= 1, false);

		//If 1 second passes, and we are still attached, lets Go back to listening to animation updates as normal
		//This is setup like this because typically if we run this animation we are going to be detached after the animation
		//so there is no good reason to register for updates again.
		Runnable registerForUpdatesRunner = new Runnable() {
			@Override
			public void run() {
				if (getActivity() != null && isResumed() && !isRemoving()) {
					registerForAnimUpdates(ResultsListLoadingFragment.this);
				}
			}
		};
		mRootC.postDelayed(registerForUpdatesRunner, 1000);
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
		updateGravities(sLoadingFrags.size() <= 1, false);
	}


	private void loadingAnimUpdate(int animNumber) {
		if (mLoadingC != null && getActivity() != null && isResumed()) {
			for (int i = 0; i < mLoadingC.getChildCount(); i++) {
				mLoadingC.getChildAt(i)
					.setBackgroundColor(i == animNumber ? mLoadingColorDark : mLoadingColorLight);
			}

			int currentListenerCount = sLoadingFrags.size();
			if (mLastListenerCount != currentListenerCount) {
				if (mLastListenerCount <= 1 && currentListenerCount > 1) {
					updateGravities(currentListenerCount <= 1, true);
				}
				else if (mLastListenerCount > 1 && currentListenerCount <= 1) {
					updateGravities(currentListenerCount <= 1, true);
				}
				mLastListenerCount = currentListenerCount;
			}
		}
	}

	private void updateGravities(boolean alone, boolean animate) {
		if (mContentC != null) {
			if (alone) {
				setContentGravity(mLoadingAloneGravity, animate);
			}
			else {
				setContentGravity(mLoadingWithOthersGravity, animate);
			}
		}
	}

	private void setContentGravity(int gravity, boolean animate) {

		int loadingBarWidth = mLoadingC.getWidth();
		int textWidth = mLoadingTv.getVisibility() == View.VISIBLE ? mLoadingTv.getWidth() : 0;
		int loadingAndTextWidth = Math.max(loadingBarWidth, textWidth);

		float translationX = 0;

		switch (gravity) {
		case Gravity.CENTER: {
			float centerX = mRootC.getWidth() / 2f;
			float halfContent = loadingAndTextWidth / 2f;
			translationX = centerX - halfContent;
			break;
		}
		case Gravity.LEFT: {
			int leftMargin = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_padding_x);
			translationX = leftMargin;
			break;
		}
		case Gravity.RIGHT: {
			int rightMargin = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_padding_x);
			translationX = mRootC.getWidth() - loadingAndTextWidth - rightMargin;
			break;
		}
		default: {
			throw new RuntimeException(
				"ResultsListLoadingFragment currently only supports the following gravities: CENTER,LEFT,RIGHT");
		}
		}

		if (textWidth > 0) {
			if (textWidth > loadingBarWidth) {
				setViewTranslationX(mLoadingTv, translationX, animate);
				if (gravity == Gravity.CENTER) {
					setViewTranslationX(mLoadingC, translationX + (textWidth - loadingBarWidth) / 2f, animate);
				}
				else if (gravity == Gravity.LEFT) {
					setViewTranslationX(mLoadingC, translationX, animate);
				}
				else if (gravity == Gravity.RIGHT) {
					setViewTranslationX(mLoadingC, translationX + loadingAndTextWidth - loadingBarWidth, animate);
				}
			}
			else {
				setViewTranslationX(mLoadingTv, translationX + (loadingBarWidth - textWidth) / 2f, animate);
				setViewTranslationX(mLoadingC, translationX, animate);
			}
		}
		else {
			setViewTranslationX(mLoadingC, translationX, animate);
			setViewTranslationX(mLoadingTv, translationX, animate);
		}

		mLastGravity = gravity;
	}

	private void setViewTranslationX(View view, float translationX, boolean animate) {
		if (animate) {
			view.animate().translationX(translationX);
		}
		else {
			view.setTranslationX(translationX);
		}
	}

	private void setViewTranslationY(View view, float translationY, boolean animate) {
		if (animate) {
			view.animate().translationY(translationY);
		}
		else {
			view.setTranslationY(translationY);
		}
	}

	private void setLoadingTextVisible(boolean visible) {
		mLoadingTv.setAlpha(visible ? 1f : 0f);
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
			else if (!sLoadingFrags.contains(frag)) {
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
