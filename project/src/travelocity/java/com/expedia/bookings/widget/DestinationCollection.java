package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.util.LaunchScreenAnimationUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DestinationCollection extends FrameLayout {
	public DestinationCollection(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@InjectView(R.id.front_image_view)
	ImageView frontImageView;

	@InjectView(R.id.text)
	TextView textView;

	@InjectView(R.id.text_bg)
	View textBg;

	@InjectView(R.id.bg_overlay)
	View bgOverlay;

	public ImageView getFrontImageView() {
		return frontImageView;
	}

	public TextView getTextView() {
		return textView;
	}

	public View getTextBg() {
		return textBg;
	}

	public View getBgOverlay() {
		return bgOverlay;
	}

	private boolean isNearbyDefaultImage = false;
	private int customWidth;

	public int getCustomWidth() {
		return getLayoutParams().width;
	}

	public void setCustomWidth(int customWidth) {
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		layoutParams.width = customWidth;
		setLayoutParams(layoutParams);
	}


	private void init() {
		setClipChildren(false);
		Ui.inflate(getContext(), R.layout.widget_launch_destination_collection, this);
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

	private ArrayList<LaunchScreenAnimationUtil.PicassoTargetCallback> picassoTargetCallbacks = new ArrayList<>();

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

	public void setNearByDefaultImage(boolean mNearByDefaultImage) {
		this.isNearbyDefaultImage = mNearByDefaultImage;
	}

	public boolean isNearByDefaultImage() {

		return isNearbyDefaultImage;
	}

	public void setLaunchCollection(LaunchCollection collectionToAdd) {
		setNearByDefaultImage(collectionToAdd.isDestinationImageCode);
		setText(collectionToAdd.getTitle());
		setTag(collectionToAdd);
	}

}
