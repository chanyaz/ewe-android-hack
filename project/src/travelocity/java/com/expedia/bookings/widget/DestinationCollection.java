package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.HashMap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.launch.data.LaunchCollection;
import com.expedia.bookings.launch.data.LaunchDb;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.dialog.NoLocationServicesDialog;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.otto.TvlyEvents;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.launch.util.LaunchScreenAnimationUtil;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DestinationCollection extends FrameLayout implements View.OnClickListener {
	public static final float NO_OF_TILES_PORTRAIT = 3.25f;
	public static final float NO_OF_TILES_LANDSCAPE = 4.25f;
	private static final int EXPAND_ANIMATION_TIME = 300;
	private static AnimatorSet animatorSet;
	private static HashMap<String, Bitmap> bitmapCache = new HashMap();
	@InjectView(R.id.front_image_view)
	ImageView frontImageView;
	@InjectView(R.id.front_image_view_reflection)
	ImageView frontImageViewReflection;
	@InjectView(R.id.text)
	TextView textView;
	@InjectView(R.id.text_bg)
	View textBg;
	@InjectView(R.id.bg_overlay)
	View bgOverlay;
	private boolean isNearbyDefaultImage = false;
	private PicassoTargetCallback picassoTargetCallback;

	public DestinationCollection(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		Ui.inflate(getContext(), R.layout.widget_launch_destination_collection, this);
		setOnClickListener(this);
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

		Point screenSize = AndroidUtils.getDisplaySize(getContext());
		int destinationWidth = (int) (screenSize.x / NO_OF_TILES_LANDSCAPE);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			int imageMarginBottom = getResources().getDimensionPixelSize(R.dimen.destination_image_margin_bottom);
			int actionBarNavBarHeight = LaunchScreenAnimationUtil.getActionBarNavBarSize(getContext());
			int imageHeight = screenSize.y - imageMarginBottom - actionBarNavBarHeight;

			FrameLayout.LayoutParams frontImageViewReflectionLayoutParams = (LayoutParams) frontImageViewReflection
				.getLayoutParams();
			frontImageViewReflectionLayoutParams.height = imageHeight;
			frontImageViewReflectionLayoutParams.topMargin = imageHeight;
			frontImageViewReflection.setLayoutParams(frontImageViewReflectionLayoutParams);
			frontImageViewReflection.setVisibility(VISIBLE);

			destinationWidth = (int) (screenSize.x / NO_OF_TILES_PORTRAIT);
		}
		LinearLayout.LayoutParams destinationLayoutParams = (LinearLayout.LayoutParams) getLayoutParams();
		destinationLayoutParams.width = destinationWidth;
		setLayoutParams(destinationLayoutParams);

		FrameLayout.LayoutParams textViewLayoutParams = (LayoutParams) textView.getLayoutParams();
		textViewLayoutParams.width = destinationWidth;
		textView.setLayoutParams(textViewLayoutParams);
	}

	public void cleanup() {
		setBackgroundDrawable(null);
		if (frontImageView != null) {
			frontImageView.setImageDrawable(null);
		}
	}

	public void setDrawable(final String url) {
		if (frontImageView != null) {
			frontImageView.setImageDrawable(createHeaderBitmapDrawable(url));
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
		if (animatorSet != null && animatorSet.isRunning()) {
			return;
		}
		final LaunchCollection collectionToAdd = (LaunchCollection) getTag();
		OmnitureTracking.trackTabletLaunchTileSelect(collectionToAdd.id);
		if (collectionToAdd.id.equals(LaunchDb.YOUR_SEARCH_TILE_ID)) {
			Events.post(new Events.SearchSuggestionSelected(collectionToAdd.locations.get(0).location, true));
		}
		else if (collectionToAdd.id.equals(LaunchDb.CURRENT_LOCATION_SEARCH_TILE_ID)) {
			if (null == collectionToAdd.locations || collectionToAdd.locations.isEmpty()) {
				// Show the message to user to enable location
				NoLocationServicesDialog dialog = NoLocationServicesDialog.newInstance();
				dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), NoLocationServicesDialog.TAG);
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

			animatorSet = new AnimatorSet();
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

			ObjectAnimator textTranslateAnimation = ObjectAnimator
				.ofFloat(textView, "translationX", -(screenWidth - textView.getWidth())
					/ 2);

			ObjectAnimator textBgColorAnimation = ObjectAnimator.ofFloat(textBg, "alpha", 0f);
			ObjectAnimator searchAnimation = ObjectAnimator.ofFloat(searchContainer, "alpha", 0f);

			int[] destinationPosition = new int[2];
			getLocationOnScreen(destinationPosition);

			ObjectAnimator scrollAnimation = ObjectAnimator.ofInt(horizontalScrollView, "scrollX",
				horizontalScrollView.getScrollX() + destinationPosition[0]);

			animatorSet.playTogether(expandAnimation, searchAnimation, scrollAnimation, textTranslateAnimation,
				textBgColorAnimation);
			animatorSet.setDuration(EXPAND_ANIMATION_TIME);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					//Reset all the view properties, so that when user come back all view are in place
					Events.post(new Events.LaunchCollectionClicked(collectionToAdd));

					horizontalScrollView.setEnabled(true);
					horizontalScrollView.setOnTouchListener(null);
					setCustomWidth(tileWidth);
					bgOverlay.setAlpha(0);
					textBg.setAlpha(1f);
					searchContainer.setAlpha(1f);
					frontImageViewReflection.setAlpha(1f);
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

	private HeaderBitmapDrawable createHeaderBitmapDrawable(String imageUrl) {
		final int marginTop = LaunchScreenAnimationUtil.getActionBarNavBarSize(getContext());
		final int marginBottom = getContext().getResources().getDimensionPixelSize(
			R.dimen.destination_tile_extra_bottom_padding);
		int screenWidth = getResources().getDimensionPixelSize(R.dimen.destination_tile_image_width);
		imageUrl = new Akeakamai(imageUrl).downsize(Akeakamai.pixels(screenWidth / 2),
			Akeakamai.pixels((screenWidth - marginBottom - marginTop) / 2)).quality(75).build();
		Bitmap bitmap = bitmapCache.get(imageUrl);

		ArrayList<String> urls = new ArrayList<String>();
		urls.add(imageUrl);
		if (isNearByDefaultImage()) {
			String defaultImage = Images.getTabletLaunch(LaunchDb.NEAR_BY_TILE_DEFAULT_IMAGE_CODE);
			final String defaultImageUrl = new Akeakamai(defaultImage).downsize(Akeakamai.pixels(screenWidth / 2),
				Akeakamai.pixels((screenWidth - marginBottom - marginTop) / 2)).quality(75).build();
			urls.add(defaultImageUrl);
			bitmap = bitmapCache.get(defaultImage);
		}

		HeaderBitmapDrawable frontImageHeaderBitmapDrawable = new HeaderBitmapDrawable();
		frontImageHeaderBitmapDrawable.setScaleType(HeaderBitmapDrawable.ScaleType.CENTER_CROP);

		if (bitmap == null) {
			picassoTargetCallback = new PicassoTargetCallback(frontImageHeaderBitmapDrawable, imageUrl);
			new PicassoHelper.Builder(getContext()).setPlaceholder(
				R.drawable.bg_itin_placeholder).setTarget(picassoTargetCallback).build().load(urls);
		}
		else {
			updateFrontDestinationImage(frontImageHeaderBitmapDrawable, bitmap);
		}
		return frontImageHeaderBitmapDrawable;
	}

	private void updateFrontDestinationImage(HeaderBitmapDrawable frontImageHeaderBitmapDrawable, Bitmap bitmap) {
		frontImageHeaderBitmapDrawable.setBitmap(bitmap);
		frontImageViewReflection.setImageDrawable(frontImageHeaderBitmapDrawable);
		((LaunchCollection) getTag()).imageDrawable = frontImageHeaderBitmapDrawable;
		Events.post(new TvlyEvents.DestinationCollectionDrawableAvailable(((LaunchCollection) getTag())));
	}

	private class PicassoTargetCallback extends PicassoTarget {
		private final HeaderBitmapDrawable frontImageHeaderBitmapDrawable;
		private final String url;

		public PicassoTargetCallback(HeaderBitmapDrawable frontImageHeaderBitmapDrawable, String url) {
			this.frontImageHeaderBitmapDrawable = frontImageHeaderBitmapDrawable;
			this.url = url;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			super.onBitmapLoaded(bitmap, from);
			bitmapCache.put(url, bitmap);
			updateFrontDestinationImage(frontImageHeaderBitmapDrawable, bitmap);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			super.onPrepareLoad(placeHolderDrawable);
			frontImageHeaderBitmapDrawable.setPlaceholderDrawable(placeHolderDrawable);
			frontImageViewReflection.setImageDrawable(placeHolderDrawable);
			((LaunchCollection) getTag()).imageDrawable = placeHolderDrawable;
		}
	}

}
