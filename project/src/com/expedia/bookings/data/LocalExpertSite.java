package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class LocalExpertSite implements Parcelable {

	private CharSequence mCity;
	private int mCityIcon;
	private CharSequence mPhoneNumber;
	private List<LocalExpertAttraction> mAttractions = new ArrayList<LocalExpertAttraction>();

	private LocalExpertSite() {
		// Default constructor; use Builder
	}

	public CharSequence getCity() {
		return mCity;
	}

	public int getCityIcon() {
		return mCityIcon;
	}

	public CharSequence getPhoneNumber() {
		return mPhoneNumber;
	}

	public List<LocalExpertAttraction> getAttractions() {
		return mAttractions;
	}

	//////////////////////////////////////////////////////////////////////////
	// Builder

	public static class Builder {
		private Context mContext;

		private LocalExpertSite mSite;

		public Builder(Context context) {
			mContext = context;
			mSite = new LocalExpertSite();
		}

		public LocalExpertSite build() {
			return mSite;
		}

		public Builder setCity(int cityResId) {
			return setCity(mContext.getText(cityResId));
		}

		public Builder setCity(CharSequence city) {
			mSite.mCity = city;
			return this;
		}

		public Builder setCityIcon(int cityIcon) {
			mSite.mCityIcon = cityIcon;
			return this;
		}

		public Builder setPhoneNumber(int phoneNumberResId) {
			return setPhoneNumber(mContext.getString(phoneNumberResId));
		}

		public Builder setPhoneNumber(CharSequence phoneNumber) {
			mSite.mPhoneNumber = phoneNumber;
			return this;
		}

		public Builder addAttraction(LocalExpertAttraction attraction) {
			mSite.mAttractions.add(attraction);
			return this;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Parcelable

	private LocalExpertSite(Parcel in) {
		mCity = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
		mCityIcon = in.readInt();
		mPhoneNumber = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
		in.readList(mAttractions, getClass().getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		TextUtils.writeToParcel(mCity, dest, flags);
		dest.writeInt(mCityIcon);
		TextUtils.writeToParcel(mPhoneNumber, dest, flags);
		dest.writeList(mAttractions);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<LocalExpertSite> CREATOR = new Parcelable.Creator<LocalExpertSite>() {
		public LocalExpertSite createFromParcel(Parcel in) {
			return new LocalExpertSite(in);
		}

		public LocalExpertSite[] newArray(int size) {
			return new LocalExpertSite[size];
		}
	};

}
