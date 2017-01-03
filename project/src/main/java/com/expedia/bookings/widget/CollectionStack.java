package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.launch.data.LaunchCollection;
import com.expedia.bookings.launch.data.LaunchDb;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Ui;
import com.squareup.picasso.Picasso;

public class CollectionStack extends FrameLayout {
	public CollectionStack(Context context) {
		super(context);
		init();
	}

	public CollectionStack(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CollectionStack(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private float mBasePadding;

	private FrameLayout mPressedStateView;
	private ImageView mFrontImageView;
	private ImageView mMiddleImageView;
	private ImageView mBackImageView;
	private TextView mTextView;
	private ImageView mCheckView;

	private int mBackgroundColor;
	private boolean mIsStack = true;

	private ArrayList<StackCallback> callbacks = new ArrayList<StackCallback>();
	private boolean mIsNearbyDefaultImage = false;

	private void init() {
		setClipChildren(false);

		mBasePadding = getContext().getResources().getDimension(R.dimen.destination_stack_padding);

		View root = Ui.inflate(getContext(), R.layout.widget_collection_stack, this);

		mBackgroundColor = getContext().getResources().getColor(R.color.tablet_bg_tiles_blend);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mPressedStateView = Ui.findView(this, R.id.stack_pressed_view);
		mFrontImageView = Ui.findView(this, R.id.front_image_view);
		mMiddleImageView = Ui.findView(this, R.id.middle_image_view);
		mBackImageView = Ui.findView(this, R.id.back_image_view);
		mTextView = Ui.findView(this, R.id.text);
		mCheckView = Ui.findView(this, R.id.checkmark);

		mFrontImageView.setTranslationX(mBasePadding * 2);
		mFrontImageView.setTranslationY(mBasePadding * 2);
		mPressedStateView.setTranslationX(mBasePadding * 2);
		mPressedStateView.setTranslationY(mBasePadding * 2);

		mMiddleImageView.setTranslationY(mBasePadding);
	}

	public void cleanup() {
		setBackgroundDrawable(null);
		if (mBackImageView != null) {
			mBackImageView.setImageDrawable(null);
		}
		if (mMiddleImageView != null) {
			mMiddleImageView.setImageDrawable(null);
		}
		if (mFrontImageView != null) {
			mFrontImageView.setImageDrawable(null);
		}
	}

	public void disableStack() {
		mIsStack = false;
	}

	public void setStackDrawable(final String url) {
		if (mIsStack) {
			int gradColor;
			HeaderBitmapDrawable drawable;

			drawable = makeHeaderBitmapDrawable(url);
			gradColor = getContext().getResources().getColor(R.color.tablet_collection_back_image_overlay);
			drawable.setGradient(new int[] {gradColor, gradColor}, null);
			mBackImageView.setImageDrawable(drawable);
			mBackImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

			drawable = makeHeaderBitmapDrawable(url);
			gradColor = getContext().getResources().getColor(R.color.tablet_collection_middle_image_overlay);
			drawable.setGradient(new int[] {gradColor, gradColor}, null);
			mMiddleImageView.setImageDrawable(drawable);
			mMiddleImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
		else {
			removeView(mBackImageView);
			removeView(mMiddleImageView);
			mBackImageView = null;
			mMiddleImageView = null;
		}

		if (mFrontImageView != null) {
			Drawable bg = makeHeaderBitmapDrawable(url);
			mFrontImageView.setImageDrawable(bg);
			mFrontImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
	}

	private HeaderBitmapDrawable makeHeaderBitmapDrawable(String url) {
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setCornerMode(HeaderBitmapDrawable.CornerMode.ALL);
		headerBitmapDrawable.setCornerRadius(getContext().getResources().getDimensionPixelSize(R.dimen.destination_stack_corner_radius));

		final int width = getContext().getResources().getDimensionPixelSize(R.dimen.destination_search_stack_width);
		final String imageUrl = new Akeakamai(url) //
			.downsize(Akeakamai.pixels(width), Akeakamai.preserve()) //
			.build();

		StackCallback callback = new StackCallback(headerBitmapDrawable);
		//These callbacks require a strong reference
		callbacks.add(callback);

		ArrayList<String> urls = new ArrayList<String>();
		urls.add(imageUrl);
		if (isNearByDefaultImage()) {
			String defaultImage = Images.getTabletLaunch(LaunchDb.NEAR_BY_TILE_DEFAULT_IMAGE_CODE);
			final String defaultImageUrl = new Akeakamai(defaultImage) //
				.downsize(Akeakamai.pixels(width), Akeakamai.preserve()) //
				.build();
			urls.add(defaultImageUrl);
		}
		new PicassoHelper.Builder(getContext()).setPlaceholder(
			R.drawable.bg_itin_placeholder).setTarget(callback).build().load(urls);

		headerBitmapDrawable.setScaleType(HeaderBitmapDrawable.ScaleType.TOP_CROP);

		return headerBitmapDrawable;
	}

	private class StackCallback extends PicassoTarget {
		private HeaderBitmapDrawable mHeaderBitmapDrawable;

		StackCallback(HeaderBitmapDrawable headerBitmapDrawable) {
			mHeaderBitmapDrawable = headerBitmapDrawable;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			super.onBitmapLoaded(bitmap, from);

			Palette palette = Palette.generate(bitmap);
			int color = palette.getVibrantColor(getResources().getColor(R.color.transparent_dark));
			mHeaderBitmapDrawable.setBitmap(bitmap);

			ColorBuilder fullColorBuilder = new ColorBuilder(color).darkenBy(0.3f);
			int fullColor = fullColorBuilder.setAlpha(217).build();

			GradientDrawable textViewBackground = (GradientDrawable) getResources()
				.getDrawable(R.drawable.bg_collection_title);
			textViewBackground.setColor(fullColor);
			GradientDrawable checkMarkViewBackground = (GradientDrawable) getResources()
				.getDrawable(R.drawable.selected_tile_overlay);
			checkMarkViewBackground.setColor(fullColor);
			mTextView.setBackground(textViewBackground);
			mCheckView.setBackground(checkMarkViewBackground);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			super.onBitmapFailed(errorDrawable);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			super.onPrepareLoad(placeHolderDrawable);
			mHeaderBitmapDrawable.setPlaceholderDrawable(placeHolderDrawable);
		}
	}

	public void setText(CharSequence title) {
		mTextView.setText(title);
	}

	public void setNearByDefaultImage(boolean mNearByDefaultImage) {
		this.mIsNearbyDefaultImage = mNearByDefaultImage;
	}

	public boolean isNearByDefaultImage() {

		return mIsNearbyDefaultImage;
	}

	/**
	 * Used for animating the background stack effect. Valid values range from [-1, 1].
	 * @param amount
	 */
	public void setStackPosition(float amount) {
		if (amount < -1.0f || amount > 1.0f) {
			// Outside the bounds, just ignore
			// We're partially offscreen too,
			// don't waste time
			return;
		}

		if (!mIsStack) {
			return;
		}

		final float frontLeft = mBasePadding * 2.0f;
		final float backLeft = amount * 3.0f * frontLeft + frontLeft;
		final float middleLeft = (backLeft + frontLeft) / 2.0f;

		mBackImageView.setTranslationX(backLeft);
		mMiddleImageView.setTranslationX(middleLeft);
	}

	public void setCheckEnabled(boolean enabled) {
		mCheckView.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	public void setLaunchCollection(LaunchCollection collectionToAdd) {
		setNearByDefaultImage(collectionToAdd.isDestinationImageCode);
		setStackDrawable(collectionToAdd.getImageUrl());
		setText(collectionToAdd.getTitle());
		setTag(collectionToAdd);
	}

}
