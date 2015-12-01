package com.expedia.bookings.bitmaps;

import org.jetbrains.annotations.Nullable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Callback when a bitmap needs to be return
 **/
public class PicassoTarget implements Target {

	private Callback mCallBack;
	public boolean mIsFallbackImage = false;

	public PicassoTarget() {

	}

	public void setCallBack(Callback callBack) {
		mCallBack = callBack;
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		if (mCallBack != null) {
			mCallBack.onSuccess();
		}
	}

	@Override
	public void onBitmapFailed(@Nullable Drawable errorDrawable) {
		if (mCallBack != null) {
			mCallBack.onError();
		}
	}

	@Override
	public void onPrepareLoad(@Nullable Drawable placeHolderDrawable) {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PicassoTarget)) {
			return false;
		}

		PicassoTarget that = (PicassoTarget) o;

		if (mIsFallbackImage != that.mIsFallbackImage) {
			return false;
		}

		if (mCallBack != null) {
			return mCallBack.equals(that.mCallBack);
		}
		else {
			return that.mCallBack == null;
		}
	}

	@Override
	public int hashCode() {
		int result = mCallBack != null ? mCallBack.hashCode() : 0;
		result = 31 * result + (mIsFallbackImage ? 1 : 0);
		return result;
	}
}
