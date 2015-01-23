package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.content.Context;
import android.os.Parcel;

import com.expedia.bookings.data.LocalExpertAttraction;
import com.expedia.bookings.data.LocalExpertSite;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class LocalExpertDataTest {

	private Context getContext() {
		return Robolectric.application;
	}

	@Test
	public void testLocalExpertAttraction() {
		String firstLine = "first line";
		String secondLine = "second line";
		int iconSmall = 1;
		int iconLarge = 2;

		// Build the attraction
		LocalExpertAttraction.Builder builder = new LocalExpertAttraction.Builder(getContext());
		builder.setFirstLine(firstLine);
		builder.setSecondLine(secondLine);
		builder.setIconSmall(iconSmall);
		builder.setIconLarge(iconLarge);
		LocalExpertAttraction attraction = builder.build();

		// Check that it equals the previous data
		Assert.assertEquals(firstLine, attraction.getFirstLine());
		Assert.assertEquals(secondLine, attraction.getSecondLine());
		Assert.assertEquals(iconSmall, attraction.getIconSmall());
		Assert.assertEquals(iconLarge, attraction.getIconLarge());

		// Parcel/unparcel, make sure it is still equivalent
		Parcel parcel = Parcel.obtain();
		attraction.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		byte[] data = parcel.marshall();
		parcel.unmarshall(data, 0, data.length);
		parcel.setDataPosition(0);
		attraction = LocalExpertAttraction.CREATOR.createFromParcel(parcel);

		Assert.assertEquals(firstLine, attraction.getFirstLine());
		Assert.assertEquals(secondLine, attraction.getSecondLine());
		Assert.assertEquals(iconSmall, attraction.getIconSmall());
		Assert.assertEquals(iconLarge, attraction.getIconLarge());
	}

	@Test
	public void testLocalExpertSite() {
		String city = "San Francisco";
		int cityIcon = 1;
		String phoneNumber = "2222222222";
		int background = 4;
		String firstLine = "first line";
		String secondLine = "second line";
		int iconSmall = 1;
		int iconLarge = 2;

		// Build the attraction
		LocalExpertAttraction.Builder builder = new LocalExpertAttraction.Builder(getContext());
		builder.setFirstLine(firstLine);
		builder.setSecondLine(secondLine);
		builder.setIconSmall(iconSmall);
		builder.setIconLarge(iconLarge);
		LocalExpertAttraction attraction = builder.build();

		// Build the local expert site
		LocalExpertSite.Builder builder2 = new LocalExpertSite.Builder(getContext());
		builder2.setCity(city);
		builder2.setCityIcon(cityIcon);
		builder2.setPhoneNumber(phoneNumber);
		builder2.setBackground(background);
		builder2.addAttraction(attraction);
		LocalExpertSite site = builder2.build();

		// Make sure everything made it in alright
		Assert.assertEquals(city, site.getCity());
		Assert.assertEquals(cityIcon, site.getCityIcon());
		Assert.assertEquals(phoneNumber, site.getPhoneNumber());
		Assert.assertEquals(background, site.getBackgroundResId());
		attraction = site.getAttractions().get(0);
		Assert.assertEquals(firstLine, attraction.getFirstLine());
		Assert.assertEquals(secondLine, attraction.getSecondLine());
		Assert.assertEquals(iconSmall, attraction.getIconSmall());
		Assert.assertEquals(iconLarge, attraction.getIconLarge());

		// Parcel/unparcel, make sure it is still equivalent
		Parcel parcel = Parcel.obtain();
		site.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		byte[] data = parcel.marshall();
		parcel.unmarshall(data, 0, data.length);
		parcel.setDataPosition(0);
		site = LocalExpertSite.CREATOR.createFromParcel(parcel);

		Assert.assertEquals(city, site.getCity());
		Assert.assertEquals(cityIcon, site.getCityIcon());
		Assert.assertEquals(phoneNumber, site.getPhoneNumber());
		Assert.assertEquals(background, site.getBackgroundResId());
		attraction = site.getAttractions().get(0);
		Assert.assertEquals(firstLine, attraction.getFirstLine());
		Assert.assertEquals(secondLine, attraction.getSecondLine());
		Assert.assertEquals(iconSmall, attraction.getIconSmall());
		Assert.assertEquals(iconLarge, attraction.getIconLarge());
	}
}
