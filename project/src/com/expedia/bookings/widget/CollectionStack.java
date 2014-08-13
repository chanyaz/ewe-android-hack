package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.BitmapUtils;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.Ui;

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

		ArrayList<String> urls = new ArrayList<String>();
		urls.add(imageUrl);
		headerBitmapDrawable.setCallback(new L2ImageCache.OnBitmapLoaded() {
			@Override
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				int color = BitmapUtils.getAvgColorOnePixelTrick(bitmap);
				int textColor = new ColorBuilder(color)
					.darkenBy(0.4f)
					.setAlpha(224)
					.build();

				GradientDrawable d = (GradientDrawable) getResources().getDrawable(R.drawable.bg_collection_title);
				d.setColor(textColor);
				if (Build.VERSION.SDK_INT < 16) {
					mTextView.setBackgroundDrawable(d);
				}
				else {
					mTextView.setBackground(d);
				}

				int fullColor = new ColorBuilder(color)
					.darkenBy(0.3f)
					.setAlpha(217)
					.build();

				mCheckView.setBackgroundColor(fullColor);
			}

			@Override
			public void onBitmapLoadFailed(String url) {

			}
		});
		headerBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getContext().getResources(), urls, R.drawable.bg_itin_placeholder));

		headerBitmapDrawable.setScaleType(HeaderBitmapDrawable.ScaleType.TOP_CROP);

		return headerBitmapDrawable;
	}

	public void setText(String title) {
		mTextView.setEllipsize(TextUtils.TruncateAt.END);
		mTextView.setLines(2);
		mTextView.setText(title);
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
}
