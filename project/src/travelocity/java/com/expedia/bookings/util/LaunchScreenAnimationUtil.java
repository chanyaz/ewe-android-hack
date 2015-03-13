package com.expedia.bookings.util;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.picasso.Picasso;

/**
 * Defines common utilities for working with TVLY tablet home screen animations.
 */
public class LaunchScreenAnimationUtil {
	private LaunchScreenAnimationUtil() {
	}

	public static int getActionBarNavBarSize(Context context) {
		return LayoutUtils.getActionBarSize(context) + context.getResources()
			.getDimensionPixelSize(R.dimen.extra_status_bar_padding);
	}

	public static int getMarginBottom(Context context) {
		return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? context
			.getResources().getDimensionPixelSize(R.dimen.destination_tile_extra_bottom_padding) : 0;
	}

	public static void applyColorToOverlay(Activity activity, View... views) {
		ColorBuilder fullColorBuilder = new ColorBuilder(
			Ui.obtainThemeColor(activity, R.attr.skin_collection_overlay_static_color));
		int textColor = fullColorBuilder
			.setOpacity(0.8f)
			.darkenBy(0.8f)
			.setAlpha(200)
			.build();

		GradientDrawable textViewBackground = (GradientDrawable) activity.getResources()
			.getDrawable(R.drawable.bg_collection_title);
		textViewBackground.setColor(textColor);
		if (Build.VERSION.SDK_INT < 16) {
			for (View view : views) {
				view.setBackgroundDrawable(textViewBackground);
			}
		}
		else {
			for (View view : views) {
				view.setBackground(textViewBackground);
			}
		}
	}

	public static HeaderBitmapDrawable makeHeaderBitmapDrawable(Context context,
		ArrayList<PicassoTargetCallback> picassoTargetCallbacks, String url,
		boolean isNearByDefaultImage) {
		HeaderBitmapDrawable headerBitmapDrawable = new HeaderBitmapDrawable();
		headerBitmapDrawable.setCornerMode(HeaderBitmapDrawable.CornerMode.ALL);
		headerBitmapDrawable
			.setCornerRadius(context.getResources().getDimensionPixelSize(R.dimen.destination_stack_corner_radius));
		Point screenSize = AndroidUtils.getDisplaySize(context);

		final int marginTop = getActionBarNavBarSize(context);
		final int marginBottom = getMarginBottom(context);

		final String imageUrl = new Akeakamai(url)
			.downsize(Akeakamai.preserve(), Akeakamai.pixels(screenSize.y - marginBottom - marginTop))
			.build();
		PicassoTargetCallback callback = new PicassoTargetCallback(headerBitmapDrawable);
		picassoTargetCallbacks.add(callback);
		//These callbacks require a strong reference

		ArrayList<String> urls = new ArrayList<String>();
		urls.add(imageUrl);
		if (isNearByDefaultImage) {
			String defaultImage = Images.getTabletLaunch(LaunchDb.NEAR_BY_TILE_DEFAULT_IMAGE_CODE);
			final String defaultImageUrl = new Akeakamai(defaultImage) //
				.downsize(Akeakamai.pixels(screenSize.x), Akeakamai.pixels(screenSize.y - marginBottom - marginTop))
				.build();
			urls.add(defaultImageUrl);
		}
		new PicassoHelper.Builder(context).setPlaceholder(Ui.obtainThemeResID(context,
			R.attr.skin_collection_placeholder)).setTarget(callback).build().load(urls);

		return headerBitmapDrawable;
	}

	public static class PicassoTargetCallback extends PicassoTarget {
		private HeaderBitmapDrawable headerBitmapDrawable;

		PicassoTargetCallback(HeaderBitmapDrawable headerBitmapDrawable) {
			this.headerBitmapDrawable = headerBitmapDrawable;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			super.onBitmapLoaded(bitmap, from);
			headerBitmapDrawable.setBitmap(bitmap);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			super.onBitmapFailed(errorDrawable);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			super.onPrepareLoad(placeHolderDrawable);
			headerBitmapDrawable.setPlaceholderDrawable(placeHolderDrawable);
		}
	}
}
