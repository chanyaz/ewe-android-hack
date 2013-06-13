package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.expedia.bookings.R;

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
	// Convenience builders for preset sites

	public enum Preset {
		HAWAII,
		LAS_VEGAS,
		ORLANDO,
	}

	public static LocalExpertSite buildPreset(Context context, Preset preset) {
		LocalExpertSite.Builder siteBuilder = new LocalExpertSite.Builder(context);

		switch (preset) {
		case HAWAII:
			siteBuilder.setCity(R.string.site_hawaii);
			siteBuilder.setCityIcon(R.drawable.ic_local_expert_hawaii);
			siteBuilder.setPhoneNumber("1-888-353-8528");

			// Location-specific attractions
			siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
					.setFirstLine(R.string.attraction_beaches_first)
					.setSecondLine(R.string.attraction_beaches_second)
					.setIconSmall(R.drawable.ic_local_expert_beach_small)
					.setIconLarge(R.drawable.ic_local_expert_beach_large)
					.build());

			siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
					.setFirstLine(R.string.attraction_helicopter_first)
					.setSecondLine(R.string.attraction_helicopter_second)
					.setIconSmall(R.drawable.ic_local_expert_helicopter_small)
					.setIconLarge(R.drawable.ic_local_expert_helicopter_large)
					.build());

			siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
					.setFirstLine(R.string.attraction_scuba_first)
					.setSecondLine(R.string.attraction_scuba_second)
					.setIconSmall(R.drawable.ic_local_expert_scuba_small)
					.setIconLarge(R.drawable.ic_local_expert_scuba_large)
					.build());

			break;
		case LAS_VEGAS:
			siteBuilder.setCity(R.string.site_las_vegas);
			siteBuilder.setCityIcon(R.drawable.ic_local_expert_vegas);
			siteBuilder.setPhoneNumber("1-888-353-8529");

			// Location-specific attractions
			siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
					.setFirstLine(R.string.attraction_casinos_first)
					.setSecondLine(R.string.attraction_casinos_second)
					.setIconSmall(R.drawable.ic_local_expert_slots_small)
					.setIconLarge(R.drawable.ic_local_expert_slots_large)
					.build());

			siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
					.setFirstLine(R.string.attraction_ferrari_first)
					.setSecondLine(R.string.attraction_ferrari_second)
					.setIconSmall(R.drawable.ic_local_expert_car_small)
					.setIconLarge(R.drawable.ic_local_expert_car_large)
					.build());

			break;
		case ORLANDO:
			siteBuilder.setCity(R.string.site_orlando);
			siteBuilder.setCityIcon(R.drawable.ic_local_expert_orlando);
			siteBuilder.setPhoneNumber("1-888-300-7352");

			// Location-specific attractions
			siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
					.setFirstLine(R.string.attraction_creatures_first)
					.setSecondLine(R.string.attraction_creatures_second)
					.setIconSmall(R.drawable.ic_local_expert_whale_small)
					.setIconLarge(R.drawable.ic_local_expert_whale_large)
					.build());

			siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
					.setFirstLine(R.string.attraction_theme_parks_first)
					.setSecondLine(R.string.attraction_theme_parks_second)
					.setIconSmall(R.drawable.ic_local_expert_ferris_small)
					.setIconLarge(R.drawable.ic_local_expert_ferris_large)
					.build());

			break;
		}

		// Common attractions that are shown for all sites
		siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
				.setFirstLine(R.string.attraction_family_first)
				.setSecondLine(R.string.attraction_family_second)
				.setIconSmall(R.drawable.ic_local_expert_family_small)
				.setIconLarge(R.drawable.ic_local_expert_family_large)
				.build());

		siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
				.setFirstLine(R.string.attraction_food_first)
				.setSecondLine(R.string.attraction_food_second)
				.setIconSmall(R.drawable.ic_local_expert_food_small)
				.setIconLarge(R.drawable.ic_local_expert_food_large)
				.build());

		siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
				.setFirstLine(R.string.attraction_entertainment_first)
				.setSecondLine(R.string.attraction_entertainment_second)
				.setIconSmall(R.drawable.ic_local_expert_mask_small)
				.setIconLarge(R.drawable.ic_local_expert_mask_large)
				.build());

		siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
				.setFirstLine(R.string.attraction_music_first)
				.setSecondLine(R.string.attraction_music_second)
				.setIconSmall(R.drawable.ic_local_expert_music_small)
				.setIconLarge(R.drawable.ic_local_expert_music_large)
				.build());

		siteBuilder.addAttraction((new LocalExpertAttraction.Builder(context))
				.setFirstLine(R.string.attraction_vip_first)
				.setSecondLine(R.string.attraction_vip_second)
				.setIconSmall(R.drawable.ic_local_expert_vip_small)
				.setIconLarge(R.drawable.ic_local_expert_vip_large)
				.build());

		return siteBuilder.build();
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
