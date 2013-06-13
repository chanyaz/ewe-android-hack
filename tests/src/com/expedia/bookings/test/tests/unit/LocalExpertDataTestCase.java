package com.expedia.bookings.test.tests.unit;

import android.os.Parcel;
import android.test.AndroidTestCase;

import com.expedia.bookings.data.LocalExpertAttraction;
import com.expedia.bookings.data.LocalExpertSite;

public class LocalExpertDataTestCase extends AndroidTestCase {

	public void testLocalExpertAttraction() {
		String firstLine = "first line";
		String secondLine = "second line";
		int iconSmall = 1;
		int iconLarge = 2;
		boolean iconAboveText = true;

		// Build the attraction
		LocalExpertAttraction.Builder builder = new LocalExpertAttraction.Builder(getContext());
		builder.setFirstLine(firstLine);
		builder.setSecondLine(secondLine);
		builder.setIconSmall(iconSmall);
		builder.setIconLarge(iconLarge);
		builder.setIconAboveText(iconAboveText);
		LocalExpertAttraction attraction = builder.build();

		// Check that it equals the previous data
		assertEquals(firstLine, attraction.getFirstLine());
		assertEquals(secondLine, attraction.getSecondLine());
		assertEquals(iconSmall, attraction.getIconSmall());
		assertEquals(iconLarge, attraction.getIconLarge());
		assertEquals(iconAboveText, attraction.isIconAboveText());

		// Parcel/unparcel, make sure it is still equivalent
		Parcel parcel = Parcel.obtain();
		attraction.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		byte[] data = parcel.marshall();
		parcel.unmarshall(data, 0, data.length);
		parcel.setDataPosition(0);
		attraction = LocalExpertAttraction.CREATOR.createFromParcel(parcel);

		assertEquals(firstLine, attraction.getFirstLine());
		assertEquals(secondLine, attraction.getSecondLine());
		assertEquals(iconSmall, attraction.getIconSmall());
		assertEquals(iconLarge, attraction.getIconLarge());
		assertEquals(iconAboveText, attraction.isIconAboveText());
	}

	public void testLocalExpertSite() {
		String city = "San Francisco";
		int cityIcon = 1;
		String phoneNumber = "2222222222";
		String firstLine = "first line";
		String secondLine = "second line";
		int iconSmall = 1;
		int iconLarge = 2;
		boolean iconAboveText = true;

		// Build the attraction
		LocalExpertAttraction.Builder builder = new LocalExpertAttraction.Builder(getContext());
		builder.setFirstLine(firstLine);
		builder.setSecondLine(secondLine);
		builder.setIconSmall(iconSmall);
		builder.setIconLarge(iconLarge);
		builder.setIconAboveText(iconAboveText);
		LocalExpertAttraction attraction = builder.build();

		// Build the local expert site
		LocalExpertSite.Builder builder2 = new LocalExpertSite.Builder(getContext());
		builder2.setCity(city);
		builder2.setCityIcon(cityIcon);
		builder2.setPhoneNumber(phoneNumber);
		builder2.addAttraction(attraction);
		LocalExpertSite site = builder2.build();

		// Make sure everything made it in alright
		assertEquals(city, site.getCity());
		assertEquals(cityIcon, site.getCityIcon());
		assertEquals(phoneNumber, site.getPhoneNumber());
		attraction = site.getAttractions().get(0);
		assertEquals(firstLine, attraction.getFirstLine());
		assertEquals(secondLine, attraction.getSecondLine());
		assertEquals(iconSmall, attraction.getIconSmall());
		assertEquals(iconLarge, attraction.getIconLarge());
		assertEquals(iconAboveText, attraction.isIconAboveText());

		// Parcel/unparcel, make sure it is still equivalent
		Parcel parcel = Parcel.obtain();
		site.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		byte[] data = parcel.marshall();
		parcel.unmarshall(data, 0, data.length);
		parcel.setDataPosition(0);
		site = LocalExpertSite.CREATOR.createFromParcel(parcel);

		assertEquals(city, site.getCity());
		assertEquals(cityIcon, site.getCityIcon());
		assertEquals(phoneNumber, site.getPhoneNumber());
		attraction = site.getAttractions().get(0);
		assertEquals(firstLine, attraction.getFirstLine());
		assertEquals(secondLine, attraction.getSecondLine());
		assertEquals(iconSmall, attraction.getIconSmall());
		assertEquals(iconLarge, attraction.getIconLarge());
		assertEquals(iconAboveText, attraction.isIconAboveText());
	}
}
