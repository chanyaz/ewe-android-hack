package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Ui;
import com.squareup.picasso.Picasso;

public class DestinationCollectionStack extends FrameLayout {
	public DestinationCollectionStack(Context context) {
		super(context);
		init();
	}

	public DestinationCollectionStack(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DestinationCollectionStack(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private ArrayList<StackCallback> callbacks = new ArrayList<StackCallback>();
	private ImageView mFrontImageView;
	private TextView mTextView;

	private boolean mIsNearbyDefaultImage = false;

	private void init() {
		View root = Ui.inflate(getContext(), R.layout.widget_launch_collection_travelocity, this);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		mFrontImageView = Ui.findView(this, R.id.front_image_view);
		mTextView = Ui.findView(this, R.id.text);
	}

	public void cleanup() {
		setBackgroundDrawable(null);
		if (mFrontImageView != null) {
			mFrontImageView.setImageDrawable(null);
		}
	}

	public void setStackDrawable(final String url) {

		if (mFrontImageView != null) {
			Drawable bg = makeHeaderBitmapDrawable(url);
			mFrontImageView.setImageDrawable(bg);
			mFrontImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
	}

	private HeaderBitmapDrawable makeHeaderBitmapDrawable(String url) {
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setCornerMode(HeaderBitmapDrawable.CornerMode.ALL);

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
		new PicassoHelper.Builder(getContext()).setPlaceholder(Ui.obtainThemeResID(getContext(),
			R.attr.skin_collection_placeholder)).setTarget(callback).build().load(urls);

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

			mHeaderBitmapDrawable.setBitmap(bitmap);

			int textColor;
			int fullColor;
			ColorBuilder fullColorBuilder;
			fullColorBuilder = new ColorBuilder(
				Ui.obtainThemeColor(getContext(), R.attr.skin_collection_overlay_static_color));
			textColor = fullColorBuilder
				.setOpacity(0.8f)
				.setAlpha(224)
				.build();
			fullColor = fullColorBuilder.setAlpha(0.3f).build();

			GradientDrawable textViewBackground = (GradientDrawable) getResources()
				.getDrawable(R.drawable.bg_collection_title);
			textViewBackground.setColor(textColor);
			GradientDrawable checkMarkViewBackground = (GradientDrawable) getResources()
				.getDrawable(R.drawable.selected_tile_overlay);
			checkMarkViewBackground.setColor(fullColor);
			if (Build.VERSION.SDK_INT < 16) {
				mTextView.setBackgroundDrawable(textViewBackground);
			}
			else {
				mTextView.setBackground(textViewBackground);
			}
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

	public void setLaunchCollection(LaunchCollection collectionToAdd) {
		setNearByDefaultImage(collectionToAdd.isDestinationImageCode);
		setText(collectionToAdd.getTitle());
		setTag(collectionToAdd);
	}

}
