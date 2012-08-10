package com.expedia.bookings.model;

import java.util.ArrayList;

import com.mobiata.android.Log;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This is a class to control yoyo flows. Basically this is just a wrapper for a
 * stack that implements Parcelable so it can be passed between activities.
 * @author jdrotos
 *
 */
public class YoYo implements Parcelable {
	public final static String TAG_YOYO = "TAG_YOYO";

	private ArrayList<Class> mYoYoStack = new ArrayList<Class>();
	private int mIndex = 0;

	public YoYo() {
	}

	public void addYoYoTrick(Class destClass) {
		mYoYoStack.add(destClass);
	}

	/**
	 * Get the next activity in the yoyo to launch, or null
	 * @param currentActivity - pass in the current activity so we know when we have progressed in the stack, otherwise hitting back will mess things up
	 * @return
	 */
	public Class popNextTrick(Class currentActivity) {
		Class retClass = null;
		if (hasValidIndex() && currentActivity == mYoYoStack.get(mIndex)) {
			mIndex++;
		}
		if (hasValidIndex()) {
			retClass = mYoYoStack.get(mIndex);
		}
		return retClass;
	}

	public void clearTrickStack() {
		mYoYoStack.clear();
		mIndex = 0;
	}

	private boolean hasValidIndex() {
		return (mIndex < mYoYoStack.size() && mIndex >= 0);
	}

	public boolean isLast() {
		return mIndex == mYoYoStack.size() - 1;
	}

	public boolean isEmpty(Class currentActivity) {
		return (popNextTrick(currentActivity) == null);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] classNames = new String[mYoYoStack.size()];
		for (int i = 0; i < classNames.length; i++) {
			classNames[i] = mYoYoStack.get(i).getName();
		}
		dest.writeStringArray(classNames);
		dest.writeInt(mIndex);
	}

	public static final Parcelable.Creator<YoYo> CREATOR = new Parcelable.Creator<YoYo>() {
		public YoYo createFromParcel(Parcel in) {
			return new YoYo(in);
		}

		public YoYo[] newArray(int size) {
			return new YoYo[size];
		}
	};

	private YoYo(Parcel in) {
		String[] arr = in.createStringArray();
		for (int i = 0; i < arr.length; i++) {
			try {
				mYoYoStack.add(Class.forName(arr[i]));
			}
			catch (Exception ex) {
				Log.e("Failed to parse class", ex);
			}
		}
		mIndex = in.readInt();

	}
}
