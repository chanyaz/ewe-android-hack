package com.expedia.bookings.data;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.Images;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
public class HotelMediaTest {

	@Test
	public void fromToJsonWithPlaceholder() throws Exception {
		HotelMedia mediaWithPlaceholder = new HotelMedia();
		mediaWithPlaceholder.setIsPlaceholder(true);

		JSONObject jsonObject = mediaWithPlaceholder.toJson();
		HotelMedia objFromJson = new HotelMedia();
		objFromJson.fromJson(jsonObject);

		assertTrue(objFromJson.getIsPlaceHolder());
		assertEquals(mediaWithPlaceholder, objFromJson);
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ})
	public void fromToJsonWithUrl() {
		HotelMedia mediaWithUrl = new HotelMedia(Images.getMediaHost() + "/hotels/1000000/50000/41300/41245/41245_228_l.jpg");

		JSONObject jsonObject = mediaWithUrl.toJson();
		HotelMedia objFromJson = new HotelMedia();
		objFromJson.fromJson(jsonObject);

		assertEquals("https://images.trvl-media.com/hotels/1000000/50000/41300/41245/41245_228_l.jpg", objFromJson.getOriginalUrl());
		assertFalse(objFromJson.getIsPlaceHolder());
		assertEquals(mediaWithUrl, objFromJson);
	}
}
