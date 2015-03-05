package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.dialog.NoLocationServicesDialog;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.util.LaunchScreenAnimationUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DestinationCollection extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
	private static final int EXPAND_ANIMATION_TIME = 300;
	@InjectView(R.id.front_image_view)
	ImageView frontImageView;
	@InjectView(R.id.text)
	TextView textView;
	@InjectView(R.id.text_bg)
	View textBg;
	@InjectView(R.id.bg_overlay)
	View bgOverlay;
	private boolean isNearbyDefaultImage = false;
	private int customWidth;
	private ArrayList<LaunchScreenAnimationUtil.PicassoTargetCallback> picassoTargetCallbacks = new ArrayList<>();

	public DestinationCollection(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		Ui.inflate(getContext(), R.layout.widget_launch_destination_collection, this);
		setOnClickListener(this);
		setOnLongClickListener(this);
	}

	public int getCustomWidth() {
		return getLayoutParams().width;
	}

	public void setCustomWidth(int customWidth) {
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		layoutParams.width = customWidth;
		setLayoutParams(layoutParams);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		FontCache.setTypeface(textView, FontCache.Font.ROBOTO_LIGHT);
	}

	public void cleanup() {
		setBackgroundDrawable(null);
		if (frontImageView != null) {
			frontImageView.setImageDrawable(null);
		}
	}

	public void setDrawable(final String url) {
		if (frontImageView != null) {
			frontImageView
				.setImageDrawable(
					LaunchScreenAnimationUtil
						.makeHeaderBitmapDrawable(getContext(), picassoTargetCallbacks, url, isNearByDefaultImage()));
			LaunchScreenAnimationUtil.applyColorToOverlay((Activity) getContext(), textBg, bgOverlay);
		}
	}

	public void setText(CharSequence title) {
		textView.setText(title);
	}

	public boolean isNearByDefaultImage() {

		return isNearbyDefaultImage;
	}

	public void setNearByDefaultImage(boolean mNearByDefaultImage) {
		this.isNearbyDefaultImage = mNearByDefaultImage;
	}

	public void setLaunchCollection(LaunchCollection collectionToAdd) {
		setNearByDefaultImage(collectionToAdd.isDestinationImageCode);
		setText(collectionToAdd.getTitle());
		setTag(collectionToAdd);
	}

	private void onDestinationClick() {
		final LaunchCollection collectionToAdd = (LaunchCollection) getTag();
		OmnitureTracking.trackTabletLaunchTileSelect(getContext(), collectionToAdd.id);
		if (collectionToAdd.id.equals(LaunchDb.YOUR_SEARCH_TILE_ID)) {
			Events.post(new Events.SearchSuggestionSelected(collectionToAdd.locations.get(0).location, true));
		}
		else if (collectionToAdd.id.equals(LaunchDb.CURRENT_LOCATION_SEARCH_TILE_ID)) {
			if (null == collectionToAdd.locations || collectionToAdd.locations.isEmpty()) {
				// Show the message to user to enable location
				NoLocationServicesDialog dialog = NoLocationServicesDialog.newInstance();
				dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "NO_LOCATION_FRAG");
			}
			else {
				//Deeplink the current location to Hotel Mode
				SuggestionV2 destination = collectionToAdd.locations.get(0).location;
				destination.setResultType(SuggestionV2.ResultType.CURRENT_LOCATION);
				SearchParams mSearchParams = new SearchParams();
				mSearchParams.setDestination(destination);
				NavUtils.goToTabletResults(getContext(), mSearchParams, LineOfBusiness.HOTELS);
			}
		}
		else {
			final HorizontalScrollView horizontalScrollView = Ui.findView(getRootView(), R.id.destinations_scrollview);
			final View searchContainer = Ui.findView(getRootView(), R.id.fake_search_bar_container);
			int screenWidth = AndroidUtils.getDisplaySize(getContext()).x;

			AnimatorSet animatorSet = new AnimatorSet();
			horizontalScrollView.setEnabled(false);
			horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});
			final int tileWidth = getCustomWidth();
			final int horizontalScrollViewScrollX = horizontalScrollView.getScrollX();

			ObjectAnimator expandAnimation = ObjectAnimator.ofInt(this, "customWidth", screenWidth);

			ObjectAnimator overlayAnimation = ObjectAnimator.ofFloat(bgOverlay, "alpha", 1f);

			ObjectAnimator textTranslateAnimation = ObjectAnimator
				.ofFloat(textView, "translationX", -(screenWidth - textView.getWidth())
					/ 2);

			ObjectAnimator textBgColorAnimation = ObjectAnimator.ofFloat(textBg, "alpha", 0f);

			ObjectAnimator searchAnimation = ObjectAnimator.ofFloat(searchContainer, "alpha", 0f);

			int[] destinationPosition = new int[2];
			getLocationOnScreen(destinationPosition);

			ObjectAnimator scrollAnimation = ObjectAnimator.ofInt(horizontalScrollView, "scrollX",
				horizontalScrollView.getScrollX() + destinationPosition[0]);

			animatorSet.playTogether(expandAnimation, overlayAnimation, searchAnimation,
				scrollAnimation, textTranslateAnimation, textBgColorAnimation);
			animatorSet.setDuration(EXPAND_ANIMATION_TIME);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					//Reset all the view properties, so that when user come back all view are in place
					collectionToAdd.imageDrawable = frontImageView.getDrawable();
					Events.post(new Events.LaunchCollectionClicked(collectionToAdd));

					horizontalScrollView.setEnabled(true);
					horizontalScrollView.setOnTouchListener(null);
					setCustomWidth(tileWidth);
					bgOverlay.setAlpha(0);
					textBg.setAlpha(1f);
					searchContainer.setAlpha(1f);
					textView.setTranslationX(0);
					horizontalScrollView.setScrollX(horizontalScrollViewScrollX);
				}
			});
			animatorSet.start();
		}
	}

	@Override
	public void onClick(View v) {
		onDestinationClick();
	}

	@Override
	public boolean onLongClick(View v) {
		onDestinationClick();
		return false;
	}
}
