package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;

public class LaunchPin extends FrameLayout {

	private RoundImageView mImageView;
	private TextView mTextView;

	private LaunchLocation mLocation;

	public LaunchPin(Context context) {
		super(context);
	}

	public LaunchPin(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LaunchPin(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mImageView = Ui.findView(this, R.id.launch_pin_image_view);
		mTextView = Ui.findView(this, R.id.launch_pin_text_view);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LaunchPin) {
			LaunchPin p = (LaunchPin) o;
			if (mLocation != null && p.mLocation != null) {
				// TODO: better comparison than this?
				return mLocation.title.equals(p.mLocation.title);
			}
		}
		return false;
	}

	public void bind(LaunchLocation location) {
		mLocation = location;
		retrieveImage(location);
		setPinText(location.title);
	}

	/**
	 * Returns a Rect matching the global location of the circle on the screen.
	 *
	 * @return
	 */
	public Rect getGlobalOrigin() {
		Rect origin = ScreenPositionUtils.getGlobalScreenPosition(this);
		int size = getResources().getDimensionPixelSize(R.dimen.launch_pin_size);
		origin.bottom = origin.top + size;
		origin.left = (origin.left + origin.right - size) / 2;
		origin.right = origin.left + size;
		return origin;
	}

	private void retrieveImage(LaunchLocation location) {
		UrlBitmapDrawable bitmap = UrlBitmapDrawable.loadImageView(location.getImageUrl(), mImageView);
		bitmap.setOnBitmapLoadedCallback(new L2ImageCache.OnBitmapLoaded() {
			@Override
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				mImageView.setImageBitmap(bitmap);
			}

			@Override
			public void onBitmapLoadFailed(String url) {

			}
		});
	}

	private void setPinText(String upper) {
		TextAppearanceSpan upperSpan = new TextAppearanceSpan(getContext(), R.style.MapPinUpperTextAppearance);

		SpannableBuilder sb = new SpannableBuilder();
		sb.append(upper, upperSpan);

		mTextView.setText(sb.build(), android.widget.TextView.BufferType.SPANNABLE);
	}

}
