package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.data.lx.LXImage;
import com.expedia.bookings.utils.Images;

import junit.framework.Assert;

@RunWith(RobolectricRunner.class)
public class ImagesTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Test
	public void testLXImageURLsForHighResDevice() {
		List<String> urls = Images.getLXImageURLBasedOnWidth(getAllResImages(), 1000);
		Assert.assertEquals(3, urls.size());
		Assert.assertEquals("https:large", urls.get(0));
		Assert.assertEquals("https:medium", urls.get(1));
		Assert.assertEquals("https:small", urls.get(2));
	}

	@Test
	public void testLXImageURLsForLowResDevice() {
		List<String> urls = Images.getLXImageURLBasedOnWidth(getAllResImages(), 300);
		Assert.assertEquals(3, urls.size());
		Assert.assertEquals("https:small", urls.get(0));
		Assert.assertEquals("https:medium", urls.get(1));
		Assert.assertEquals("https:large", urls.get(2));
	}

	@Test
	public void testLXImageURLsForMediumResDevice() {
		List<String> urls = Images.getLXImageURLBasedOnWidth(getAllResImages(), 450);
		Assert.assertEquals(3, urls.size());
		Assert.assertEquals("https:medium", urls.get(0));
		Assert.assertEquals("https:large", urls.get(1));
		Assert.assertEquals("https:small", urls.get(2));
	}

	@Test
	public void testLXImageURLsForTwoImagesAvailable() {
		List<LXImage> images = new ArrayList<>();
		images.add(new LXImage("medium", LXImage.ImageSize.MEDIUM));
		images.add(new LXImage("large", LXImage.ImageSize.LARGE));

		List<String> urls = Images.getLXImageURLBasedOnWidth(images, 1000);
		Assert.assertEquals(2, urls.size());
		Assert.assertEquals("https:large", urls.get(0));
		Assert.assertEquals("https:medium", urls.get(1));
	}

	@Test
	public void testLXImageURLsForSingleImageAvailable() {
		List<LXImage> images = new ArrayList<>();
		images.add(new LXImage("large", LXImage.ImageSize.LARGE));

		List<String> urls = Images.getLXImageURLBasedOnWidth(images, 450);
		Assert.assertEquals(1, urls.size());
		Assert.assertEquals("https:large", urls.get(0));
	}

	@Test
	public void testLXImageURLsForNoImageAvailable() {
		List<LXImage> images = new ArrayList<>();
		List<String> urls = Images.getLXImageURLBasedOnWidth(images, 450);
		Assert.assertEquals(0, urls.size());
	}

	@Test
	public void testLXAllThingsToDoCategoryWithImageCodeDestinationImage() {
		final String categoryAllThingsToDo = "All Things To Do";
		final String imageCode = "QSF";
		final String expectedURL =
			Images.getMediaHost()
				+ "/mobiata/mobile/apps/ExpediaBooking/TabletDestinations/images/QSF.jpg?downsize=450px:*&";
		String obtainedURL = Images.forLxCategory(getContext(), categoryAllThingsToDo, imageCode, 450);
		Assert.assertEquals(expectedURL, obtainedURL);
	}

	@Test
	public void testLXAllThingsToDoCategoryWithoutImageCodeDestinationImage() {
		final String categoryAllThingsToDo = "All Things To Do";
		final String imageCode = "";
		final String expectedURL =
			Images.getMediaHost()
				+ "/mobiata/mobile/apps/ExpediaBooking/ActivityCategories/images/AllThingsToDo.jpg?downsize=450px:*&";
		String obtainedURL = Images.forLxCategory(getContext(), categoryAllThingsToDo, imageCode, 450);
		Assert.assertEquals(expectedURL, obtainedURL);
	}

	@Test
	public void testLXWhenCategoryIsNotAllThingsToDoDestinationImage() {
		final String categoryAllThingsToDo = "Attractions";
		final String imageCode = "";
		final String expectedURL =
			Images.getMediaHost()
				+ "/mobiata/mobile/apps/ExpediaBooking/ActivityCategories/images/Attractions.jpg?downsize=450px:*&";
		String obtainedURL = Images.forLxCategory(getContext(), categoryAllThingsToDo, imageCode, 450);
		Assert.assertEquals(expectedURL, obtainedURL);
	}

	private List<LXImage> getAllResImages() {
		List<LXImage> images = new ArrayList<>();
		images.add(new LXImage("small", LXImage.ImageSize.SMALL));
		images.add(new LXImage("medium", LXImage.ImageSize.MEDIUM));
		images.add(new LXImage("large", LXImage.ImageSize.LARGE));
		return images;
	}
}
