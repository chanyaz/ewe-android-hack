package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.style.TextAppearanceSpan;
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
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;

public class CollectionStack extends FrameLayout {
	public CollectionStack(Context context) {
		super(context);
		init(context);
	}

	public CollectionStack(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CollectionStack(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private Context mContext;

	private float mBasePadding;

	private ImageView mFrontImageView;
	private ImageView mMiddleImageView;
	private ImageView mBackImageView;
	private TextView mTextView;
	private ImageView mCheckView;

	private int mBackgroundColor;
	private boolean mIsStack = true;

	private void init(Context context) {
		mContext = context;

		setClipChildren(false);

		mBasePadding = mContext.getResources().getDimension(R.dimen.destination_stack_padding);

		View root = Ui.inflate(context, R.layout.widget_collection_stack, this);

		mBackgroundColor = mContext.getResources().getColor(R.color.tablet_bg_tiles_blend);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		mFrontImageView = Ui.findView(this, R.id.front_image_view);
		mMiddleImageView = Ui.findView(this, R.id.middle_image_view);
		mBackImageView = Ui.findView(this, R.id.back_image_view);
		mTextView = Ui.findView(this, R.id.text);
		mCheckView = Ui.findView(this, R.id.checkmark);

		mFrontImageView.setTranslationX(mBasePadding * 2);
		mFrontImageView.setTranslationY(mBasePadding * 2);

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
			gradColor = Color.parseColor("#e5141d36");
			drawable.setGradient(new int[]{gradColor, gradColor}, null);
			mBackImageView.setImageDrawable(drawable);
			mBackImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

			drawable = makeHeaderBitmapDrawable(url);
			gradColor = Color.parseColor("#b2141d36");
			drawable.setGradient(new int[]{gradColor, gradColor}, null);
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
		headerBitmapDrawable.setCornerRadius(mContext.getResources().getDimensionPixelSize(R.dimen.destination_stack_corner_radius));

		ArrayList<String> urls = new ArrayList<String>();
		urls.add(url);
		headerBitmapDrawable.setCallback(new L2ImageCache.OnBitmapLoaded() {
			@Override
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				ColorBuilder builder = new ColorBuilder(BitmapUtils.getAvgColorOnePixelTrick(bitmap)).darkenBy(0.4f);
				mTextView.setBackgroundColor(builder.build());
			}

			@Override
			public void onBitmapLoadFailed(String url) {

			}
		});
		headerBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(mContext.getResources(), urls, R.drawable.bg_itin_placeholder));

		headerBitmapDrawable.setScaleType(HeaderBitmapDrawable.ScaleType.TOP_CROP);

		return headerBitmapDrawable;
	}

	public void setText(String upper) {
		TextAppearanceSpan upperSpan = new TextAppearanceSpan(getContext(), R.style.DestinationStackUpperTextAppearance);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(upper, upperSpan);

		mTextView.setText(sb.build(), TextView.BufferType.SPANNABLE);
	}

	// From [-1, 1] because that's how I roll
	// Used for animating the background stack effect
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
