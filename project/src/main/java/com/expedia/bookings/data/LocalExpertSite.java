package com.expedia.bookings.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.mobiata.android.util.IoUtils;

public class LocalExpertSite implements Parcelable {

	private CharSequence mCity;
	private int mCityIcon;
	private CharSequence mPhoneNumber;
	private int mBackground;
	private List<LocalExpertAttraction> mAttractions = new ArrayList<LocalExpertAttraction>();

	// Used solely for omniture tracking purposes
	private String mTrackingId;

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

	public int getBackgroundResId() {
		return mBackground;
	}

	public List<LocalExpertAttraction> getAttractions() {
		return mAttractions;
	}

	public String getTrackingId() {
		return mTrackingId;
	}

	//////////////////////////////////////////////////////////////////////////
	// Data loaded on init

	private static Map<Destination, String> sPhoneNumbers = new HashMap<Destination, String>();

	public static void init(Context context) {
		try {
			InputStream is = context.getAssets().open("ExpediaSharedData/LocalExpertConfig.json");
			String data = IoUtils.convertStreamToString(is);
			JSONObject leData = new JSONObject(data);
			JSONArray locations = leData.optJSONArray("locations");
			for (int a = 0; a < locations.length(); a++) {
				JSONObject location = locations.optJSONObject(a);

				String name = location.optString("name");
				Destination destination = null;
				if (name.equals("Hawaii")) {
					destination = Destination.HAWAII;
				}
				else if (name.equals("Las Vegas")) {
					destination = Destination.LAS_VEGAS;
				}
				else if (name.equals("Orlando")) {
					destination = Destination.ORLANDO;
				}

				sPhoneNumbers.put(destination, location.optString("phoneNumber"));
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience builders for preset sites

	public enum Destination {
		HAWAII("Hawaii"),
		LAS_VEGAS("LasVegas"),
		ORLANDO("Orlando");

		private String mTrackingId;

		private Destination(String trackingId) {
			mTrackingId = trackingId;
		}

		public String getTrackingId() {
			return mTrackingId;
		}
	}

	public static LocalExpertSite buildDestination(Context context, Destination destination) {
		LocalExpertSite.Builder siteBuilder = new LocalExpertSite.Builder(context);

		siteBuilder.setTrackingId(destination.getTrackingId());
		siteBuilder.setPhoneNumber(sPhoneNumbers.get(destination));

		switch (destination) {
		case HAWAII:
			siteBuilder.setCity(R.string.site_hawaii);
			siteBuilder.setCityIcon(R.drawable.ic_local_expert_hawaii);
			siteBuilder.setBackground(R.drawable.bg_local_expert_hawaii);

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
			siteBuilder.setBackground(R.drawable.bg_local_expert_las_vegas);

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
			siteBuilder.setBackground(R.drawable.bg_local_expert_orlando);

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

		public Builder setBackground(int resId) {
			mSite.mBackground = resId;
			return this;
		}

		public Builder addAttraction(LocalExpertAttraction attraction) {
			mSite.mAttractions.add(attraction);
			return this;
		}

		public Builder setTrackingId(String trackingId) {
			mSite.mTrackingId = trackingId;
			return this;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Parcelable

	private LocalExpertSite(Parcel in) {
		mCity = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
		mCityIcon = in.readInt();
		mPhoneNumber = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
		mBackground = in.readInt();
		in.readList(mAttractions, getClass().getClassLoader());
		mTrackingId = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		TextUtils.writeToParcel(mCity, dest, flags);
		dest.writeInt(mCityIcon);
		TextUtils.writeToParcel(mPhoneNumber, dest, flags);
		dest.writeInt(mBackground);
		dest.writeList(mAttractions);
		dest.writeString(mTrackingId);
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
