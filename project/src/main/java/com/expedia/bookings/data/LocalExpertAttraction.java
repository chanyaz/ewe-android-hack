package com.expedia.bookings.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class LocalExpertAttraction implements Parcelable {

	private CharSequence mFirstLine;
	private CharSequence mSecondLine;
	private int mIconSmall;
	private int mIconLarge;

	private LocalExpertAttraction() {
		// Default constructor; use Builder
	}

	public CharSequence getFirstLine() {
		return mFirstLine;
	}

	public CharSequence getSecondLine() {
		return mSecondLine;
	}

	public int getIconSmall() {
		return mIconSmall;
	}

	public int getIconLarge() {
		return mIconLarge;
	}

	//////////////////////////////////////////////////////////////////////////
	// Builder

	public static class Builder {
		private Context mContext;

		private LocalExpertAttraction mAttraction;

		public Builder(Context context) {
			mContext = context;
			mAttraction = new LocalExpertAttraction();
		}

		public LocalExpertAttraction build() {
			return mAttraction;
		}

		public Builder setFirstLine(int resId) {
			return setFirstLine(mContext.getString(resId));
		}

		public Builder setFirstLine(CharSequence firstLine) {
			mAttraction.mFirstLine = firstLine;
			return this;
		}

		public Builder setSecondLine(int resId) {
			return setSecondLine(mContext.getString(resId));
		}

		public Builder setSecondLine(CharSequence secondLine) {
			mAttraction.mSecondLine = secondLine;
			return this;
		}

		public Builder setIconSmall(int resId) {
			mAttraction.mIconSmall = resId;
			return this;
		}

		public Builder setIconLarge(int resId) {
			mAttraction.mIconLarge = resId;
			return this;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Parcelable

	private LocalExpertAttraction(Parcel in) {
		mFirstLine = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
		mSecondLine = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
		mIconSmall = in.readInt();
		mIconLarge = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		TextUtils.writeToParcel(mFirstLine, dest, flags);
		TextUtils.writeToParcel(mSecondLine, dest, flags);
		dest.writeInt(mIconSmall);
		dest.writeInt(mIconLarge);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<LocalExpertAttraction> CREATOR = new Parcelable.Creator<LocalExpertAttraction>() {
		public LocalExpertAttraction createFromParcel(Parcel in) {
			return new LocalExpertAttraction(in);
		}

		public LocalExpertAttraction[] newArray(int size) {
			return new LocalExpertAttraction[size];
		}
	};

}
